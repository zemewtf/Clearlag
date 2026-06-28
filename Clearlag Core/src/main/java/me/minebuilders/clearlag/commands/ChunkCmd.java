package me.minebuilders.clearlag.commands;

import me.minebuilders.clearlag.Util;
import me.minebuilders.clearlag.language.LanguageValue;
import me.minebuilders.clearlag.language.messages.Message;
import me.minebuilders.clearlag.modules.CommandModule;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

public class ChunkCmd extends CommandModule {

    @LanguageValue(key = "command.chunk.header")
    private Message header;

    @LanguageValue(key = "command.chunk.print")
    private Message outprint;

    @Override
    protected void run(CommandSender sender, String[] args) {

        final int size = (args.length >= 1 && Util.isInteger(args[0])) ? Integer.parseInt(args[0]) : 5;

        if (me.minebuilders.clearlag.SchedulerUtil.IS_FOLIA) {
            sender.sendMessage(org.bukkit.ChatColor.GOLD + "Scanning chunks across regional threads...");
            me.minebuilders.clearlag.SchedulerUtil.runGlobal(() -> {
                java.util.List<World> worlds = Bukkit.getWorlds();
                int totalChunksCount = 0;
                for (World w : worlds) {
                    totalChunksCount += w.getLoadedChunks().length;
                }

                if (totalChunksCount == 0) {
                    header.sendMessage(sender);
                    return;
                }

                final java.util.concurrent.atomic.AtomicInteger remainingChunks = new java.util.concurrent.atomic.AtomicInteger(totalChunksCount);
                final java.util.List<ChunkCountResult> results = java.util.Collections.synchronizedList(new java.util.ArrayList<>());

                for (World world : worlds) {
                    for (Chunk c : world.getLoadedChunks()) {
                        int cx = c.getX();
                        int cz = c.getZ();
                        me.minebuilders.clearlag.SchedulerUtil.runRegion(world, cx, cz, () -> {
                            try {
                                if (c.isLoaded()) {
                                    int amount = c.getEntities().length;
                                    results.add(new ChunkCountResult(c, amount));
                                }
                            } finally {
                                if (remainingChunks.decrementAndGet() == 0) {
                                    me.minebuilders.clearlag.SchedulerUtil.runGlobal(() -> {
                                        results.sort((o1, o2) -> Integer.compare(o2.amount, o1.amount));

                                        header.sendMessage(sender);

                                        int maxIndex = Math.min(size, results.size());
                                        for (int i = 0; i < maxIndex; i++) {
                                            ChunkCountResult res = results.get(i);
                                            outprint.sendMessage(sender, (i + 1), res.chunk.getWorld().getName(), res.chunk.getX(), res.chunk.getZ(), res.amount);
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            });
        } else {
            final Integer[] sizes = new Integer[size];
            final Chunk[] chunks = new Chunk[size];

            for (World world : Bukkit.getWorlds()) {

                for (Chunk c : world.getLoadedChunks()) {

                    final int amount = c.getEntities().length;

                    for (int i = 0; i < size; i++) {

                        if (sizes[i] == null || sizes[i] < amount) {

                            Util.shiftRight(chunks, i);
                            Util.shiftRight(sizes, i);

                            chunks[i] = c;
                            sizes[i] = amount;

                            break;
                        }
                    }
                }
            }

            header.sendMessage(sender);

            for (int i = 0; i < sizes.length; i++) {

                final Chunk c = chunks[i];

                if (c != null && sizes[i] != null) {
                    outprint.sendMessage(sender, (i + 1), c.getWorld().getName(), c.getX(), c.getZ(), sizes[i]);
                }
            }
        }
    }

    private static class ChunkCountResult {
        final Chunk chunk;
        final int amount;

        ChunkCountResult(Chunk chunk, int amount) {
            this.chunk = chunk;
            this.amount = amount;
        }
    }
}