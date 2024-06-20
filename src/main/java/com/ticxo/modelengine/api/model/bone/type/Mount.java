package com.ticxo.modelengine.api.model.bone.type;

import java.util.Collection;
import java.util.Set;
import org.bukkit.entity.Entity;
import org.joml.Vector3f;

public interface Mount {
   boolean isDriver();

   Vector3f getLocation();

   Vector3f getGlobalLocation();

   boolean addPassenger(Entity var1);

   boolean addPassengers(Collection<Entity> var1);

   void removePassenger(Entity var1);

   void removePassengers(Collection<Entity> var1);

   Set<Entity> clearPassengers();

   Set<Entity> getPassengers();

   boolean canMountMore();
}
