package com.ticxo.modelengine.api.mount.controller;

import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.bone.type.Mount;
import com.ticxo.modelengine.api.nms.entity.wrapper.LookController;
import com.ticxo.modelengine.api.nms.entity.wrapper.MoveController;
import org.bukkit.entity.Entity;
import org.joml.Vector3f;

public interface MountController {
   Entity getEntity();

   MountController.MountInput getInput();

   void setInput(MountController.MountInput var1);

   Mount getMount();

   void setCanDamageMount(boolean var1);

   boolean canDamageMount();

   void setCanInteractMount(boolean var1);

   boolean canInteractMount();

   void updateDriverMovement(MoveController var1, ActiveModel var2);

   void updatePassengerMovement(MoveController var1, ActiveModel var2);

   default void updateRiderPosition(MoveController controller) {
      Vector3f pos = this.getMount().getGlobalLocation();
      controller.movePassenger(this.getEntity(), (double)pos.x, (double)pos.y, (double)pos.z);
   }

   void updateDirection(LookController var1, ActiveModel var2);

   public static class MountInput {
      private float side;
      private float front;
      private boolean jump;
      private boolean sneak;
      private boolean updated;

      public MountInput() {
         this(0.0F, 0.0F, false, false);
      }

      public MountInput(float side, float front, boolean jump, boolean sneak) {
         this.side = side;
         this.front = front;
         this.jump = jump;
         this.sneak = sneak;
      }

      public float getSide() {
         return this.side;
      }

      public float getFront() {
         return this.front;
      }

      public boolean isJump() {
         return this.jump;
      }

      public boolean isSneak() {
         return this.sneak;
      }

      public boolean isUpdated() {
         return this.updated;
      }

      public void setSide(float side) {
         this.side = side;
      }

      public void setFront(float front) {
         this.front = front;
      }

      public void setJump(boolean jump) {
         this.jump = jump;
      }

      public void setSneak(boolean sneak) {
         this.sneak = sneak;
      }

      public void setUpdated(boolean updated) {
         this.updated = updated;
      }
   }
}
