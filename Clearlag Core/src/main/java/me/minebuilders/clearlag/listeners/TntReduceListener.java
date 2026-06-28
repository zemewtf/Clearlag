package me.minebuilders.clearlag.listeners;

import me.minebuilders.clearlag.annotations.ConfigPath;
import me.minebuilders.clearlag.annotations.ConfigValue;
import me.minebuilders.clearlag.modules.EventModule;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityExplodeEvent;

@ConfigPath(path = "tnt-reducer")
public class TntReduceListener extends EventModule {

    @ConfigValue
    private final int checkRadius = 5;

    @ConfigValue
    private final int maxPrimed = 3;

    @EventHandler
    public void onBoom(EntityExplodeEvent event) {

        Entity e = event.getEntity();

        org.bukkit.entity.EntityType tntType = me.minebuilders.clearlag.SchedulerUtil.TNT_TYPE;
        if (tntType != null && e.getType() == tntType) {

            int counter = 0;

            for (Entity tnt : e.getNearbyEntities(checkRadius, checkRadius, checkRadius)) {

                if (tntType != null && tnt.getType() == tntType)

                    if (counter > maxPrimed)
                        tnt.remove();
                else
                    ++counter;
            }
        }
    }

}
