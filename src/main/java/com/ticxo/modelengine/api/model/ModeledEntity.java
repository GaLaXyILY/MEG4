package com.ticxo.modelengine.api.model;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.entity.BaseEntity;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehavior;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehaviorType;
import com.ticxo.modelengine.api.model.bone.behavior.GlobalBehaviorData;
import com.ticxo.modelengine.api.model.bone.manager.MountData;
import com.ticxo.modelengine.api.utils.data.io.DataIO;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ModeledEntity extends DataIO {
   BaseEntity<?> getBase();

   int getTick();

   boolean tick();

   void destroy();

   boolean isInitialized();

   boolean isDestroyed();

   void markRemoved();

   void restore();

   void queuePostInitTask(Runnable var1);

   boolean isBaseEntityVisible();

   void setBaseEntityVisible(boolean var1);

   void markHurt();

   int getHurtTick();

   boolean shouldBeSaved();

   void setSaved(boolean var1);

   boolean isModelRotationLocked();

   void setModelRotationLocked(boolean var1);

   boolean isGlowing();

   int getGlowColor();

   float getYHeadRot();

   float getXHeadRot();

   float getYBodyRot();

   Optional<ActiveModel> addModel(@NotNull ActiveModel var1, boolean var2);

   Optional<ActiveModel> removeModel(String var1);

   Optional<ActiveModel> getModel(@Nullable String var1);

   Map<String, ActiveModel> getModels();

   <T extends BoneBehavior> GlobalBehaviorData getOrCreateGlobalBehaviorData(BoneBehaviorType<T> var1, Supplier<GlobalBehaviorData> var2);

   <T extends BoneBehavior> GlobalBehaviorData getGlobalBehaviorData(BoneBehaviorType<T> var1);

   <T extends BoneBehavior> GlobalBehaviorData removeGlobalBehaviorData(BoneBehaviorType<T> var1);

   Map<BoneBehaviorType<?>, GlobalBehaviorData> getAllGlobalBehaviorData(BoneBehaviorType<?> var1);

   <T extends GlobalBehaviorData & MountData> T getMountData();

   default void registerSelf() {
      ModelEngineAPI.getAPI().getModelUpdaters().registerModeledEntity(this.getBase(), this).ifPresent(ModeledEntity::destroy);
   }
}
