package com.ticxo.modelengine.api.animation.interpolator;

import com.ticxo.modelengine.api.animation.keyframe.type.AbstractKeyframe;
import com.ticxo.modelengine.api.animation.property.IAnimationProperty;
import com.ticxo.modelengine.api.model.bone.ModelBone;
import java.util.TreeMap;
import java.util.function.Supplier;
import org.jetbrains.annotations.Nullable;

public class KeyframeInterpolator<IN extends AbstractKeyframe<OUT>, OUT> extends TreeMap<Float, IN> {
   protected KeyframeInterpolator.Interpolation<IN, OUT> interpolateFunc;
   protected Supplier<OUT> defaultValue = null;

   public KeyframeInterpolator<IN, OUT> setInterpolateFunc(KeyframeInterpolator.Interpolation<IN, OUT> interpolateFunc) {
      this.interpolateFunc = interpolateFunc;
      return this;
   }

   public KeyframeInterpolator<IN, OUT> setDefaultValue(Supplier<OUT> value) {
      this.defaultValue = value;
      return this;
   }

   @Nullable
   public OUT interpolate(ModelBone bone, IAnimationProperty property) {
      if (this.isEmpty()) {
         return this.defaultValue.get();
      } else {
         float time = (float)property.getTime();
         if (this.containsKey(time)) {
            return ((AbstractKeyframe)this.get(time)).getValue(0, property);
         } else {
            float nextKey = this.getHigherKey(time);
            float lastKey = this.getLowerKey(time);
            if (nextKey == lastKey) {
               return ((AbstractKeyframe)this.get(lastKey)).getValue(0, property);
            } else {
               float t = (time - lastKey) / (nextKey - lastKey);
               OUT next = ((AbstractKeyframe)this.get(nextKey)).getValue(0, property);
               OUT prev = ((AbstractKeyframe)this.get(lastKey)).getValue(0, property);
               return this.interpolateFunc.interpolate(new KeyframeInterpolator.Context(lastKey, nextKey, property, bone, this), prev, next, t);
            }
         }
      }
   }

   public float getHigherKey(float time) {
      Float high = (Float)this.higherKey(time);
      return high == null ? (Float)this.lastKey() : high;
   }

   public float getLowerKey(float time) {
      Float low = (Float)this.lowerKey(time);
      return low == null ? (Float)this.firstKey() : low;
   }

   @FunctionalInterface
   public interface Interpolation<IN extends AbstractKeyframe<OUT>, OUT> {
      OUT interpolate(KeyframeInterpolator.Context<IN, OUT> var1, OUT var2, OUT var3, float var4);
   }

   public static class Context<IN extends AbstractKeyframe<OUT>, OUT> {
      public final float prevKey;
      public final float nextKey;
      public final IAnimationProperty property;
      public final ModelBone bone;
      public final KeyframeInterpolator<IN, OUT> interpolator;

      public Context(float prevKey, float nextKey, IAnimationProperty property, ModelBone bone, KeyframeInterpolator<IN, OUT> interpolator) {
         this.prevKey = prevKey;
         this.nextKey = nextKey;
         this.property = property;
         this.bone = bone;
         this.interpolator = interpolator;
      }
   }
}
