package com.ticxo.modelengine.core.model;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.entity.BaseEntity;
import com.ticxo.modelengine.api.entity.Hitbox;
import com.ticxo.modelengine.api.events.AddModelEvent;
import com.ticxo.modelengine.api.events.RemoveModelEvent;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.model.bone.BoneBehaviorTypes;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehavior;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehaviorType;
import com.ticxo.modelengine.api.model.bone.behavior.GlobalBehaviorData;
import com.ticxo.modelengine.api.model.bone.manager.MountData;
import com.ticxo.modelengine.api.nms.entity.wrapper.BodyRotationController;
import com.ticxo.modelengine.api.utils.data.io.SavedData;
import com.ticxo.modelengine.api.utils.data.tracker.DataTracker;
import com.ticxo.modelengine.api.utils.math.TMath;
import com.ticxo.modelengine.core.model.bone.manager.MountDataImpl;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class ModeledEntityImpl implements ModeledEntity {
   private final BaseEntity<?> base;
   private final Map<String, ActiveModel> models = Maps.newConcurrentMap();
   private final Map<BoneBehaviorType<?>, GlobalBehaviorData> data = Maps.newConcurrentMap();
   private final boolean initialized;
   private final List<Runnable> queuedTask = new ArrayList();
   private final DataTracker<Float> trueYHeadRot = new DataTracker(TMath::isSimilar);
   private final DataTracker<Float> trueXHeadRot = new DataTracker(TMath::isSimilar);
   private final DataTracker<Float> trueYBodyRot = new DataTracker(TMath::isSimilar);
   private int tick;
   private boolean isBaseEntityVisible = true;
   private boolean destroyed;
   private boolean removed;
   private int hurtTick = 0;
   private boolean isModelRotationLocked;
   private int rotationTick = -1;
   private float yHeadRot = 0.0F;
   private float xHeadRot = 0.0F;
   private float yBodyRot = 0.0F;
   private boolean shouldSave = true;
   private ActiveModel lastHitboxOverride = null;

   public ModeledEntityImpl(@NotNull BaseEntity<?> base, @Nullable Consumer<ModeledEntity> consumer) {
      this.base = base;
      this.registerSelf();
      if (consumer != null) {
         consumer.accept(this);
      }

      synchronized(this.queuedTask) {
         this.queuedTask.forEach(Runnable::run);
         this.initialized = true;
      }
   }

   public boolean tick() {
      if (!this.initialized) {
         return true;
      } else {
         if (this.hurtTick > 0) {
            --this.hurtTick;
         }

         if (!this.isModelRotationLocked && this.base.isAlive()) {
            BodyRotationController bodyRotationController = this.base.getBodyRotationController();
            bodyRotationController.tick();
            this.trueYHeadRot.set(bodyRotationController.getYHeadRot());
            this.trueXHeadRot.set(bodyRotationController.getXHeadRot());
            this.trueYBodyRot.set(bodyRotationController.getYBodyRot());
            if (this.rotationTick == -1) {
               this.yHeadRot = (Float)this.trueYHeadRot.get();
               this.xHeadRot = (Float)this.trueXHeadRot.get();
               this.yBodyRot = (Float)this.trueYBodyRot.get();
               this.rotationTick = 0;
            }

            if (!this.base.isWalking()) {
               this.yBodyRot = (Float)this.trueYBodyRot.get();
            }

            if (this.trueYHeadRot.isDirty() || this.trueXHeadRot.isDirty() || this.trueYBodyRot.isDirty()) {
               this.rotationTick = 3;
               this.trueYHeadRot.clearDirty();
               this.trueXHeadRot.clearDirty();
               this.trueYBodyRot.clearDirty();
            }

            if (this.rotationTick > 0) {
               this.yHeadRot = TMath.rotLerp(this.yHeadRot, (Float)this.trueYHeadRot.get(), (double)(1.0F / (float)this.rotationTick));
               this.xHeadRot = TMath.rotLerp(this.xHeadRot, (Float)this.trueXHeadRot.get(), (double)(1.0F / (float)this.rotationTick));
               this.yBodyRot = TMath.rotLerp(this.yBodyRot, (Float)this.trueYBodyRot.get(), (double)(1.0F / (float)this.rotationTick));
               --this.rotationTick;
            }
         }

         boolean hasFinished = true;
         Iterator var2 = this.models.values().iterator();

         while(var2.hasNext()) {
            ActiveModel model = (ActiveModel)var2.next();
            model.tick();
            if (hasFinished) {
               hasFinished = model.getAnimationHandler().hasFinishedAllAnimations();
            }
         }

         this.base.setForcedAlive(!hasFinished);
         ++this.tick;
         return !this.removed && !this.base.isRemoved() && (this.base.isAlive() || !hasFinished && !this.base.getData().getTracking().isEmpty());
      }
   }

   public void destroy() {
      this.destroyed = true;
      this.models.forEach((s, model) -> {
         model.destroy();
      });
      this.models.clear();
   }

   public void markRemoved() {
      this.removed = true;
   }

   public void restore() {
      this.removed = false;
   }

   public void queuePostInitTask(Runnable runnable) {
      synchronized(this.queuedTask) {
         if (this.initialized) {
            runnable.run();
         } else {
            this.queuedTask.add(runnable);
         }

      }
   }

   public void setBaseEntityVisible(boolean flag) {
      if (this.isBaseEntityVisible() != flag) {
         this.isBaseEntityVisible = flag;
         this.base.setVisible(flag);
      }
   }

   public void markHurt() {
      this.hurtTick = 10;
   }

   public boolean shouldBeSaved() {
      return this.shouldSave;
   }

   public void setSaved(boolean flag) {
      this.shouldSave = flag;
   }

   public boolean isGlowing() {
      return this.base.isGlowing();
   }

   public int getGlowColor() {
      return this.base.getGlowColor();
   }

   public Optional<ActiveModel> addModel(@NotNull ActiveModel model, boolean overrideHitbox) {
      assert !this.isDestroyed() : "Modeled Entity has been destroyed!";

      assert model.getModeledEntity() == null || model.isRemoved() : "Active Model already belongs to a different Modeled Entity";

      AddModelEvent event = new AddModelEvent(this, model);
      event.setOverrideHitbox(overrideHitbox);
      ModelEngineAPI.callEvent(event);
      if (event.isCancelled()) {
         return Optional.empty();
      } else {
         model.setRemoved(false);
         model.setModeledEntity(this);
         model.generateModel();
         Optional<ActiveModel> previous = this.removeModel(model.getBlueprint().getName());
         this.models.put(model.getBlueprint().getName(), model);
         model.getMountManager().ifPresent((mountManager) -> {
            GlobalBehaviorData mountData = this.getMountData();
            if (((MountData)mountData).getMainMountManager() == null) {
               ((MountData)mountData).setMainMountManager(mountManager);
            }

         });
         if (overrideHitbox) {
            if (this.lastHitboxOverride != null) {
               this.lastHitboxOverride.setMainHitbox(false);
            }

            model.setMainHitbox(true);
            this.lastHitboxOverride = model;
            Hitbox mainHitbox = model.getBlueprint().getMainHitbox();
            Vector3f scale = model.getScale();
            Hitbox scaledRenderHitbox = new Hitbox(mainHitbox.getWidth() * (double)scale.x, mainHitbox.getHeight() * (double)scale.y, mainHitbox.getDepth() * (double)scale.z, mainHitbox.getEyeHeight() * (double)scale.y);
            this.base.getData().setCullHitbox(scaledRenderHitbox);
            Object var9 = this.base.getOriginal();
            if (var9 instanceof Entity) {
               Entity entity = (Entity)var9;
               Vector3f hitboxScale = model.getHitboxScale();
               Hitbox scaledHitbox = new Hitbox(mainHitbox.getWidth() * (double)hitboxScale.x, mainHitbox.getHeight() * (double)hitboxScale.y, mainHitbox.getDepth() * (double)hitboxScale.z, mainHitbox.getEyeHeight() * (double)hitboxScale.y);
               ModelEngineAPI.getEntityHandler().setHitbox(entity, scaledHitbox);
            }
         }

         return previous;
      }
   }

   public Optional<ActiveModel> removeModel(String id) {
      assert !this.isDestroyed() : "Modeled Entity has been destroyed!";

      ActiveModel model = (ActiveModel)this.models.get(id);
      if (model == null) {
         return Optional.empty();
      } else {
         RemoveModelEvent event = new RemoveModelEvent(this, model);
         ModelEngineAPI.callEvent(event);
         if (event.isCancelled()) {
            return Optional.empty();
         } else {
            this.models.remove(id);
            model.setRemoved(true);
            return Optional.of(model);
         }
      }
   }

   public Optional<ActiveModel> getModel(@Nullable String id) {
      return Optional.ofNullable(id == null ? null : (ActiveModel)this.models.get(id));
   }

   public Map<String, ActiveModel> getModels() {
      return ImmutableMap.copyOf(this.models);
   }

   public <T extends BoneBehavior> GlobalBehaviorData getOrCreateGlobalBehaviorData(BoneBehaviorType<T> type, Supplier<GlobalBehaviorData> supplier) {
      return (GlobalBehaviorData)this.data.computeIfAbsent(type, (boneBehaviorType) -> {
         return (GlobalBehaviorData)supplier.get();
      });
   }

   public <T extends BoneBehavior> GlobalBehaviorData getGlobalBehaviorData(BoneBehaviorType<T> type) {
      return (GlobalBehaviorData)this.data.get(type);
   }

   public <T extends BoneBehavior> GlobalBehaviorData removeGlobalBehaviorData(BoneBehaviorType<T> type) {
      return (GlobalBehaviorData)this.data.remove(type);
   }

   public Map<BoneBehaviorType<?>, GlobalBehaviorData> getAllGlobalBehaviorData(BoneBehaviorType<?> type) {
      return ImmutableMap.copyOf(this.data);
   }

   public <T extends GlobalBehaviorData & MountData> T getMountData() {
      return this.getOrCreateGlobalBehaviorData(BoneBehaviorTypes.MOUNT, MountDataImpl::new);
   }

   public void save(SavedData data) {
      data.putString("version", "R4.0.3");
      data.putBoolean("base_visible", this.isBaseEntityVisible());
      data.putBoolean("rotation_locked", this.isModelRotationLocked());
      ArrayList<SavedData> list = new ArrayList();
      Iterator var3 = this.models.values().iterator();

      while(var3.hasNext()) {
         ActiveModel activeModel = (ActiveModel)var3.next();
         Optional var10000 = activeModel.save();
         Objects.requireNonNull(list);
         var10000.ifPresent(list::add);
      }

      data.putList("models", list);
      this.base.save().ifPresent((entityData) -> {
         data.putData("base_entity", entityData);
      });
   }

   public void load(SavedData data) {
      this.setBaseEntityVisible(data.getBoolean("base_visible"));
      this.setModelRotationLocked(data.getBoolean("rotation_locked"));
      List<SavedData> list = data.getList("models", SavedData.class);
      Iterator var3 = list.iterator();

      while(var3.hasNext()) {
         SavedData modelData = (SavedData)var3.next();
         ActiveModel model = ActiveModelImpl.fromData(modelData);
         if (model != null) {
            model.setAutoRendererInitialization(false);
            this.addModel(model, model.isMainHitbox()).ifPresent(ActiveModel::destroy);
            model.load(modelData);
            model.initializeRenderer();
         }
      }

      Optional var10000 = data.getData("base_entity");
      BaseEntity var10001 = this.base;
      Objects.requireNonNull(var10001);
      var10000.ifPresent(var10001::load);
   }

   public BaseEntity<?> getBase() {
      return this.base;
   }

   public Map<BoneBehaviorType<?>, GlobalBehaviorData> getData() {
      return this.data;
   }

   public boolean isInitialized() {
      return this.initialized;
   }

   public List<Runnable> getQueuedTask() {
      return this.queuedTask;
   }

   public DataTracker<Float> getTrueYHeadRot() {
      return this.trueYHeadRot;
   }

   public DataTracker<Float> getTrueXHeadRot() {
      return this.trueXHeadRot;
   }

   public DataTracker<Float> getTrueYBodyRot() {
      return this.trueYBodyRot;
   }

   public int getTick() {
      return this.tick;
   }

   public boolean isBaseEntityVisible() {
      return this.isBaseEntityVisible;
   }

   public boolean isDestroyed() {
      return this.destroyed;
   }

   public boolean isRemoved() {
      return this.removed;
   }

   public int getHurtTick() {
      return this.hurtTick;
   }

   public boolean isModelRotationLocked() {
      return this.isModelRotationLocked;
   }

   public int getRotationTick() {
      return this.rotationTick;
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

   public boolean isShouldSave() {
      return this.shouldSave;
   }

   public ActiveModel getLastHitboxOverride() {
      return this.lastHitboxOverride;
   }

   public void setModelRotationLocked(boolean isModelRotationLocked) {
      this.isModelRotationLocked = isModelRotationLocked;
   }
}
