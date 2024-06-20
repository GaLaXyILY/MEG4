package com.ticxo.modelengine.api.utils.data.interpolator;

import java.util.TreeMap;
import org.jetbrains.annotations.Nullable;

public class Interpolator<IN, OUT> extends TreeMap<Float, IN> {
   protected Interpolator.Interpolation<IN> interpolateFunc;
   protected Interpolator.Parse<IN, OUT> parseFunc;
   protected OUT defaultValue = null;

   public Interpolator<IN, OUT> setInterpolateFunc(Interpolator.Interpolation<IN> interpolateFunc) {
      this.interpolateFunc = interpolateFunc;
      return this;
   }

   public Interpolator<IN, OUT> setParseFunc(Interpolator.Parse<IN, OUT> parseFunc) {
      this.parseFunc = parseFunc;
      return this;
   }

   public Interpolator<IN, OUT> setDefaultValue(OUT value) {
      this.defaultValue = value;
      return this;
   }

   @Nullable
   public OUT interpolate(float key) {
      if (this.isEmpty()) {
         return this.defaultValue;
      } else if (this.containsKey(key)) {
         return this.parseFunc.parse(this.get(key));
      } else {
         float nextKey = this.getHigherKey(key);
         float lastKey = this.getLowerKey(key);
         if (nextKey == lastKey) {
            return this.parseFunc.parse(this.get(lastKey));
         } else {
            float t = (key - lastKey) / (nextKey - lastKey);
            IN next = this.get(nextKey);
            IN prev = this.get(lastKey);
            return this.parseFunc.parse(this.interpolateFunc.interpolate(new Interpolator.Context(lastKey, nextKey), prev, next, t));
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
   public interface Interpolation<IN> {
      IN interpolate(Interpolator.Context var1, IN var2, IN var3, float var4);
   }

   @FunctionalInterface
   public interface Parse<IN, OUT> {
      OUT parse(IN var1);
   }

   public static class Context {
      public final float prevKey;
      public final float nextKey;

      public Context(float prevKey, float nextKey) {
         this.prevKey = prevKey;
         this.nextKey = nextKey;
      }
   }
}
