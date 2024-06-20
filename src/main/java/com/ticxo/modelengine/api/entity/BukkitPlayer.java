package com.ticxo.modelengine.api.entity;

import com.ticxo.modelengine.api.entity.data.BukkitEntityData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class BukkitPlayer extends BukkitEntity {
   public BukkitPlayer(Player original) {
      super(original);
   }

   protected BukkitEntityData createEntityData(Entity original) {
      return new BukkitPlayer.BukkitPlayerData(original);
   }

   public boolean isWalking() {
      return ((BukkitPlayer.BukkitPlayerData)this.getData()).getWalkTick() > 0;
   }

   public boolean isJumping() {
      return ((BukkitPlayer.BukkitPlayerData)this.getData()).getJumpTick() > 0;
   }

   public boolean isFlying() {
      return ((BukkitPlayer.BukkitPlayerData)this.getData()).isFlying;
   }

   public static class BukkitPlayerData extends BukkitEntityData {
      private int walkTick;
      private int jumpTick;
      private boolean isFlying;

      public BukkitPlayerData(Entity entity) {
         super(entity);
      }

      public void syncUpdate() {
         super.syncUpdate();
         if (this.walkTick > 0) {
            --this.walkTick;
         }

         if (this.jumpTick > 0 && this.entity.isOnGround()) {
            --this.jumpTick;
         }

         this.isFlying = ((Player)this.entity).isFlying();
      }

      public int getWalkTick() {
         return this.walkTick;
      }

      public void setWalkTick(int walkTick) {
         this.walkTick = walkTick;
      }

      public int getJumpTick() {
         return this.jumpTick;
      }

      public void setJumpTick(int jumpTick) {
         this.jumpTick = jumpTick;
      }
   }
}
