package com.ticxo.modelengine.api.model;

import com.ticxo.modelengine.api.animation.handler.AnimationHandler;
import com.ticxo.modelengine.api.generator.blueprint.BlueprintBone;
import com.ticxo.modelengine.api.generator.blueprint.ModelBlueprint;
import com.ticxo.modelengine.api.model.bone.BoneBehaviorTypes;
import com.ticxo.modelengine.api.model.bone.ModelBone;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehavior;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehaviorType;
import com.ticxo.modelengine.api.model.bone.manager.BehaviorManager;
import com.ticxo.modelengine.api.model.bone.manager.LeashManager;
import com.ticxo.modelengine.api.model.bone.manager.MountManager;
import com.ticxo.modelengine.api.model.bone.render.BehaviorRenderer;
import com.ticxo.modelengine.api.model.bone.type.Leash;
import com.ticxo.modelengine.api.model.bone.type.Mount;
import com.ticxo.modelengine.api.model.render.ModelRenderer;
import com.ticxo.modelengine.api.utils.data.io.DataIO;
import java.util.Map;
import java.util.Optional;
import org.bukkit.Color;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public interface ActiveModel extends DataIO {
   ModeledEntity getModeledEntity();

   void setModeledEntity(ModeledEntity var1);

   ModelBlueprint getBlueprint();

   ModelRenderer getModelRenderer();

   AnimationHandler getAnimationHandler();

   Map<String, ModelBone> getBones();

   Map<BoneBehaviorType<?>, BehaviorManager<?>> getBehaviorManagers();

   Map<BoneBehaviorType<?>, BehaviorRenderer> getBehaviorRenderers();

   boolean isMainHitbox();

   void setMainHitbox(boolean var1);

   Vector3f getScale();

   void setScale(double var1);

   Vector3f getHitboxScale();

   void setHitboxScale(double var1);

   void tick();

   void destroy();

   boolean isDestroyed();

   boolean isRemoved();

   void setRemoved(boolean var1);

   void setAutoRendererInitialization(boolean var1);

   boolean isHitboxVisible();

   void setHitboxVisible(boolean var1);

   boolean isShadowVisible();

   void setShadowVisible(boolean var1);

   void initializeRenderer();

   void generateModel();

   void forceGenerateBone(String var1, String var2, BlueprintBone var3);

   void removeBone(String var1);

   void setCanHurt(boolean var1);

   boolean canHurt();

   Color getDefaultTint();

   void setDefaultTint(Color var1);

   Color getDamageTint();

   void setDamageTint(Color var1);

   boolean wasMarkedHurt();

   boolean isMarkedHurt();

   boolean isGlowing();

   void setGlowing(@Nullable Boolean var1);

   int getGlowColor();

   void setGlowColor(@Nullable Integer var1);

   int getBlockLight();

   void setBlockLight(int var1);

   int getSkyLight();

   void setSkyLight(int var1);

   float getXHeadRot();

   float getYHeadRot();

   boolean isLockPitch();

   void setLockPitch(boolean var1);

   boolean isLockYaw();

   void setLockYaw(boolean var1);

   default Optional<ModelBone> getBone(String boneId) {
      return Optional.ofNullable((ModelBone)this.getBones().get(boneId));
   }

   <T extends BoneBehavior> Optional<BehaviorManager<T>> getBehaviorManager(BoneBehaviorType<T> var1);

   Optional<BehaviorRenderer> getBehaviorRenderer(BoneBehaviorType<?> var1);

   default <T extends MountManager & BehaviorManager<? extends Mount>> Optional<T> getMountManager() {
      Optional maybe = this.getBehaviorManager(BoneBehaviorTypes.MOUNT);
      return maybe.map((behaviorManager) -> {
         return (MountManager)behaviorManager;
      });
   }

   default <T extends LeashManager & BehaviorManager<? extends Leash>> Optional<T> getLeashManager() {
      Optional maybe = this.getBehaviorManager(BoneBehaviorTypes.LEASH);
      return maybe.map((behaviorManager) -> {
         return (LeashManager)behaviorManager;
      });
   }
}
