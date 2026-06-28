package me.minebuilders.clearlag.commands;

import me.minebuilders.clearlag.Util;
import me.minebuilders.clearlag.exceptions.WrongCommandArgumentException;
import me.minebuilders.clearlag.language.LanguageValue;
import me.minebuilders.clearlag.language.messages.MessageTree;
import me.minebuilders.clearlag.modules.CommandModule;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class TpChunkCmd extends CommandModule {

    @LanguageValue(key = "command.tpchunk.")
    private MessageTree lang;

    public TpChunkCmd() {
        argLength = 2;
    }

    @Override
    protected void run(Player player, String[] args) throws WrongCommandArgumentException {

        final World world;

        if (args.length >= 3) {
            world = Bukkit.getServer().getWorld(args[2]);

            if (world == null)
                throw new WrongCommandArgumentException(lang.getMessage("invalidworld"), args[2]);

        } else {
            world = player.getWorld();
        }

        if (!Util.isInteger(args[0]) || !Util.isInteger(args[1]))
            throw new WrongCommandArgumentException(lang.getMessage("invalidinteger"), (Util.isInteger(args[0]) ? args[1] : args[0]));

        final int chunkX = Integer.parseInt(args[0]);
        final int chunkZ = Integer.parseInt(args[1]);

        final int x = chunkX * 16 + 8;
        final int z = chunkZ * 16 + 8;

        player.teleport(new Location(world, x, world.getHighestBlockYAt(x, z), z));

        lang.sendMessage("teleported", player, chunkX, chunkZ);
    }
}