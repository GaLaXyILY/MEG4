package com.ticxo.modelengine.core.data;

import com.google.common.collect.Maps;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.animation.BlueprintAnimation;
import com.ticxo.modelengine.api.animation.ModelState;
import com.ticxo.modelengine.api.animation.handler.AnimationHandler;
import com.ticxo.modelengine.api.animation.property.IAnimationProperty;
import com.ticxo.modelengine.api.animation.property.SimpleProperty;
import com.ticxo.modelengine.api.entity.Dummy;
import com.ticxo.modelengine.api.entity.Hitbox;
import com.ticxo.modelengine.api.generator.blueprint.ModelBlueprint;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.bone.BoneBehaviorTypes;
import com.ticxo.modelengine.api.model.bone.behavior.GlobalBehaviorData;
import com.ticxo.modelengine.api.model.bone.manager.MountData;
import com.ticxo.modelengine.api.model.bone.manager.MountManager;
import com.ticxo.modelengine.api.nms.entity.wrapper.BodyRotationController;
import com.ticxo.modelengine.api.utils.data.io.SavedData;
import com.ticxo.modelengine.core.model.ModeledEntityImpl;
import java.util.Map;
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
    private final Map<String, ModelData> models = Maps.newConcurrentMap();

    public SavedData convert(@Nullable Location location) {
        MountData mountData;
        Object manager;
        Dummy dummy = new Dummy();
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
        ModeledEntityImpl modeledEntity = new ModeledEntityImpl(this, dummy, null){
            final /* synthetic */ V3Data this$0;

            @Override
            public void registerSelf() {
            }
        };
        modeledEntity.setBaseEntityVisible(this.isBaseEntityVisible);
        modeledEntity.setModelRotationLocked(this.modelRotationLock);
        this.models.forEach((arg_0, arg_1) -> V3Data.lambda$convert$2(modeledEntity, arg_0, arg_1));
        GlobalBehaviorData globalData = modeledEntity.getGlobalBehaviorData(BoneBehaviorTypes.MOUNT);
        if (globalData instanceof MountData && (manager = (mountData = (MountData)((Object)globalData)).getMainMountManager()) != null) {
            ((MountManager)manager).setCanDrive(this.canSteer);
            ((MountManager)manager).setCanRide(this.canRide);
        }
        SavedData data = modeledEntity.save().orElse(null);
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

    public Map<String, ModelData> getModels() {
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
        }
        if (!(o instanceof V3Data)) {
            return false;
        }
        V3Data other = (V3Data)o;
        if (!other.canEqual(this)) {
            return false;
        }
        if (this.getRenderDistance() != other.getRenderDistance()) {
            return false;
        }
        if (this.isHeadClampUneven() != other.isHeadClampUneven()) {
            return false;
        }
        if (this.isBodyClampUneven() != other.isBodyClampUneven()) {
            return false;
        }
        if (Float.compare(this.getMaxHeadAngle(), other.getMaxHeadAngle()) != 0) {
            return false;
        }
        if (Float.compare(this.getMaxBodyAngle(), other.getMaxBodyAngle()) != 0) {
            return false;
        }
        if (Float.compare(this.getMinHeadAngle(), other.getMinHeadAngle()) != 0) {
            return false;
        }
        if (Float.compare(this.getMinBodyAngle(), other.getMinBodyAngle()) != 0) {
            return false;
        }
        if (this.isPlayerMode() != other.isPlayerMode()) {
            return false;
        }
        if (Float.compare(this.getStableAngle(), other.getStableAngle()) != 0) {
            return false;
        }
        if (this.getRotationDelay() != other.getRotationDelay()) {
            return false;
        }
        if (this.getRotationDuration() != other.getRotationDuration()) {
            return false;
        }
        if (this.isCanSteer() != other.isCanSteer()) {
            return false;
        }
        if (this.isCanRide() != other.isCanRide()) {
            return false;
        }
        if (this.isBaseEntityVisible() != other.isBaseEntityVisible()) {
            return false;
        }
        if (this.isModelRotationLock() != other.isModelRotationLock()) {
            return false;
        }
        Double this$stepHeight = this.getStepHeight();
        Double other$stepHeight = other.getStepHeight();
        if (this$stepHeight == null ? other$stepHeight != null : !((Object)this$stepHeight).equals(other$stepHeight)) {
            return false;
        }
        String this$mountModelId = this.getMountModelId();
        String other$mountModelId = other.getMountModelId();
        if (this$mountModelId == null ? other$mountModelId != null : !this$mountModelId.equals(other$mountModelId)) {
            return false;
        }
        String this$mountDriverBone = this.getMountDriverBone();
        String other$mountDriverBone = other.getMountDriverBone();
        if (this$mountDriverBone == null ? other$mountDriverBone != null : !this$mountDriverBone.equals(other$mountDriverBone)) {
            return false;
        }
        Hitbox this$hitbox = this.getHitbox();
        Hitbox other$hitbox = other.getHitbox();
        if (this$hitbox == null ? other$hitbox != null : !((Object)this$hitbox).equals(other$hitbox)) {
            return false;
        }
        Map<String, ModelData> this$models = this.getModels();
        Map<String, ModelData> other$models = other.getModels();
        return !(this$models == null ? other$models != null : !((Object)this$models).equals(other$models));
    }

    protected boolean canEqual(Object other) {
        return other instanceof V3Data;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        result = result * 59 + this.getRenderDistance();
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
        Double $stepHeight = this.getStepHeight();
        result = result * 59 + ($stepHeight == null ? 43 : ((Object)$stepHeight).hashCode());
        String $mountModelId = this.getMountModelId();
        result = result * 59 + ($mountModelId == null ? 43 : $mountModelId.hashCode());
        String $mountDriverBone = this.getMountDriverBone();
        result = result * 59 + ($mountDriverBone == null ? 43 : $mountDriverBone.hashCode());
        Hitbox $hitbox = this.getHitbox();
        result = result * 59 + ($hitbox == null ? 43 : ((Object)$hitbox).hashCode());
        Map<String, ModelData> $models = this.getModels();
        result = result * 59 + ($models == null ? 43 : ((Object)$models).hashCode());
        return result;
    }

    public String toString() {
        return "V3Data(renderDistance=" + this.getRenderDistance() + ", isHeadClampUneven=" + this.isHeadClampUneven() + ", isBodyClampUneven=" + this.isBodyClampUneven() + ", maxHeadAngle=" + this.getMaxHeadAngle() + ", maxBodyAngle=" + this.getMaxBodyAngle() + ", minHeadAngle=" + this.getMinHeadAngle() + ", minBodyAngle=" + this.getMinBodyAngle() + ", playerMode=" + this.isPlayerMode() + ", stableAngle=" + this.getStableAngle() + ", rotationDelay=" + this.getRotationDelay() + ", rotationDuration=" + this.getRotationDuration() + ", canSteer=" + this.isCanSteer() + ", canRide=" + this.isCanRide() + ", mountModelId=" + this.getMountModelId() + ", mountDriverBone=" + this.getMountDriverBone() + ", isBaseEntityVisible=" + this.isBaseEntityVisible() + ", modelRotationLock=" + this.isModelRotationLock() + ", stepHeight=" + this.getStepHeight() + ", hitbox=" + this.getHitbox() + ", models=" + this.getModels() + ")";
    }

    private static /* synthetic */ void lambda$convert$2(1 modeledEntity, String modelId, ModelData modelData) {
        ActiveModel activeModel = ModelEngineAPI.createActiveModel(modelId);
        if (activeModel == null) {
            return;
        }
        activeModel.setCanHurt(modelData.canHurt);
        activeModel.setLockPitch(modelData.lockPitch);
        activeModel.setLockYaw(modelData.lockYaw);
        modelData.defaultStates.forEach((modelState, property) -> activeModel.getAnimationHandler().setDefaultProperty(new AnimationHandler.DefaultProperty((ModelState)modelState, property.stateId, property.lerpIn, property.lerpOut, property.speed)));
        ModelBlueprint blueprint = activeModel.getBlueprint();
        modelData.states.forEach((state, stateData) -> {
            BlueprintAnimation animation = blueprint.getAnimations().get(state);
            if (animation == null) {
                return;
            }
            SimpleProperty property = new SimpleProperty(activeModel, animation, stateData.lerpIn, stateData.lerpOut, stateData.speed);
            property.setPhase(stateData.phase);
            property.setForceLoopMode(stateData.forceLoopMode);
            property.setForceOverride(stateData.forceOverride);
            activeModel.getAnimationHandler().playAnimation(property, true);
        });
        modeledEntity.addModel(activeModel, false).ifPresent(ActiveModel::destroy);
    }

    public static class ModelData {
        public Map<String, StateData> getStates() {
            return null;
        }

        public Map<ModelState, StateProperty> getDefaultStates() {
            return null;
        }

        public boolean isCanHurt() {
            return false;
        }

        public boolean isLockPitch() {
            return false;
        }

        public boolean isLockYaw() {
            return false;
        }

        public void setCanHurt(boolean bl) {
        }

        public void setLockPitch(boolean bl) {
        }

        public void setLockYaw(boolean bl) {
        }

        public boolean equals(Object object) {
            return false;
        }

        protected boolean canEqual(Object object) {
            return false;
        }

        public int hashCode() {
            return 0;
        }

        public String toString() {
            return null;
        }
    }

    public static class StateData {
        public double getLerpIn() {
            return 0.0;
        }

        public double getLerpOut() {
            return 0.0;
        }

        public double getTime() {
            return 0.0;
        }

        public double getSpeed() {
            return 0.0;
        }

        public IAnimationProperty.Phase getPhase() {
            return null;
        }

        public BlueprintAnimation.LoopMode getForceLoopMode() {
            return null;
        }

        public boolean isForceOverride() {
            return false;
        }

        public void setLerpIn(double d) {
        }

        public void setLerpOut(double d) {
        }

        public void setTime(double d) {
        }

        public void setSpeed(double d) {
        }

        public void setPhase(IAnimationProperty.Phase phase) {
        }

        public void setForceLoopMode(BlueprintAnimation.LoopMode loopMode) {
        }

        public void setForceOverride(boolean bl) {
        }

        public boolean equals(Object object) {
            return false;
        }

        protected boolean canEqual(Object object) {
            return false;
        }

        public int hashCode() {
            return 0;
        }

        public String toString() {
            return null;
        }
    }

    public static class StateProperty {
        public String getStateId() {
            return null;
        }

        public double getLerpIn() {
            return 0.0;
        }

        public double getLerpOut() {
            return 0.0;
        }

        public double getSpeed() {
            return 0.0;
        }

        public void setStateId(String string) {
        }

        public void setLerpIn(double d) {
        }

        public void setLerpOut(double d) {
        }

        public void setSpeed(double d) {
        }

        public boolean equals(Object object) {
            return false;
        }

        protected boolean canEqual(Object object) {
            return false;
        }

        public int hashCode() {
            return 0;
        }

        public String toString() {
            return null;
        }
    }
}
