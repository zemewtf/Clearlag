package me.minebuilders.clearlag.entities.attributes;

import org.bukkit.entity.Entity;

public class EntityHasPassengerAttribute extends EntityAttribute<Entity> {

    @Override
    public boolean containsData(Entity entity) {
        boolean hasPassenger = !entity.isEmpty() || (entity.getPassengers() != null && !entity.getPassengers().isEmpty());
        return (reversed != hasPassenger);
    }

}
