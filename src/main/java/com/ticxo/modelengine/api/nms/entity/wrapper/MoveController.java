package com.ticxo.modelengine.api.nms.entity.wrapper;

import com.ticxo.modelengine.api.ModelEngineAPI;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public interface MoveController {
   void move(float var1, float var2, float var3, float var4);

   void globalMove(float var1, float var2, float var3, float var4);

   void jump();

   void setVelocity(double var1, double var3, double var5);

   void addVelocity(double var1, double var3, double var5);

   void nullifyFallDistance();

   default void movePassenger(Entity entity, double x, double y, double z) {
      ModelEngineAPI.getEntityHandler().movePassenger(entity, x, y, z);
   }

   boolean isOnGround();

   boolean isInWater();

   float getSpeed();

   Vector getVelocity();

   void queuePostTick(Runnable var1);
}
