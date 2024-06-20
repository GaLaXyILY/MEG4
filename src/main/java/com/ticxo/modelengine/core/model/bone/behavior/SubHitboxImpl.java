package com.ticxo.modelengine.core.model.bone.behavior;

import com.google.common.collect.Maps;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.entity.Hitbox;
import com.ticxo.modelengine.api.model.bone.ModelBone;
import com.ticxo.modelengine.api.model.bone.behavior.AbstractBoneBehavior;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehaviorData;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehaviorType;
import com.ticxo.modelengine.api.model.bone.type.SubHitbox;
import com.ticxo.modelengine.api.nms.entity.HitboxEntity;
import com.ticxo.modelengine.api.utils.config.DebugToggle;
import com.ticxo.modelengine.api.utils.math.OrientedBoundingBox;
import com.ticxo.modelengine.api.utils.ticker.DualTicker;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class SubHitboxImpl extends AbstractBoneBehavior<SubHitboxImpl> implements SubHitbox {
   private final boolean isOBB;
   private final Vector3f fixedDimension = new Vector3f();
   private final Vector3f origin = new Vector3f();
   private final int hitboxId;
   private final Vector3f dimension = new Vector3f();
   private final Vector3f location = new Vector3f();
   private final Quaternionf rotation = new Quaternionf();
   private final Map<UUID, Entity> boundEntities = Maps.newConcurrentMap();
   private float yaw;
   private HitboxEntity hitboxEntity;
   private float damageMultiplier = 1.0F;

   public SubHitboxImpl(ModelBone bone, BoneBehaviorType<SubHitboxImpl> type, BoneBehaviorData data) {
      super(bone, type, data);
      Hitbox hitbox = (Hitbox)data.get("dimension");
      if (hitbox == null) {
         throw new RuntimeException("Unable to retrieve sub-hitbox dimension of bone " + bone.getUniqueBoneId());
      } else {
         this.hitboxId = ModelEngineAPI.getEntityHandler().getNextEntityId();
         this.isOBB = (Boolean)data.get("obb", false);
         Vector3f bonePosition = bone.getBlueprintBone().getGlobalPosition();
         this.origin.set((Vector3fc)data.get("origin", bonePosition)).sub(bonePosition);
         if (this.isOBB) {
            this.fixedDimension.set(hitbox.getWidth(), hitbox.getHeight(), hitbox.getDepth());
         } else {
            this.fixedDimension.set(hitbox.getMaxWidth(), hitbox.getHeight(), hitbox.getMaxWidth());
         }

      }
   }

   public void onApply() {
      Location location = this.bone.getActiveModel().getModeledEntity().getBase().getLocation();
      this.removeOld();
      this.hitboxEntity = ModelEngineAPI.getEntityHandler().createHitbox(location, this.bone, this);
      ModelEngineAPI.getInteractionTracker().setEntityRelay(this.hitboxId, this.hitboxEntity.getEntityId());
   }

   public void onRemove() {
      ModelEngineAPI.getInteractionTracker().removeEntityRelay(this.hitboxId);
      this.removeOld();
      DualTicker.queueSyncTask(() -> {
         this.boundEntities.forEach((uuid, entity) -> {
            entity.remove();
         });
      });
   }

   public void postGlobalCalculation() {
      Vector3f position = new Vector3f(this.origin);
      Vector3f parentPosition = this.bone.getGlobalPosition();
      Quaternionf parentRotation = this.bone.getGlobalLeftRotation();
      Vector3f parentScale = this.bone.getGlobalScale();
      parentPosition.add(position.mul(parentScale).rotate(parentRotation), this.location);
      this.rotation.set(parentRotation);
      parentScale.mul(this.fixedDimension, this.dimension);
   }

   public void onFinalize() {
      this.dimension.mul((float)this.bone.getBlueprintBone().getScale()).mul(this.bone.getActiveModel().getScale());
      this.location.mul(this.bone.getActiveModel().getScale());
      Location baseLocation = this.bone.getActiveModel().getModeledEntity().getBase().getLocation();
      this.yaw = 180.0F - this.bone.getYaw();
      this.location.rotateY(this.yaw * 0.017453292F).add((float)baseLocation.getX(), (float)baseLocation.getY(), (float)baseLocation.getZ());
      if (!this.isOBB) {
         this.location.sub(0.0F, this.dimension.y * 0.5F, 0.0F);
      }

      if (this.hitboxEntity != null) {
         this.hitboxEntity.queueLocation(this.location);
         if (DebugToggle.isDebugging(DebugToggle.SHOW_OBB) && this.isOBB) {
            OrientedBoundingBox obb = this.hitboxEntity.getOrientedBoundingBox();
            if (obb != null) {
               obb.visualize(baseLocation.getWorld());
            }
         }

      }
   }

   public void addBoundEntity(Entity entity) {
      this.getBoundEntities().put(entity.getUniqueId(), entity);
      ModelEngineAPI.getEntityHandler().forceDespawn(entity);
      ModelEngineAPI.setRenderCanceled(entity.getEntityId(), true);
   }

   public void removeBoundEntity(Entity entity) {
      this.getBoundEntities().remove(entity.getUniqueId(), entity);
      ModelEngineAPI.setRenderCanceled(entity.getEntityId(), false);
      ModelEngineAPI.getEntityHandler().forceSpawn(entity);
   }

   private void removeOld() {
      if (this.hitboxEntity != null) {
         this.hitboxEntity.markRemoved();
      }

   }

   public boolean isOBB() {
      return this.isOBB;
   }

   public Vector3f getFixedDimension() {
      return this.fixedDimension;
   }

   public Vector3f getOrigin() {
      return this.origin;
   }

   public int getHitboxId() {
      return this.hitboxId;
   }

   public Vector3f getDimension() {
      return this.dimension;
   }

   public Vector3f getLocation() {
      return this.location;
   }

   public Quaternionf getRotation() {
      return this.rotation;
   }

   public Map<UUID, Entity> getBoundEntities() {
      return this.boundEntities;
   }

   public float getYaw() {
      return this.yaw;
   }

   public HitboxEntity getHitboxEntity() {
      return this.hitboxEntity;
   }

   public float getDamageMultiplier() {
      return this.damageMultiplier;
   }

   public void setDamageMultiplier(float damageMultiplier) {
      this.damageMultiplier = damageMultiplier;
   }
}
