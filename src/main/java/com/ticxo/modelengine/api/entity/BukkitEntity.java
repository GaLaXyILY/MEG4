package com.ticxo.modelengine.api.entity;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.entity.data.BukkitEntityData;
import com.ticxo.modelengine.api.nms.entity.EntityHandler;
import com.ticxo.modelengine.api.nms.entity.wrapper.BodyRotationController;
import com.ticxo.modelengine.api.nms.entity.wrapper.LookController;
import com.ticxo.modelengine.api.nms.entity.wrapper.MoveController;
import com.ticxo.modelengine.api.nms.impl.DefaultBodyRotationController;
import com.ticxo.modelengine.api.nms.impl.EmptyLookController;
import com.ticxo.modelengine.api.nms.impl.EmptyMoveController;
import com.ticxo.modelengine.api.utils.data.io.SavedData;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

public class BukkitEntity implements BaseEntity<Entity> {
   protected final EntityHandler entityHandler = ModelEngineAPI.getNMSHandler().getEntityHandler();
   protected final Entity original;
   protected final BukkitEntityData data;
   protected final BodyRotationController bodyRotationController;
   protected final MoveController moveController;
   protected final LookController lookController;
   protected boolean isVisible;

   public BukkitEntity(Entity original) {
      this.original = original;
      this.data = this.createEntityData(original);
      this.bodyRotationController = this.entityHandler.wrapBodyRotationControl(original, () -> {
         return new DefaultBodyRotationController(this);
      });
      this.moveController = this.entityHandler.wrapMoveController(original, EmptyMoveController::new);
      this.lookController = this.entityHandler.wrapLookController(original, EmptyLookController::new);
      this.entityHandler.wrapNavigation(original);
   }

   protected BukkitEntityData createEntityData(Entity original) {
      return new BukkitEntityData(original);
   }

   public void setVisible(boolean flag) {
      this.isVisible = flag;
      Iterator var2;
      Player player;
      if (this.isVisible) {
         var2 = this.data.getTracking().keySet().iterator();

         while(var2.hasNext()) {
            player = (Player)var2.next();
            this.entityHandler.forceSpawn(this, player);
         }
      } else {
         var2 = this.data.getTracking().keySet().iterator();

         while(var2.hasNext()) {
            player = (Player)var2.next();
            this.entityHandler.forceDespawn(this, player);
         }
      }

   }

   public boolean isRemoved() {
      return this.entityHandler.isRemoved(this.original);
   }

   public boolean isAlive() {
      return this.data.isEntityValid();
   }

   public boolean isForcedAlive() {
      return this.data.isForcedAlive();
   }

   public void setForcedAlive(boolean flag) {
      this.data.setForcedAlive(flag);
   }

   public int getEntityId() {
      return this.original.getEntityId();
   }

   public UUID getUUID() {
      return this.original.getUniqueId();
   }

   public double getMaxStepHeight() {
      return this.entityHandler.getStepHeight(this.original);
   }

   public void setMaxStepHeight(double stepHeight) {
      this.entityHandler.setStepHeight(this.original, stepHeight);
   }

   public int getRenderRadius() {
      return this.data.getTracked().getBaseRange();
   }

   public void setRenderRadius(int radius) {
      this.data.getTracked().setBaseRange(radius);
   }

   public void setCollidableWith(Entity entity, boolean flag) {
      Entity var4 = this.original;
      if (var4 instanceof LivingEntity) {
         LivingEntity livingEntity = (LivingEntity)var4;
         Set set = livingEntity.getCollidableExemptions();
         if (flag) {
            set.remove(entity.getUniqueId());
         } else {
            set.add(entity.getUniqueId());
         }

      }
   }

   public boolean isGlowing() {
      return this.original.isGlowing();
   }

   public int getGlowColor() {
      return ModelEngineAPI.getEntityHandler().getGlowColor(this.original);
   }

   public boolean hurt(@Nullable HumanEntity player, Object nmsDamageCause, float damage) {
      return this.entityHandler.hurt(this.original, nmsDamageCause, damage);
   }

   public EntityHandler.InteractionResult interact(HumanEntity player, EquipmentSlot slot) {
      if (player instanceof Player) {
         Player serverPlayer = (Player)player;
         PlayerInteractAtEntityEvent event = new PlayerInteractAtEntityEvent(serverPlayer, this.original, new Vector(0, 0, 0), slot);
         Bukkit.getPluginManager().callEvent(event);
         if (event.isCancelled()) {
            return EntityHandler.InteractionResult.FAIL;
         }
      }

      return this.entityHandler.interact(this.original, player, slot);
   }

   public float getYRot() {
      return this.entityHandler.getYRot(this.original);
   }

   public float getYHeadRot() {
      return this.entityHandler.getYHeadRot(this.original);
   }

   public float getXHeadRot() {
      return this.entityHandler.getXHeadRot(this.original);
   }

   public float getYBodyRot() {
      return this.entityHandler.getYBodyRot(this.original);
   }

   public boolean isWalking() {
      return this.entityHandler.isWalking(this.original);
   }

   public boolean isStrafing() {
      return this.entityHandler.isStrafing(this.original);
   }

   public boolean isJumping() {
      return this.entityHandler.isJumping(this.original);
   }

   public boolean isFlying() {
      return this.entityHandler.isFlying(this.original);
   }

   public void save(SavedData data) {
      BaseEntity.super.save(data);
      Entity var3 = this.original;
      if (var3 instanceof LivingEntity) {
         LivingEntity livingEntity = (LivingEntity)var3;
         data.putList("collide_exemption", livingEntity.getCollidableExemptions());
      }

   }

   public void load(SavedData data) {
      BaseEntity.super.load(data);
      Entity var3 = this.original;
      if (var3 instanceof LivingEntity) {
         LivingEntity livingEntity = (LivingEntity)var3;
         livingEntity.getCollidableExemptions().addAll(data.getList("collide_exemption"));
      }

   }

   public Entity getOriginal() {
      return this.original;
   }

   public BukkitEntityData getData() {
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

   public boolean isVisible() {
      return this.isVisible;
   }
}
