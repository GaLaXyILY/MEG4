package com.ticxo.modelengine.core.mythic.compatibility;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.entity.BaseEntity;
import com.ticxo.modelengine.api.nms.entity.EntityHandler;
import com.ticxo.modelengine.api.nms.entity.wrapper.BodyRotationController;
import com.ticxo.modelengine.api.nms.entity.wrapper.LookController;
import com.ticxo.modelengine.api.nms.entity.wrapper.MoveController;
import com.ticxo.modelengine.api.nms.impl.EmptyBodyRotationController;
import com.ticxo.modelengine.api.nms.impl.EmptyLookController;
import com.ticxo.modelengine.api.nms.impl.EmptyMoveController;
import com.ticxo.modelengine.core.ModelEngine;
import io.lumine.mythic.api.adapters.AbstractVector;
import io.lumine.mythic.api.skills.IParentSkill;
import io.lumine.mythic.core.skills.projectiles.ProjectileBulletableTracker;
import java.util.UUID;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.Nullable;

public class ProjectileEntity<T extends ProjectileBulletableTracker & IParentSkill> implements BaseEntity<T> {
   private final T original;
   private final ProjectileData data;
   private final int entityId;
   private final UUID uuid;
   private final BodyRotationController bodyRotationController = new EmptyBodyRotationController();
   private final MoveController moveController = new EmptyMoveController();
   private final LookController lookController = new EmptyLookController();
   private int renderRadius = 32;

   public ProjectileEntity(T original) {
      this.original = original;
      this.entityId = ModelEngineAPI.getEntityHandler().getNextEntityId();
      this.uuid = UUID.randomUUID();
      this.data = new ProjectileData(this);
      ModelEngine.CORE.getMythicCompatibility().getMythicSupport().getTrackers().put(original, this);
   }

   public boolean isVisible() {
      return true;
   }

   public void setVisible(boolean flag) {
   }

   public boolean isRemoved() {
      return !this.data.isDataValid();
   }

   public boolean isAlive() {
      return this.data.isDataValid();
   }

   public boolean isForcedAlive() {
      return false;
   }

   public void setForcedAlive(boolean flag) {
   }

   public UUID getUUID() {
      return this.uuid;
   }

   public double getMaxStepHeight() {
      return 0.0D;
   }

   public void setMaxStepHeight(double stepHeight) {
   }

   public void setCollidableWith(Entity entity, boolean flag) {
   }

   public boolean isGlowing() {
      return false;
   }

   public int getGlowColor() {
      return -1;
   }

   public boolean hurt(@Nullable HumanEntity player, Object nmsDamageCause, float damage) {
      return false;
   }

   public EntityHandler.InteractionResult interact(HumanEntity player, EquipmentSlot slot) {
      return null;
   }

   public float getYRot() {
      AbstractVector vel = this.original.getCurrentVelocity();
      return vel == null ? 0.0F : (float)Math.toDegrees(Math.atan2(-vel.getX(), vel.getZ()));
   }

   public float getYHeadRot() {
      return this.getYRot();
   }

   public float getXHeadRot() {
      AbstractVector vel = this.original.getCurrentVelocity();
      if (vel == null) {
         return 0.0F;
      } else {
         double hMag = Math.sqrt(vel.getX() * vel.getX() + vel.getZ() * vel.getZ());
         return (float)Math.toDegrees(-Math.atan2(vel.getY(), hMag));
      }
   }

   public float getYBodyRot() {
      return this.getYRot();
   }

   public boolean isWalking() {
      return false;
   }

   public boolean isStrafing() {
      return false;
   }

   public boolean isJumping() {
      return false;
   }

   public boolean isFlying() {
      return false;
   }

   public void removeSelf() {
      ModelEngine.CORE.getMythicCompatibility().getMythicSupport().getTrackers().remove(this.original, this);
   }

   public T getOriginal() {
      return this.original;
   }

   public ProjectileData getData() {
      return this.data;
   }

   public int getEntityId() {
      return this.entityId;
   }

   public BodyRotationController getBodyRotationController() {
      return this.bodyRotationController;
   }

   public MoveController getMoveController() {
      return this.moveController;
   }

   public LookController getLookController() {
      return this.lookController;
   }

   public int getRenderRadius() {
      return this.renderRadius;
   }

   public void setRenderRadius(int renderRadius) {
      this.renderRadius = renderRadius;
   }
}
