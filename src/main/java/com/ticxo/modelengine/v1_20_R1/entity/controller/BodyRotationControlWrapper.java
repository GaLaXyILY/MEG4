package com.ticxo.modelengine.v1_20_R1.entity.controller;

import com.ticxo.modelengine.api.nms.entity.wrapper.BodyRotationController;
import com.ticxo.modelengine.api.utils.math.TMath;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.ai.control.EntityAIBodyControl;

public class BodyRotationControlWrapper extends EntityAIBodyControl implements BodyRotationController {
   private final EntityInsentient mob;
   private boolean isHeadClampUneven;
   private boolean isBodyClampUneven;
   private float maxHeadAngle;
   private float maxBodyAngle;
   private float minHeadAngle;
   private float minBodyAngle;
   private boolean playerMode;
   private float stableAngle = 15.0F;
   private int rotationDelay = 10;
   private int rotationDuration = 10;
   private int headStableTime;
   private float lastStableYHeadRot;

   public BodyRotationControlWrapper(EntityInsentient mob) {
      super(mob);
      this.mob = mob;
      this.maxHeadAngle = (float)mob.fC();
      this.maxBodyAngle = (float)mob.fC();
      this.minHeadAngle = -this.maxHeadAngle;
      this.minBodyAngle = -this.maxBodyAngle;
   }

   public void a() {
      if (this.isMoving()) {
         this.mob.aV = this.mob.dy();
         this.rotateHeadIfNecessary();
         this.lastStableYHeadRot = this.mob.aX;
         this.headStableTime = 0;
      } else if (this.notCarryingMobPassengers()) {
         if (Math.abs(this.mob.aX - this.lastStableYHeadRot) > this.stableAngle) {
            this.headStableTime = 0;
            this.lastStableYHeadRot = this.mob.aX;
            this.rotateBodyIfNecessary();
         } else if (!this.playerMode) {
            ++this.headStableTime;
            if (this.headStableTime > this.rotationDelay) {
               this.rotateHeadTowardsFront();
            }
         }
      }

   }

   public float getYHeadRot() {
      return this.mob.aX;
   }

   public void setYHeadRot(float rot) {
      this.mob.aX = rot;
   }

   public float getXHeadRot() {
      return this.mob.dA();
   }

   public void setXHeadRot(float rot) {
      this.mob.b_(rot);
   }

   public float getYBodyRot() {
      return this.mob.aV;
   }

   public void setYBodyRot(float rot) {
      this.mob.aV = rot;
   }

   private void rotateBodyIfNecessary() {
      this.mob.aV = TMath.rotateIfNecessary(this.mob.aV, this.mob.aX, this.isBodyClampUneven ? this.minBodyAngle : -this.maxBodyAngle, this.maxBodyAngle);
   }

   private void rotateHeadIfNecessary() {
      this.mob.aX = TMath.rotateIfNecessary(this.mob.aX, this.mob.aV, this.isHeadClampUneven ? this.minHeadAngle : -this.maxHeadAngle, this.maxHeadAngle);
   }

   private void rotateHeadTowardsFront() {
      float ratio = (float)(this.headStableTime - this.rotationDelay) / (float)this.rotationDuration;
      float clampedRatio = TMath.clamp(ratio, 0.0F, 1.0F);
      float maxClamp = this.maxHeadAngle * (1.0F - clampedRatio);
      float minClamp = (this.isHeadClampUneven ? this.minHeadAngle : -this.maxHeadAngle) * (1.0F - clampedRatio);
      this.mob.aV = TMath.rotateIfNecessary(this.mob.aV, this.mob.aX, minClamp, maxClamp);
   }

   private boolean notCarryingMobPassengers() {
      return !(this.mob.cO() instanceof EntityInsentient);
   }

   private boolean isMoving() {
      double dX = this.mob.dn() - this.mob.J;
      double dZ = this.mob.dt() - this.mob.L;
      return dX * dX + dZ * dZ > 2.500000277905201E-7D;
   }

   public EntityInsentient getMob() {
      return this.mob;
   }

   public boolean isHeadClampUneven() {
      return this.isHeadClampUneven;
   }

   public boolean isBodyClampUneven() {
      return this.isBodyClampUneven;
   }

   public float getMaxHeadAngle() {
      return this.maxHeadAngle;
   }

   public float getMaxBodyAngle() {
      return this.maxBodyAngle;
   }

   public float getMinHeadAngle() {
      return this.minHeadAngle;
   }

   public float getMinBodyAngle() {
      return this.minBodyAngle;
   }

   public boolean isPlayerMode() {
      return this.playerMode;
   }

   public float getStableAngle() {
      return this.stableAngle;
   }

   public int getRotationDelay() {
      return this.rotationDelay;
   }

   public int getRotationDuration() {
      return this.rotationDuration;
   }

   public int getHeadStableTime() {
      return this.headStableTime;
   }

   public float getLastStableYHeadRot() {
      return this.lastStableYHeadRot;
   }

   public void setHeadClampUneven(boolean isHeadClampUneven) {
      this.isHeadClampUneven = isHeadClampUneven;
   }

   public void setBodyClampUneven(boolean isBodyClampUneven) {
      this.isBodyClampUneven = isBodyClampUneven;
   }

   public void setMaxHeadAngle(float maxHeadAngle) {
      this.maxHeadAngle = maxHeadAngle;
   }

   public void setMaxBodyAngle(float maxBodyAngle) {
      this.maxBodyAngle = maxBodyAngle;
   }

   public void setMinHeadAngle(float minHeadAngle) {
      this.minHeadAngle = minHeadAngle;
   }

   public void setMinBodyAngle(float minBodyAngle) {
      this.minBodyAngle = minBodyAngle;
   }

   public void setPlayerMode(boolean playerMode) {
      this.playerMode = playerMode;
   }

   public void setStableAngle(float stableAngle) {
      this.stableAngle = stableAngle;
   }

   public void setRotationDelay(int rotationDelay) {
      this.rotationDelay = rotationDelay;
   }

   public void setRotationDuration(int rotationDuration) {
      this.rotationDuration = rotationDuration;
   }

   public void setHeadStableTime(int headStableTime) {
      this.headStableTime = headStableTime;
   }

   public void setLastStableYHeadRot(float lastStableYHeadRot) {
      this.lastStableYHeadRot = lastStableYHeadRot;
   }
}
