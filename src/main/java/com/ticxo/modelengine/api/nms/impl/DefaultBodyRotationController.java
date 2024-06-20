package com.ticxo.modelengine.api.nms.impl;

import com.ticxo.modelengine.api.entity.BaseEntity;
import com.ticxo.modelengine.api.nms.entity.wrapper.BodyRotationController;
import com.ticxo.modelengine.api.utils.math.TMath;
import java.util.Iterator;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;

public class DefaultBodyRotationController implements BodyRotationController {
   private final BaseEntity<?> entity;
   private float xHeadRot;
   private float yHeadRot;
   private float yBodyRot;
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

   public DefaultBodyRotationController(BaseEntity<?> entity) {
      this.entity = entity;
      this.xHeadRot = entity.getXHeadRot();
      this.yHeadRot = entity.getYHeadRot();
      this.yBodyRot = entity.getYBodyRot();
      this.maxHeadAngle = 75.0F;
      this.maxBodyAngle = 75.0F;
      this.minHeadAngle = -this.maxHeadAngle;
      this.minBodyAngle = -this.maxBodyAngle;
   }

   public void tick() {
      this.xHeadRot = this.entity.getXHeadRot();
      this.yHeadRot = this.entity.getYHeadRot();
      if (this.entity.isWalking()) {
         this.yBodyRot = this.entity.getYRot();
         this.rotateHeadIfNecessary();
         this.lastStableYHeadRot = this.yHeadRot;
         this.headStableTime = 0;
      } else if (this.notCarryingMobPassengers()) {
         if (Math.abs(this.yHeadRot - this.lastStableYHeadRot) > this.stableAngle) {
            this.headStableTime = 0;
            this.lastStableYHeadRot = this.yHeadRot;
            this.rotateBodyIfNecessary();
         } else if (!this.playerMode) {
            ++this.headStableTime;
            if (this.headStableTime > this.rotationDelay) {
               this.rotateHeadTowardsFront();
            }
         }
      }

   }

   private void rotateBodyIfNecessary() {
      this.yBodyRot = TMath.rotateIfNecessary(this.yBodyRot, this.yHeadRot, this.isBodyClampUneven ? this.minBodyAngle : -this.maxBodyAngle, this.maxBodyAngle);
   }

   private void rotateHeadIfNecessary() {
      this.yHeadRot = TMath.rotateIfNecessary(this.yHeadRot, this.yBodyRot, this.isHeadClampUneven ? this.minHeadAngle : -this.maxHeadAngle, this.maxHeadAngle);
   }

   private void rotateHeadTowardsFront() {
      float ratio = (float)(this.headStableTime - this.rotationDelay) / (float)this.rotationDuration;
      float clampedRatio = TMath.clamp(ratio, 0.0F, 1.0F);
      float maxClamp = this.maxHeadAngle * (1.0F - clampedRatio);
      float minClamp = (this.isHeadClampUneven ? this.minHeadAngle : -this.maxHeadAngle) * (1.0F - clampedRatio);
      this.yBodyRot = TMath.rotateIfNecessary(this.yBodyRot, this.yHeadRot, minClamp, maxClamp);
   }

   private boolean notCarryingMobPassengers() {
      Iterator var1 = this.entity.getPassengers().iterator();

      Entity entity;
      do {
         if (!var1.hasNext()) {
            return true;
         }

         entity = (Entity)var1.next();
      } while(!(entity instanceof Mob));

      return false;
   }

   public BaseEntity<?> getEntity() {
      return this.entity;
   }

   public float getXHeadRot() {
      return this.xHeadRot;
   }

   public float getYHeadRot() {
      return this.yHeadRot;
   }

   public float getYBodyRot() {
      return this.yBodyRot;
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

   public void setXHeadRot(float xHeadRot) {
      this.xHeadRot = xHeadRot;
   }

   public void setYHeadRot(float yHeadRot) {
      this.yHeadRot = yHeadRot;
   }

   public void setYBodyRot(float yBodyRot) {
      this.yBodyRot = yBodyRot;
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
