package me.minebuilders.clearlag.commands;

import me.minebuilders.clearlag.Clearlag;
import me.minebuilders.clearlag.RAMUtil;
import me.minebuilders.clearlag.Util;
import me.minebuilders.clearlag.annotations.AutoWire;
import me.minebuilders.clearlag.exceptions.WrongCommandArgumentException;
import me.minebuilders.clearlag.language.LanguageValue;
import me.minebuilders.clearlag.language.messages.MessageTree;
import me.minebuilders.clearlag.modules.CommandModule;
import me.minebuilders.clearlag.tasks.TPSTask;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.Hopper;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CheckCmd extends CommandModule {

    @AutoWire
    private TPSTask tpsTask;

    @LanguageValue(key = "command.check.")
    private MessageTree lang;

    @Override
    protected void run(CommandSender sender, String[] args) throws WrongCommandArgumentException {

        final List<World> worlds;

        if (args.length > 0) {

            worlds = new ArrayList<>(args.length);

            for (String arg : args) {

                World world = Bukkit.getWorld(arg);

                if (world == null)
                    throw new WrongCommandArgumentException(lang.getMessage("invalidworld"), arg);

                worlds.add(world);
            }

        } else {
            worlds = Bukkit.getWorlds();
        }

        if (me.minebuilders.clearlag.SchedulerUtil.IS_FOLIA) {
            sender.sendMessage(org.bukkit.ChatColor.GOLD + "Gathering server statistics across regional threads...");
            me.minebuilders.clearlag.SchedulerUtil.runGlobal(() -> {
                int totalChunksCount = 0;
                for (World w : worlds) {
                    totalChunksCount += w.getLoadedChunks().length;
                }

                if (totalChunksCount == 0) {
                    printStats(sender, 0, 0, 0, 0, 0, 0, 0, 0);
                    return;
                }

                final java.util.concurrent.atomic.AtomicInteger remainingChunks = new java.util.concurrent.atomic.AtomicInteger(totalChunksCount);
                final java.util.concurrent.atomic.AtomicInteger removed1 = new java.util.concurrent.atomic.AtomicInteger();
                final java.util.concurrent.atomic.AtomicInteger mobs = new java.util.concurrent.atomic.AtomicInteger();
                final java.util.concurrent.atomic.AtomicInteger animals = new java.util.concurrent.atomic.AtomicInteger();
                final java.util.concurrent.atomic.AtomicInteger spawners = new java.util.concurrent.atomic.AtomicInteger();
                final java.util.concurrent.atomic.AtomicInteger activehoppers = new java.util.concurrent.atomic.AtomicInteger();
                final java.util.concurrent.atomic.AtomicInteger inactivehoppers = new java.util.concurrent.atomic.AtomicInteger();
                final java.util.concurrent.atomic.AtomicInteger players = new java.util.concurrent.atomic.AtomicInteger();
                final java.util.concurrent.atomic.AtomicInteger chunks = new java.util.concurrent.atomic.AtomicInteger();

                for (World w : worlds) {
                    for (Chunk c : w.getLoadedChunks()) {
                        int cx = c.getX();
                        int cz = c.getZ();
                        me.minebuilders.clearlag.SchedulerUtil.runRegion(w, cx, cz, () -> {
                            try {
                                if (c.isLoaded()) {
                                    chunks.incrementAndGet();
                                    for (BlockState bt : c.getTileEntities()) {
                                        if (bt instanceof CreatureSpawner) {
                                            spawners.incrementAndGet();
                                        } else if (bt instanceof Hopper) {
                                            if (!isHopperEmpty((Hopper) bt)) {
                                                activehoppers.incrementAndGet();
                                            } else {
                                                inactivehoppers.incrementAndGet();
                                            }
                                        }
                                    }

                                    for (Entity e : c.getEntities()) {
                                        if (e instanceof Monster) mobs.incrementAndGet();
                                        else if (e instanceof Player) players.incrementAndGet();
                                        else if (e instanceof Creature) animals.incrementAndGet();
                                        else if (e instanceof Item) removed1.incrementAndGet();
                                    }
                                }
                            } finally {
                                if (remainingChunks.decrementAndGet() == 0) {
                                    me.minebuilders.clearlag.SchedulerUtil.runGlobal(() -> {
                                        printStats(sender, removed1.get(), mobs.get(), animals.get(), players.get(), chunks.get(), activehoppers.get(), inactivehoppers.get(), spawners.get());
                                    });
                                }
                            }
                        });
                    }
                }
            });
        } else {
            int removed1 = 0, mobs = 0, animals = 0, chunks = 0, spawners = 0, activehoppers = 0, inactivehoppers = 0, players = 0;

            for (World w : worlds) {

                for (Chunk c : w.getLoadedChunks()) {

                    for (BlockState bt : c.getTileEntities()) {

                        if (bt instanceof CreatureSpawner) {

                            spawners++;

                        } else if (bt instanceof Hopper) {

                            if (!isHopperEmpty((Hopper) bt)) {
                                activehoppers++;
                            } else {
                                inactivehoppers++;
                            }
                        }
                    }

                    for (Entity e : c.getEntities()) {
                        if (e instanceof Monster) mobs++;
                        else if (e instanceof Player) players++;
                        else if (e instanceof Creature) animals++;
                        else if (e instanceof Item) removed1++;
                    }
                    chunks++;
                }
            }

            printStats(sender, removed1, mobs, animals, players, chunks, activehoppers, inactivehoppers, spawners);
        }
    }

    private void printStats(CommandSender sender, int removed1, int mobs, int animals, int players, int chunks, int activehoppers, int inactivehoppers, int spawners) {
        lang.sendMessage("header", sender);

        lang.sendMessage("printed", sender,
                removed1,
                mobs,
                animals,
                players,
                chunks,
                activehoppers,
                inactivehoppers,
                spawners,
                Util.getTime(System.currentTimeMillis() - Clearlag.getInstance().getInitialBootTimestamp()),
                tpsTask.getStringTPS(),
                RAMUtil.getUsedMemory(), RAMUtil.getMaxMemory(),
                (RAMUtil.getMaxMemory() - RAMUtil.getUsedMemory())
        );

        lang.sendMessage("footer", sender);
    }

    private boolean isHopperEmpty(Hopper hop) {
        for (ItemStack it : hop.getInventory().getContents()) {
            if (it != null) return false;
        }
        return true;
    }
}
