package com.ticxo.modelengine.api.animation.interpolator;

import com.ticxo.modelengine.api.animation.keyframe.type.AbstractKeyframe;
import com.ticxo.modelengine.api.animation.property.IAnimationProperty;
import com.ticxo.modelengine.api.model.bone.ModelBone;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import org.jetbrains.annotations.Nullable;

public class PrePostInterpolator<IN extends AbstractKeyframe<OUT>, OUT> extends KeyframeInterpolator<IN, OUT> {
   private final BiConsumer<KeyframeInterpolator.Context<IN, OUT>, IN> finalizerFunc;

   public PrePostInterpolator(KeyframeInterpolator.Interpolation<IN, OUT> interpolateFunc, BiConsumer<KeyframeInterpolator.Context<IN, OUT>, IN> finalizerFunc) {
      this(interpolateFunc, finalizerFunc, () -> {
         return null;
      });
   }

   public PrePostInterpolator(KeyframeInterpolator.Interpolation<IN, OUT> interpolateFunc, BiConsumer<KeyframeInterpolator.Context<IN, OUT>, IN> finalizerFunc, Supplier<OUT> def) {
      this.setInterpolateFunc(interpolateFunc);
      this.finalizerFunc = finalizerFunc;
      this.setDefaultValue(def);
   }

   @Nullable
   public OUT interpolate(ModelBone bone, IAnimationProperty property) {
      if (this.isEmpty()) {
         return this.defaultValue.get();
      } else {
         float time = (float)property.getTime();
         if (this.containsKey(time)) {
            IN frame = (AbstractKeyframe)this.get(time);
            this.finalizerFunc.accept(new KeyframeInterpolator.Context(time, time, property, bone, this), frame);
            return frame.getValue(0, property);
         } else {
            float nextKey = this.getHigherKey(time);
            float lastKey = this.getLowerKey(time);
            if (nextKey == lastKey) {
               IN frame = (AbstractKeyframe)this.get(lastKey);
               this.finalizerFunc.accept(new KeyframeInterpolator.Context(lastKey, nextKey, property, bone, this), frame);
               return frame.getValue(0, property);
            } else {
               float t = (time - lastKey) / (nextKey - lastKey);
               OUT next = ((AbstractKeyframe)this.get(nextKey)).getValue(0, property);
               OUT prev = ((AbstractKeyframe)this.get(lastKey)).getValue(1, property);
               return this.interpolateFunc.interpolate(new KeyframeInterpolator.Context(lastKey, nextKey, property, bone, this), prev, next, t);
            }
         }
      }
   }
}
