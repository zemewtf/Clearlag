package me.minebuilders.clearlag.listeners;

import me.minebuilders.clearlag.annotations.ConfigPath;
import me.minebuilders.clearlag.annotations.ConfigValue;
import me.minebuilders.clearlag.modules.EventModule;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;

@ConfigPath(path = "item-merger")
public class ItemMergeListener extends EventModule {

    @ConfigValue
    private int radius;

    //Yes yes. Very ugly/outdated code. Ignore this class.. Shouldn't even be used anymore in Spigot
    @EventHandler
    public void onItemDrop(ItemSpawnEvent event) {

        Item itemEntity = event.getEntity();
        ItemStack i = itemEntity.getItemStack();
        int c = i.getAmount();

        for (Entity entity : itemEntity.getNearbyEntities(radius, radius, radius)) {

            if (entity instanceof Item && !entity.isDead()) {

                Item otherItem = (Item) entity;
                ItemStack ni = otherItem.getItemStack();

                if (i.isSimilar(ni) && i.getMaxStackSize() >= (ni.getAmount() + c)) {

                    entity.remove();

                    i.setAmount(ni.getAmount() + c);
                    itemEntity.setItemStack(i);

                    return;
                }
            }
        }
    }

}
