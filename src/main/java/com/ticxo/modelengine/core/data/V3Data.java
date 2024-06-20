/* Decompiler 2734ms, total 3262ms, lines 747 */
package com.ticxo.modelengine.core.data;

import com.google.common.collect.Maps;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.animation.BlueprintAnimation;
import com.ticxo.modelengine.api.animation.ModelState;
import com.ticxo.modelengine.api.animation.BlueprintAnimation.LoopMode;
import com.ticxo.modelengine.api.animation.handler.AnimationHandler.DefaultProperty;
import com.ticxo.modelengine.api.animation.property.SimpleProperty;
import com.ticxo.modelengine.api.animation.property.IAnimationProperty.Phase;
import com.ticxo.modelengine.api.entity.Dummy;
import com.ticxo.modelengine.api.entity.Hitbox;
import com.ticxo.modelengine.api.generator.blueprint.ModelBlueprint;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.bone.BoneBehaviorTypes;
import com.ticxo.modelengine.api.model.bone.behavior.GlobalBehaviorData;
import com.ticxo.modelengine.api.model.bone.manager.BehaviorManager;
import com.ticxo.modelengine.api.model.bone.manager.MountData;
import com.ticxo.modelengine.api.model.bone.manager.MountManager;
import com.ticxo.modelengine.api.nms.entity.wrapper.BodyRotationController;
import com.ticxo.modelengine.api.utils.data.io.SavedData;
import com.ticxo.modelengine.core.model.ModeledEntityImpl;
import java.util.Map;
import java.util.function.Consumer;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

public class V3Data {
   private int renderDistance;
   private boolean isHeadClampUneven;
   private boolean isBodyClampUneven;
   private float maxHeadAngle;
   private float maxBodyAngle;
   private float minHeadAngle;
   private float minBodyAngle;
   private boolean playerMode;
   private float stableAngle;
   private int rotationDelay;
   private int rotationDuration;
   private boolean canSteer;
   private boolean canRide;
   private String mountModelId;
   private String mountDriverBone;
   private boolean isBaseEntityVisible;
   private boolean modelRotationLock;
   private Double stepHeight;
   private Hitbox hitbox;
   private final Map<String, V3Data.ModelData> models = Maps.newConcurrentMap();

   public SavedData convert(@Nullable Location location) {
      Dummy<Object> dummy = new Dummy();
      dummy.setRenderRadius(this.renderDistance);
      dummy.setLocation(location);
      if (this.stepHeight != null) {
         dummy.setMaxStepHeight(this.stepHeight);
      }

      BodyRotationController bodyController = dummy.getBodyRotationController();
      bodyController.setHeadClampUneven(this.isHeadClampUneven);
      bodyController.setBodyClampUneven(this.isBodyClampUneven);
      bodyController.setMaxHeadAngle(this.maxHeadAngle);
      bodyController.setMaxBodyAngle(this.maxBodyAngle);
      bodyController.setMinHeadAngle(this.minHeadAngle);
      bodyController.setMinBodyAngle(this.minBodyAngle);
      bodyController.setPlayerMode(this.playerMode);
      bodyController.setStableAngle(this.stableAngle);
      bodyController.setRotationDelay(this.rotationDelay);
      bodyController.setRotationDuration(this.rotationDuration);
      <undefinedtype> modeledEntity = new ModeledEntityImpl(dummy, (Consumer)null) {
         public void registerSelf() {
         }
      };
      modeledEntity.setBaseEntityVisible(this.isBaseEntityVisible);
      modeledEntity.setModelRotationLocked(this.modelRotationLock);
      this.models.forEach((modelId, modelData) -> {
         ActiveModel activeModel = ModelEngineAPI.createActiveModel(modelId);
         if (activeModel != null) {
            activeModel.setCanHurt(modelData.canHurt);
            activeModel.setLockPitch(modelData.lockPitch);
            activeModel.setLockYaw(modelData.lockYaw);
            modelData.defaultStates.forEach((modelState, property) -> {
               activeModel.getAnimationHandler().setDefaultProperty(new DefaultProperty(modelState, property.stateId, property.lerpIn, property.lerpOut, property.speed));
            });
            ModelBlueprint blueprint = activeModel.getBlueprint();
            modelData.states.forEach((state, stateData) -> {
               BlueprintAnimation animation = (BlueprintAnimation)blueprint.getAnimations().get(state);
               if (animation != null) {
                  SimpleProperty property = new SimpleProperty(activeModel, animation, stateData.lerpIn, stateData.lerpOut, stateData.speed);
                  property.setPhase(stateData.phase);
                  property.setForceLoopMode(stateData.forceLoopMode);
                  property.setForceOverride(stateData.forceOverride);
                  activeModel.getAnimationHandler().playAnimation(property, true);
               }
            });
            modeledEntity.addModel(activeModel, false).ifPresent(ActiveModel::destroy);
         }
      });
      GlobalBehaviorData globalData = modeledEntity.getGlobalBehaviorData(BoneBehaviorTypes.MOUNT);
      if (globalData instanceof MountData) {
         MountData mountData = (MountData)globalData;
         BehaviorManager manager = mountData.getMainMountManager();
         if (manager != null) {
            ((MountManager)manager).setCanDrive(this.canSteer);
            ((MountManager)manager).setCanRide(this.canRide);
         }
      }

      SavedData data = (SavedData)modeledEntity.save().orElse((Object)null);
      modeledEntity.destroy();
      return data;
   }

   public int getRenderDistance() {
      return this.renderDistance;
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

   public boolean isCanSteer() {
      return this.canSteer;
   }

   public boolean isCanRide() {
      return this.canRide;
   }

   public String getMountModelId() {
      return this.mountModelId;
   }

   public String getMountDriverBone() {
      return this.mountDriverBone;
   }

   public boolean isBaseEntityVisible() {
      return this.isBaseEntityVisible;
   }

   public boolean isModelRotationLock() {
      return this.modelRotationLock;
   }

   public Double getStepHeight() {
      return this.stepHeight;
   }

   public Hitbox getHitbox() {
      return this.hitbox;
   }

   public Map<String, V3Data.ModelData> getModels() {
      return this.models;
   }

   public void setRenderDistance(int renderDistance) {
      this.renderDistance = renderDistance;
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

   public void setCanSteer(boolean canSteer) {
      this.canSteer = canSteer;
   }

   public void setCanRide(boolean canRide) {
      this.canRide = canRide;
   }

   public void setMountModelId(String mountModelId) {
      this.mountModelId = mountModelId;
   }

   public void setMountDriverBone(String mountDriverBone) {
      this.mountDriverBone = mountDriverBone;
   }

   public void setBaseEntityVisible(boolean isBaseEntityVisible) {
      this.isBaseEntityVisible = isBaseEntityVisible;
   }

   public void setModelRotationLock(boolean modelRotationLock) {
      this.modelRotationLock = modelRotationLock;
   }

   public void setStepHeight(Double stepHeight) {
      this.stepHeight = stepHeight;
   }

   public void setHitbox(Hitbox hitbox) {
      this.hitbox = hitbox;
   }

   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof V3Data)) {
         return false;
      } else {
         V3Data other = (V3Data)o;
         if (!other.canEqual(this)) {
            return false;
         } else if (this.getRenderDistance() != other.getRenderDistance()) {
            return false;
         } else if (this.isHeadClampUneven() != other.isHeadClampUneven()) {
            return false;
         } else if (this.isBodyClampUneven() != other.isBodyClampUneven()) {
            return false;
         } else if (Float.compare(this.getMaxHeadAngle(), other.getMaxHeadAngle()) != 0) {
            return false;
         } else if (Float.compare(this.getMaxBodyAngle(), other.getMaxBodyAngle()) != 0) {
            return false;
         } else if (Float.compare(this.getMinHeadAngle(), other.getMinHeadAngle()) != 0) {
            return false;
         } else if (Float.compare(this.getMinBodyAngle(), other.getMinBodyAngle()) != 0) {
            return false;
         } else if (this.isPlayerMode() != other.isPlayerMode()) {
            return false;
         } else if (Float.compare(this.getStableAngle(), other.getStableAngle()) != 0) {
            return false;
         } else if (this.getRotationDelay() != other.getRotationDelay()) {
            return false;
         } else if (this.getRotationDuration() != other.getRotationDuration()) {
            return false;
         } else if (this.isCanSteer() != other.isCanSteer()) {
            return false;
         } else if (this.isCanRide() != other.isCanRide()) {
            return false;
         } else if (this.isBaseEntityVisible() != other.isBaseEntityVisible()) {
            return false;
         } else if (this.isModelRotationLock() != other.isModelRotationLock()) {
            return false;
         } else {
            label108: {
               Object this$stepHeight = this.getStepHeight();
               Object other$stepHeight = other.getStepHeight();
               if (this$stepHeight == null) {
                  if (other$stepHeight == null) {
                     break label108;
                  }
               } else if (this$stepHeight.equals(other$stepHeight)) {
                  break label108;
               }

               return false;
            }

            Object this$mountModelId = this.getMountModelId();
            Object other$mountModelId = other.getMountModelId();
            if (this$mountModelId == null) {
               if (other$mountModelId != null) {
                  return false;
               }
            } else if (!this$mountModelId.equals(other$mountModelId)) {
               return false;
            }

            Object this$mountDriverBone = this.getMountDriverBone();
            Object other$mountDriverBone = other.getMountDriverBone();
            if (this$mountDriverBone == null) {
               if (other$mountDriverBone != null) {
                  return false;
               }
            } else if (!this$mountDriverBone.equals(other$mountDriverBone)) {
               return false;
            }

            label87: {
               Object this$hitbox = this.getHitbox();
               Object other$hitbox = other.getHitbox();
               if (this$hitbox == null) {
                  if (other$hitbox == null) {
                     break label87;
                  }
               } else if (this$hitbox.equals(other$hitbox)) {
                  break label87;
               }

               return false;
            }

            Object this$models = this.getModels();
            Object other$models = other.getModels();
            if (this$models == null) {
               if (other$models != null) {
                  return false;
               }
            } else if (!this$models.equals(other$models)) {
               return false;
            }

            return true;
         }
      }
   }

   protected boolean canEqual(Object other) {
      return other instanceof V3Data;
   }

   public int hashCode() {
      int PRIME = true;
      int result = 1;
      int result = result * 59 + this.getRenderDistance();
      result = result * 59 + (this.isHeadClampUneven() ? 79 : 97);
      result = result * 59 + (this.isBodyClampUneven() ? 79 : 97);
      result = result * 59 + Float.floatToIntBits(this.getMaxHeadAngle());
      result = result * 59 + Float.floatToIntBits(this.getMaxBodyAngle());
      result = result * 59 + Float.floatToIntBits(this.getMinHeadAngle());
      result = result * 59 + Float.floatToIntBits(this.getMinBodyAngle());
      result = result * 59 + (this.isPlayerMode() ? 79 : 97);
      result = result * 59 + Float.floatToIntBits(this.getStableAngle());
      result = result * 59 + this.getRotationDelay();
      result = result * 59 + this.getRotationDuration();
      result = result * 59 + (this.isCanSteer() ? 79 : 97);
      result = result * 59 + (this.isCanRide() ? 79 : 97);
      result = result * 59 + (this.isBaseEntityVisible() ? 79 : 97);
      result = result * 59 + (this.isModelRotationLock() ? 79 : 97);
      Object $stepHeight = this.getStepHeight();
      result = result * 59 + ($stepHeight == null ? 43 : $stepHeight.hashCode());
      Object $mountModelId = this.getMountModelId();
      result = result * 59 + ($mountModelId == null ? 43 : $mountModelId.hashCode());
      Object $mountDriverBone = this.getMountDriverBone();
      result = result * 59 + ($mountDriverBone == null ? 43 : $mountDriverBone.hashCode());
      Object $hitbox = this.getHitbox();
      result = result * 59 + ($hitbox == null ? 43 : $hitbox.hashCode());
      Object $models = this.getModels();
      result = result * 59 + ($models == null ? 43 : $models.hashCode());
      return result;
   }

   public String toString() {
      int var10000 = this.getRenderDistance();
      return "V3Data(renderDistance=" + var10000 + ", isHeadClampUneven=" + this.isHeadClampUneven() + ", isBodyClampUneven=" + this.isBodyClampUneven() + ", maxHeadAngle=" + this.getMaxHeadAngle() + ", maxBodyAngle=" + this.getMaxBodyAngle() + ", minHeadAngle=" + this.getMinHeadAngle() + ", minBodyAngle=" + this.getMinBodyAngle() + ", playerMode=" + this.isPlayerMode() + ", stableAngle=" + this.getStableAngle() + ", rotationDelay=" + this.getRotationDelay() + ", rotationDuration=" + this.getRotationDuration() + ", canSteer=" + this.isCanSteer() + ", canRide=" + this.isCanRide() + ", mountModelId=" + this.getMountModelId() + ", mountDriverBone=" + this.getMountDriverBone() + ", isBaseEntityVisible=" + this.isBaseEntityVisible() + ", modelRotationLock=" + this.isModelRotationLock() + ", stepHeight=" + this.getStepHeight() + ", hitbox=" + this.getHitbox() + ", models=" + this.getModels() + ")";
   }

   public static class ModelData {
      private final Map<String, V3Data.StateData> states = Maps.newConcurrentMap();
      private final Map<ModelState, V3Data.StateProperty> defaultStates = Maps.newConcurrentMap();
      private boolean canHurt;
      private boolean lockPitch;
      private boolean lockYaw;

      public Map<String, V3Data.StateData> getStates() {
         return this.states;
      }

      public Map<ModelState, V3Data.StateProperty> getDefaultStates() {
         return this.defaultStates;
      }

      public boolean isCanHurt() {
         return this.canHurt;
      }

      public boolean isLockPitch() {
         return this.lockPitch;
      }

      public boolean isLockYaw() {
         return this.lockYaw;
      }

      public void setCanHurt(boolean canHurt) {
         this.canHurt = canHurt;
      }

      public void setLockPitch(boolean lockPitch) {
         this.lockPitch = lockPitch;
      }

      public void setLockYaw(boolean lockYaw) {
         this.lockYaw = lockYaw;
      }

      public boolean equals(Object o) {
         if (o == this) {
            return true;
         } else if (!(o instanceof V3Data.ModelData)) {
            return false;
         } else {
            V3Data.ModelData other = (V3Data.ModelData)o;
            if (!other.canEqual(this)) {
               return false;
            } else if (this.isCanHurt() != other.isCanHurt()) {
               return false;
            } else if (this.isLockPitch() != other.isLockPitch()) {
               return false;
            } else if (this.isLockYaw() != other.isLockYaw()) {
               return false;
            } else {
               Object this$states = this.getStates();
               Object other$states = other.getStates();
               if (this$states == null) {
                  if (other$states != null) {
                     return false;
                  }
               } else if (!this$states.equals(other$states)) {
                  return false;
               }

               Object this$defaultStates = this.getDefaultStates();
               Object other$defaultStates = other.getDefaultStates();
               if (this$defaultStates == null) {
                  if (other$defaultStates != null) {
                     return false;
                  }
               } else if (!this$defaultStates.equals(other$defaultStates)) {
                  return false;
               }

               return true;
            }
         }
      }

      protected boolean canEqual(Object other) {
         return other instanceof V3Data.ModelData;
      }

      public int hashCode() {
         int PRIME = true;
         int result = 1;
         int result = result * 59 + (this.isCanHurt() ? 79 : 97);
         result = result * 59 + (this.isLockPitch() ? 79 : 97);
         result = result * 59 + (this.isLockYaw() ? 79 : 97);
         Object $states = this.getStates();
         result = result * 59 + ($states == null ? 43 : $states.hashCode());
         Object $defaultStates = this.getDefaultStates();
         result = result * 59 + ($defaultStates == null ? 43 : $defaultStates.hashCode());
         return result;
      }

      public String toString() {
         Map var10000 = this.getStates();
         return "V3Data.ModelData(states=" + var10000 + ", defaultStates=" + this.getDefaultStates() + ", canHurt=" + this.isCanHurt() + ", lockPitch=" + this.isLockPitch() + ", lockYaw=" + this.isLockYaw() + ")";
      }
   }

   public static class StateData {
      private double lerpIn;
      private double lerpOut;
      private double time;
      private double speed;
      private Phase phase;
      private LoopMode forceLoopMode;
      private boolean forceOverride;

      public double getLerpIn() {
         return this.lerpIn;
      }

      public double getLerpOut() {
         return this.lerpOut;
      }

      public double getTime() {
         return this.time;
      }

      public double getSpeed() {
         return this.speed;
      }

      public Phase getPhase() {
         return this.phase;
      }

      public LoopMode getForceLoopMode() {
         return this.forceLoopMode;
      }

      public boolean isForceOverride() {
         return this.forceOverride;
      }

      public void setLerpIn(double lerpIn) {
         this.lerpIn = lerpIn;
      }

      public void setLerpOut(double lerpOut) {
         this.lerpOut = lerpOut;
      }

      public void setTime(double time) {
         this.time = time;
      }

      public void setSpeed(double speed) {
         this.speed = speed;
      }

      public void setPhase(Phase phase) {
         this.phase = phase;
      }

      public void setForceLoopMode(LoopMode forceLoopMode) {
         this.forceLoopMode = forceLoopMode;
      }

      public void setForceOverride(boolean forceOverride) {
         this.forceOverride = forceOverride;
      }

      public boolean equals(Object o) {
         if (o == this) {
            return true;
         } else if (!(o instanceof V3Data.StateData)) {
            return false;
         } else {
            V3Data.StateData other = (V3Data.StateData)o;
            if (!other.canEqual(this)) {
               return false;
            } else if (Double.compare(this.getLerpIn(), other.getLerpIn()) != 0) {
               return false;
            } else if (Double.compare(this.getLerpOut(), other.getLerpOut()) != 0) {
               return false;
            } else if (Double.compare(this.getTime(), other.getTime()) != 0) {
               return false;
            } else if (Double.compare(this.getSpeed(), other.getSpeed()) != 0) {
               return false;
            } else if (this.isForceOverride() != other.isForceOverride()) {
               return false;
            } else {
               Object this$phase = this.getPhase();
               Object other$phase = other.getPhase();
               if (this$phase == null) {
                  if (other$phase != null) {
                     return false;
                  }
               } else if (!this$phase.equals(other$phase)) {
                  return false;
               }

               Object this$forceLoopMode = this.getForceLoopMode();
               Object other$forceLoopMode = other.getForceLoopMode();
               if (this$forceLoopMode == null) {
                  if (other$forceLoopMode != null) {
                     return false;
                  }
               } else if (!this$forceLoopMode.equals(other$forceLoopMode)) {
                  return false;
               }

               return true;
            }
         }
      }

      protected boolean canEqual(Object other) {
         return other instanceof V3Data.StateData;
      }

      public int hashCode() {
         int PRIME = true;
         int result = 1;
         long $lerpIn = Double.doubleToLongBits(this.getLerpIn());
         int result = result * 59 + (int)($lerpIn >>> 32 ^ $lerpIn);
         long $lerpOut = Double.doubleToLongBits(this.getLerpOut());
         result = result * 59 + (int)($lerpOut >>> 32 ^ $lerpOut);
         long $time = Double.doubleToLongBits(this.getTime());
         result = result * 59 + (int)($time >>> 32 ^ $time);
         long $speed = Double.doubleToLongBits(this.getSpeed());
         result = result * 59 + (int)($speed >>> 32 ^ $speed);
         result = result * 59 + (this.isForceOverride() ? 79 : 97);
         Object $phase = this.getPhase();
         result = result * 59 + ($phase == null ? 43 : $phase.hashCode());
         Object $forceLoopMode = this.getForceLoopMode();
         result = result * 59 + ($forceLoopMode == null ? 43 : $forceLoopMode.hashCode());
         return result;
      }

      public String toString() {
         double var10000 = this.getLerpIn();
         return "V3Data.StateData(lerpIn=" + var10000 + ", lerpOut=" + this.getLerpOut() + ", time=" + this.getTime() + ", speed=" + this.getSpeed() + ", phase=" + this.getPhase() + ", forceLoopMode=" + this.getForceLoopMode() + ", forceOverride=" + this.isForceOverride() + ")";
      }
   }

   public static class StateProperty {
      private String stateId;
      private double lerpIn;
      private double lerpOut;
      private double speed;

      public String getStateId() {
         return this.stateId;
      }

      public double getLerpIn() {
         return this.lerpIn;
      }

      public double getLerpOut() {
         return this.lerpOut;
      }

      public double getSpeed() {
         return this.speed;
      }

      public void setStateId(String stateId) {
         this.stateId = stateId;
      }

      public void setLerpIn(double lerpIn) {
         this.lerpIn = lerpIn;
      }

      public void setLerpOut(double lerpOut) {
         this.lerpOut = lerpOut;
      }

      public void setSpeed(double speed) {
         this.speed = speed;
      }

      public boolean equals(Object o) {
         if (o == this) {
            return true;
         } else if (!(o instanceof V3Data.StateProperty)) {
            return false;
         } else {
            V3Data.StateProperty other = (V3Data.StateProperty)o;
            if (!other.canEqual(this)) {
               return false;
            } else if (Double.compare(this.getLerpIn(), other.getLerpIn()) != 0) {
               return false;
            } else if (Double.compare(this.getLerpOut(), other.getLerpOut()) != 0) {
               return false;
            } else if (Double.compare(this.getSpeed(), other.getSpeed()) != 0) {
               return false;
            } else {
               Object this$stateId = this.getStateId();
               Object other$stateId = other.getStateId();
               if (this$stateId == null) {
                  if (other$stateId != null) {
                     return false;
                  }
               } else if (!this$stateId.equals(other$stateId)) {
                  return false;
               }

               return true;
            }
         }
      }

      protected boolean canEqual(Object other) {
         return other instanceof V3Data.StateProperty;
      }

      public int hashCode() {
         int PRIME = true;
         int result = 1;
         long $lerpIn = Double.doubleToLongBits(this.getLerpIn());
         int result = result * 59 + (int)($lerpIn >>> 32 ^ $lerpIn);
         long $lerpOut = Double.doubleToLongBits(this.getLerpOut());
         result = result * 59 + (int)($lerpOut >>> 32 ^ $lerpOut);
         long $speed = Double.doubleToLongBits(this.getSpeed());
         result = result * 59 + (int)($speed >>> 32 ^ $speed);
         Object $stateId = this.getStateId();
         result = result * 59 + ($stateId == null ? 43 : $stateId.hashCode());
         return result;
      }

      public String toString() {
         String var10000 = this.getStateId();
         return "V3Data.StateProperty(stateId=" + var10000 + ", lerpIn=" + this.getLerpIn() + ", lerpOut=" + this.getLerpOut() + ", speed=" + this.getSpeed() + ")";
      }
   }
}
