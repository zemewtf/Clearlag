package me.minebuilders.clearlag.entities.attributes;

import org.bukkit.entity.Entity;

/**
 * @author bob7l
 */
public class EntityMountedAttribute extends EntityAttribute<Entity> {

    @Override
    public boolean containsData(Entity entity) {
        boolean isMounted = (entity.getVehicle() != null) || !entity.isEmpty() || (entity.getPassengers() != null && !entity.getPassengers().isEmpty());
        return (reversed != isMounted);
    }

}
