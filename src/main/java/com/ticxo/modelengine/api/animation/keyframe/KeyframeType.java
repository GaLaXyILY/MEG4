package com.ticxo.modelengine.api.animation.keyframe;

import com.ticxo.modelengine.api.animation.Timeline;
import com.ticxo.modelengine.api.animation.handler.AnimationHandler;
import com.ticxo.modelengine.api.animation.interpolator.KeyframeInterpolator;
import com.ticxo.modelengine.api.animation.keyframe.type.AbstractKeyframe;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.bone.ModelBone;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class KeyframeType<KEY extends AbstractKeyframe<DATA>, DATA> {
   private final String id;
   private final Supplier<KEY> keyframeSupplier;
   private final Function<Timeline, KeyframeInterpolator<KEY, DATA>> interpolatorSupplier;
   private final Map<Class<?>, KeyframeType.ModelUpdater> modelUpdaters;
   private final Map<Class<?>, KeyframeType.BoneUpdater> boneUpdaters;
   private final boolean global;

   public KeyframeInterpolator<KEY, DATA> createInterpolator(Timeline animation) {
      return (KeyframeInterpolator)this.interpolatorSupplier.apply(animation);
   }

   public KEY createKeyframe() {
      return (AbstractKeyframe)this.keyframeSupplier.get();
   }

   public void updateModel(Class<?> handlerClass, AnimationHandler handler, Object... data) {
      KeyframeType.ModelUpdater updater = (KeyframeType.ModelUpdater)this.modelUpdaters.get(handlerClass);
      if (updater != null) {
         updater.update(handler, handler.getActiveModel(), data);
      }
   }

   public void updateBone(Class<?> handlerClass, AnimationHandler handler, ModelBone bone, Object... data) {
      KeyframeType.BoneUpdater updater = (KeyframeType.BoneUpdater)this.boneUpdaters.get(handlerClass);
      if (updater != null) {
         updater.update(handler, bone, data);
      }
   }

   public String getId() {
      return this.id;
   }

   public Supplier<KEY> getKeyframeSupplier() {
      return this.keyframeSupplier;
   }

   public Function<Timeline, KeyframeInterpolator<KEY, DATA>> getInterpolatorSupplier() {
      return this.interpolatorSupplier;
   }

   public Map<Class<?>, KeyframeType.ModelUpdater> getModelUpdaters() {
      return this.modelUpdaters;
   }

   public Map<Class<?>, KeyframeType.BoneUpdater> getBoneUpdaters() {
      return this.boneUpdaters;
   }

   public boolean isGlobal() {
      return this.global;
   }

   protected KeyframeType(String id, Supplier<KEY> keyframeSupplier, Function<Timeline, KeyframeInterpolator<KEY, DATA>> interpolatorSupplier, Map<Class<?>, KeyframeType.ModelUpdater> modelUpdaters, Map<Class<?>, KeyframeType.BoneUpdater> boneUpdaters, boolean global) {
      this.id = id;
      this.keyframeSupplier = keyframeSupplier;
      this.interpolatorSupplier = interpolatorSupplier;
      this.modelUpdaters = modelUpdaters;
      this.boneUpdaters = boneUpdaters;
      this.global = global;
   }

   @FunctionalInterface
   public interface ModelUpdater {
      void update(AnimationHandler var1, ActiveModel var2, Object... var3);
   }

   @FunctionalInterface
   public interface BoneUpdater {
      void update(AnimationHandler var1, ModelBone var2, Object... var3);
   }

   public static class Builder<KEY extends AbstractKeyframe<DATA>, DATA> {
      private final String id;
      private final Supplier<KEY> keyframeSupplier;
      private final Map<Class<?>, KeyframeType.ModelUpdater> modelUpdaters = new HashMap();
      private final Map<Class<?>, KeyframeType.BoneUpdater> boneUpdaters = new HashMap();
      private Function<Timeline, KeyframeInterpolator<KEY, DATA>> interpolatorSupplier = (blueprintAnimation) -> {
         return new KeyframeInterpolator();
      };
      private boolean global;

      public static <KEY extends AbstractKeyframe<DATA>, DATA> KeyframeType.Builder<KEY, DATA> of(String id, Supplier<KEY> keyframeSupplier) {
         return new KeyframeType.Builder(id, keyframeSupplier);
      }

      public KeyframeType.Builder<KEY, DATA> interpolator(Function<Timeline, KeyframeInterpolator<KEY, DATA>> interpolatorSupplier) {
         this.interpolatorSupplier = interpolatorSupplier;
         return this;
      }

      public KeyframeType.Builder<KEY, DATA> registerModelUpdater(Class<?> handlerClass, KeyframeType.ModelUpdater updater) {
         this.modelUpdaters.put(handlerClass, updater);
         return this;
      }

      public KeyframeType.Builder<KEY, DATA> registerBoneUpdater(Class<?> handlerClass, KeyframeType.BoneUpdater updater) {
         this.boneUpdaters.put(handlerClass, updater);
         return this;
      }

      public KeyframeType.Builder<KEY, DATA> global() {
         this.global = true;
         return this;
      }

      public KeyframeType<KEY, DATA> build() {
         return new KeyframeType(this.id, this.keyframeSupplier, this.interpolatorSupplier, this.modelUpdaters, this.boneUpdaters, this.global);
      }

      protected Builder(String id, Supplier<KEY> keyframeSupplier) {
         this.id = id;
         this.keyframeSupplier = keyframeSupplier;
      }
   }
}
