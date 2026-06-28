package me.minebuilders.clearlag.managers;

import me.minebuilders.clearlag.SchedulerUtil;
import me.minebuilders.clearlag.annotations.ConfigValue;
import me.minebuilders.clearlag.events.EntityRemoveEvent;
import me.minebuilders.clearlag.modules.ClearModule;
import me.minebuilders.clearlag.modules.ClearlagModule;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class EntityManager extends ClearlagModule {

    @ConfigValue(path = "settings.enable-api")
    private final boolean enabled = true;

    public void removeEntities(ClearModule mod, Consumer<Integer> callback) {
        if (SchedulerUtil.IS_FOLIA) {
            SchedulerUtil.runGlobal(() -> {
                List<World> worlds = Bukkit.getWorlds();
                AtomicInteger totalRemoved = new AtomicInteger(0);
                
                int totalChunksCount = 0;
                for (World w : worlds) {
                    if (mod.isWorldEnabled(w)) {
                        totalChunksCount += w.getLoadedChunks().length;
                    }
                }
                
                if (totalChunksCount == 0) {
                    callback.accept(0);
                    return;
                }
                
                AtomicInteger remainingChunks = new AtomicInteger(totalChunksCount);
                
                for (World w : worlds) {
                    if (!mod.isWorldEnabled(w)) continue;
                    for (Chunk chunk : w.getLoadedChunks()) {
                        int cx = chunk.getX();
                        int cz = chunk.getZ();
                        SchedulerUtil.runRegion(w, cx, cz, () -> {
                            try {
                                if (chunk.isLoaded()) {
                                    List<Entity> removables = mod.getRemovables(Arrays.asList(chunk.getEntities()), w);
                                    int removed = removeEntities(removables, w);
                                    totalRemoved.addAndGet(removed);
                                }
                            } finally {
                                if (remainingChunks.decrementAndGet() == 0) {
                                    SchedulerUtil.runGlobal(() -> callback.accept(totalRemoved.get()));
                                }
                            }
                        });
                    }
                }
            });
        } else {
            int removed = 0;
            for (World w : Bukkit.getWorlds()) {
                if (mod.isWorldEnabled(w)) {
                    removed += removeEntities(mod.getRemovables(new java.util.ArrayList<>(w.getEntities()), w), w);
                }
            }
            callback.accept(removed);
        }
    }

    public int removeEntities(List<Entity> removables, World w) {

        EntityRemoveEvent et = new EntityRemoveEvent(removables, w);

        if (enabled)
            Bukkit.getPluginManager().callEvent(et);

        for (Entity en : et.getEntityList())
            en.remove();

        return et.getEntityList().size();
    }
}