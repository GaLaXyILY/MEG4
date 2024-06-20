package com.ticxo.modelengine.api.nms.impl;

import com.ticxo.modelengine.api.nms.entity.wrapper.MoveController;
import org.bukkit.util.Vector;

public class EmptyMoveController implements MoveController {
   public void move(float side, float up, float front, float speedModifier) {
   }

   public void globalMove(float x, float y, float z, float speedModifier) {
   }

   public void jump() {
   }

   public void setVelocity(double x, double y, double z) {
   }

   public void addVelocity(double x, double y, double z) {
   }

   public void nullifyFallDistance() {
   }

   public boolean isOnGround() {
      return false;
   }

   public boolean isInWater() {
      return false;
   }

   public float getSpeed() {
      return 0.0F;
   }

   public Vector getVelocity() {
      return new Vector();
   }

   public void queuePostTick(Runnable runnable) {
      runnable.run();
   }
}
