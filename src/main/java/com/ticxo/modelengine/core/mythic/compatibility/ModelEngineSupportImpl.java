package com.ticxo.modelengine.core.mythic.compatibility;

import com.google.common.collect.Maps;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.generator.ModelGenerator;
import com.ticxo.modelengine.api.generator.blueprint.BlueprintBone;
import com.ticxo.modelengine.api.generator.blueprint.ModelBlueprint;
import com.ticxo.modelengine.api.nms.entity.HitboxEntity;
import com.ticxo.modelengine.api.utils.config.ConfigProperty;
import com.ticxo.modelengine.api.utils.math.OrientedBoundingBox;
import com.ticxo.modelengine.api.utils.math.TMath;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.config.MythicConfig;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.compatibility.AbstractModelEngineSupport;
import io.lumine.mythic.bukkit.compatibility.AbstractModelEngineSupport.ModelConfig;
import io.lumine.mythic.core.logging.MythicLogger;
import io.lumine.mythic.core.mobs.model.MobModel;
import io.lumine.mythic.core.skills.projectiles.ProjectileBulletableTracker;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Entity;
import org.bukkit.util.BoundingBox;
import org.joml.Vector3f;

public class ModelEngineSupportImpl extends AbstractModelEngineSupport {
   private final Map<ProjectileBulletableTracker, ProjectileEntity<?>> trackers = Maps.newConcurrentMap();

   public ModelEngineSupportImpl() {
      super(MythicBukkit.inst());
   }

   public boolean isSubHitbox(UUID uuid) {
      return ModelEngineAPI.getInteractionTracker().getHitbox(uuid) != null;
   }

   public boolean isBoundToSubHitbox(UUID subHitbox, UUID bound) {
      HitboxEntity hitbox = ModelEngineAPI.getInteractionTracker().getHitbox(subHitbox);
      return hitbox != null && hitbox.getSubHitbox().getBoundEntities().containsKey(bound);
   }

   public UUID getParentUUID(UUID uuid) {
      HitboxEntity hitbox = ModelEngineAPI.getInteractionTracker().getHitbox(uuid);
      return hitbox == null ? uuid : hitbox.getBone().getActiveModel().getModeledEntity().getBase().getUUID();
   }

   public AbstractEntity getParent(AbstractEntity abstractEntity) {
      HitboxEntity hitbox = ModelEngineAPI.getInteractionTracker().getHitbox(abstractEntity.getUniqueId());
      if (hitbox == null) {
         return abstractEntity;
      } else {
         Object var4 = hitbox.getBone().getActiveModel().getModeledEntity().getBase().getOriginal();
         if (var4 instanceof Entity) {
            Entity entity = (Entity)var4;
            return BukkitAdapter.adapt(entity);
         } else {
            return abstractEntity;
         }
      }
   }

   public boolean overlapsOOBB(BoundingBox boundingBox, AbstractEntity abstractEntity) {
      HitboxEntity hitbox = ModelEngineAPI.getInteractionTracker().getHitbox(abstractEntity.getUniqueId());
      if (hitbox == null) {
         return false;
      } else {
         OrientedBoundingBox obb = hitbox.getOrientedBoundingBox();
         return obb != null && obb.intersects(boundingBox);
      }
   }

   public ModelConfig getBoneModel(String modelId, String boneId) throws IllegalArgumentException {
      ModelBlueprint blueprint = ModelEngineAPI.getBlueprint(modelId);
      if (blueprint == null) {
         throw new IllegalArgumentException("Unknown model " + modelId + ".");
      } else {
         BlueprintBone blueprintBone = (BlueprintBone)blueprint.getFlatMap().get(boneId);
         if (blueprintBone == null) {
            throw new IllegalArgumentException("Unknown bone " + boneId + ".");
         } else if (!blueprintBone.isRenderer()) {
            throw new IllegalArgumentException(boneId + " is not a renderer bone.");
         } else {
            return new ModelConfig(blueprintBone.getDataId(), ConfigProperty.ITEM_MODEL.getBaseItem().getMaterial(), false);
         }
      }
   }

   public MobModel createMobModel(MythicMob mythicMob, MythicConfig mythicConfig) {
      return new MEGModel(mythicMob, mythicConfig);
   }

   public void queuePostModelRegistration(Runnable runnable) {
      ModelEngineAPI.getAPI().getModelGenerator().queueTask(ModelGenerator.Phase.POST_ASSETS, runnable);
   }

   public double distanceSquaredToSubHitbox(AbstractLocation abstractLocation, AbstractEntity abstractEntity) {
      HitboxEntity hitbox = ModelEngineAPI.getInteractionTracker().getHitbox(abstractEntity.getUniqueId());
      OrientedBoundingBox obb = hitbox.getOrientedBoundingBox();
      return obb != null ? obb.distanceSquared(new Vector3f((float)abstractLocation.getX(), (float)abstractLocation.getY(), (float)abstractLocation.getZ())) : TMath.distanceSquaredToBoundingBox(abstractLocation.toLocus().toVector(), abstractEntity.getBukkitEntity().getBoundingBox());
   }

   public void load(MythicBukkit mythicBukkit) {
      MythicLogger.log("Model Engine Compatibility Loaded.");
   }

   public void unload() {
   }

   public Map<ProjectileBulletableTracker, ProjectileEntity<?>> getTrackers() {
      return this.trackers;
   }
}
