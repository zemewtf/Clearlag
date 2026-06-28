package me.minebuilders.clearlag;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

public class SchedulerUtil {

    public static final boolean IS_FOLIA = isFoliaPresent();

    public static final org.bukkit.entity.EntityType ITEM_TYPE = getEntityType("ITEM", "DROPPED_ITEM");
    public static final org.bukkit.entity.EntityType TNT_TYPE = getEntityType("TNT", "PRIMED_TNT");

    private static org.bukkit.entity.EntityType getEntityType(String modernName, String legacyName) {
        try {
            return org.bukkit.entity.EntityType.valueOf(modernName);
        } catch (IllegalArgumentException e) {
            try {
                return org.bukkit.entity.EntityType.valueOf(legacyName);
            } catch (IllegalArgumentException ex) {
                return null;
            }
        }
    }

    private static boolean isFoliaPresent() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static TaskRef scheduleRepeatingGlobal(Runnable runnable, long delayTicks, long periodTicks) {
        Plugin plugin = Clearlag.getInstance();
        if (IS_FOLIA) {
            ScheduledTask task = Bukkit.getGlobalRegionScheduler().runAtFixedRate(
                    plugin,
                    t -> runnable.run(),
                    Math.max(1, delayTicks),
                    Math.max(1, periodTicks)
            );
            return new TaskRef(task, true);
        } else {
            int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(
                    plugin,
                    runnable,
                    delayTicks,
                    periodTicks
            );
            return new TaskRef(taskId, false);
        }
    }

    public static void runAsync(Runnable runnable) {
        Plugin plugin = Clearlag.getInstance();
        if (IS_FOLIA) {
            Bukkit.getAsyncScheduler().runNow(plugin, t -> runnable.run());
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
        }
    }

    public static void runGlobal(Runnable runnable) {
        Plugin plugin = Clearlag.getInstance();
        if (IS_FOLIA) {
            Bukkit.getGlobalRegionScheduler().run(plugin, t -> runnable.run());
        } else {
            Bukkit.getScheduler().runTask(plugin, runnable);
        }
    }

    public static void runGlobalLater(Runnable runnable, long delayTicks) {
        Plugin plugin = Clearlag.getInstance();
        if (IS_FOLIA) {
            Bukkit.getGlobalRegionScheduler().runDelayed(
                    plugin,
                    t -> runnable.run(),
                    Math.max(1, delayTicks)
            );
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, runnable, delayTicks);
        }
    }

    public static void runRegion(Location loc, Runnable runnable) {
        Plugin plugin = Clearlag.getInstance();
        if (IS_FOLIA) {
            Bukkit.getRegionScheduler().run(plugin, loc, t -> runnable.run());
        } else {
            Bukkit.getScheduler().runTask(plugin, runnable);
        }
    }

    public static void runRegion(World world, int chunkX, int chunkZ, Runnable runnable) {
        Plugin plugin = Clearlag.getInstance();
        if (IS_FOLIA) {
            Bukkit.getRegionScheduler().run(plugin, world, chunkX, chunkZ, t -> runnable.run());
        } else {
            Bukkit.getScheduler().runTask(plugin, runnable);
        }
    }

    public static void runEntity(Entity entity, Runnable runnable) {
        Plugin plugin = Clearlag.getInstance();
        if (IS_FOLIA) {
            entity.getScheduler().run(plugin, t -> runnable.run(), null);
        } else {
            Bukkit.getScheduler().runTask(plugin, runnable);
        }
    }

    public static class TaskRef {
        private final Object task;
        private final boolean isFolia;

        public TaskRef(Object task, boolean isFolia) {
            this.task = task;
            this.isFolia = isFolia;
        }

        public void cancel() {
            if (task == null) return;
            if (isFolia) {
                ((ScheduledTask) task).cancel();
            } else {
                Bukkit.getScheduler().cancelTask((Integer) task);
            }
        }
    }
}
