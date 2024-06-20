package com.ticxo.modelengine.api.model.bone.type;

import com.ticxo.modelengine.api.nms.entity.HitboxEntity;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Entity;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public interface SubHitbox {
   boolean isOBB();

   Vector3f getFixedDimension();

   Vector3f getOrigin();

   float getDamageMultiplier();

   void setDamageMultiplier(float var1);

   void addBoundEntity(Entity var1);

   void removeBoundEntity(Entity var1);

   Map<UUID, Entity> getBoundEntities();

   int getHitboxId();

   Vector3f getDimension();

   Vector3f getLocation();

   Quaternionf getRotation();

   float getYaw();

   HitboxEntity getHitboxEntity();
}
