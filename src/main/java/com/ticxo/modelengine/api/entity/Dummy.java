package com.ticxo.modelengine.api.entity;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.entity.data.DummyEntityData;
import com.ticxo.modelengine.api.nms.entity.EntityHandler;
import com.ticxo.modelengine.api.nms.entity.wrapper.BodyRotationController;
import com.ticxo.modelengine.api.nms.entity.wrapper.LookController;
import com.ticxo.modelengine.api.nms.entity.wrapper.MoveController;
import com.ticxo.modelengine.api.nms.impl.DefaultBodyRotationController;
import com.ticxo.modelengine.api.nms.impl.EmptyLookController;
import com.ticxo.modelengine.api.nms.impl.EmptyMoveController;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.Nullable;

public class Dummy<T> implements BaseEntity<T> {
   protected final int entityId;
   protected final UUID uuid;
   protected final T original;
   protected final DummyEntityData<T> data;
   protected final BodyRotationController bodyRotationController;
   protected final MoveController moveController;
   protected final LookController lookController;
   protected boolean detectingPlayers;
   protected boolean isRemoved;
   protected boolean isWalking;
   protected boolean isStrafing;
   protected boolean isJumping;
   protected boolean isFlying;
   protected boolean isGlowing;
   protected int glowColor;
   protected float yHeadRot;
   protected float xHeadRot;
   protected float yBodyRot;

   public Dummy() {
      this((Object)null);
   }

   public Dummy(T original) {
      this(UUID.randomUUID(), original);
   }

   public Dummy(UUID uuid, T original) {
      this(ModelEngineAPI.getEntityHandler().getNextEntityId(), uuid, original);
   }

   public Dummy(int id, UUID uuid, T original) {
      this.detectingPlayers = true;
      this.entityId = id;
      this.uuid = uuid;
      this.original = original;
      this.data = new DummyEntityData(this);
      this.bodyRotationController = new DefaultBodyRotationController(this);
      this.moveController = new EmptyMoveController();
      this.lookController = new EmptyLookController();
   }

   public boolean isAlive() {
      return !this.isRemoved;
   }

   public UUID getUUID() {
      return this.uuid;
   }

   public int getRenderRadius() {
      return this.data.getRenderRadius();
   }

   public void setRenderRadius(int radius) {
      this.data.setRenderRadius(radius);
   }

   public float getYRot() {
      return this.yHeadRot;
   }

   public void setLocation(Location location) {
      this.data.setLocation(location);
   }

   public void syncLocation(Location location) {
      this.data.setLocation(location);
      this.setYBodyRot(location.getYaw());
      this.setYHeadRot(location.getYaw());
      this.setXHeadRot(location.getPitch());
   }

   public void setForceViewing(Player player, boolean flag) {
      if (flag) {
         this.setForceHidden(player, false);
         this.data.getTracked().addForcedPairing(player);
      } else {
         this.data.getTracked().removeForcedPairing(player);
      }

   }

   public void setForceHidden(Player player, boolean flag) {
      if (flag) {
         this.setForceViewing(player, false);
         this.data.getTracked().addForcedHidden(player);
      } else {
         this.data.getTracked().removeForcedHidden(player);
      }

   }

   public boolean isVisible() {
      return true;
   }

   public void setVisible(boolean flag) {
   }

   public boolean isForcedAlive() {
      return false;
   }

   public void setForcedAlive(boolean flag) {
   }

   public double getMaxStepHeight() {
      return 0.0D;
   }

   public void setMaxStepHeight(double stepHeight) {
   }

   public void setCollidableWith(Entity entity, boolean flag) {
   }

   public boolean hurt(@Nullable HumanEntity player, Object nmsDamageCause, float damage) {
      return false;
   }

   public EntityHandler.InteractionResult interact(HumanEntity player, EquipmentSlot slot) {
      return null;
   }

   public int getEntityId() {
      return this.entityId;
   }

   public T getOriginal() {
      return this.original;
   }

   public DummyEntityData<T> getData() {
      return this.data;
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

   public boolean isDetectingPlayers() {
      return this.detectingPlayers;
   }

   public boolean isRemoved() {
      return this.isRemoved;
   }

   public boolean isWalking() {
      return this.isWalking;
   }

   public boolean isStrafing() {
      return this.isStrafing;
   }

   public boolean isJumping() {
      return this.isJumping;
   }

   public boolean isFlying() {
      return this.isFlying;
   }

   public boolean isGlowing() {
      return this.isGlowing;
   }

   public int getGlowColor() {
      return this.glowColor;
   }

   public float getYHeadRot() {
      return this.yHeadRot;
   }

   public float getXHeadRot() {
      return this.xHeadRot;
   }

   public float getYBodyRot() {
      return this.yBodyRot;
   }

   public void setDetectingPlayers(boolean detectingPlayers) {
      this.detectingPlayers = detectingPlayers;
   }

   public void setRemoved(boolean isRemoved) {
      this.isRemoved = isRemoved;
   }

   public void setWalking(boolean isWalking) {
      this.isWalking = isWalking;
   }

   public void setStrafing(boolean isStrafing) {
      this.isStrafing = isStrafing;
   }

   public void setJumping(boolean isJumping) {
      this.isJumping = isJumping;
   }

   public void setFlying(boolean isFlying) {
      this.isFlying = isFlying;
   }

   public void setGlowing(boolean isGlowing) {
      this.isGlowing = isGlowing;
   }

   public void setGlowColor(int glowColor) {
      this.glowColor = glowColor;
   }

   public void setYHeadRot(float yHeadRot) {
      this.yHeadRot = yHeadRot;
   }

   public void setXHeadRot(float xHeadRot) {
      this.xHeadRot = xHeadRot;
   }

   public void setYBodyRot(float yBodyRot) {
      this.yBodyRot = yBodyRot;
   }
}
