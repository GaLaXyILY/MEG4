package com.ticxo.modelengine.api.animation.keyframe.type;

import com.ticxo.modelengine.api.animation.property.IAnimationProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractKeyframe<T> {
   protected final float[] leftTime = new float[3];
   protected final float[] leftValue = new float[3];
   protected final float[] rightTime = new float[3];
   protected final float[] rightValue = new float[3];
   @NotNull
   protected String interpolation = "";

   public boolean isBezier() {
      return "bezier".equalsIgnoreCase(this.interpolation);
   }

   public void setBezierLeftTime(@Nullable Float x, @Nullable Float y, @Nullable Float z) {
      if (x != null) {
         this.leftTime[0] = x;
      }

      if (y != null) {
         this.leftTime[1] = y;
      }

      if (z != null) {
         this.leftTime[2] = z;
      }

   }

   public void setBezierLeftValue(@Nullable Float x, @Nullable Float y, @Nullable Float z) {
      if (x != null) {
         this.leftValue[0] = x;
      }

      if (y != null) {
         this.leftValue[1] = y;
      }

      if (z != null) {
         this.leftValue[2] = z;
      }

   }

   public void setBezierRightTime(@Nullable Float x, @Nullable Float y, @Nullable Float z) {
      if (x != null) {
         this.rightTime[0] = x;
      }

      if (y != null) {
         this.rightTime[1] = y;
      }

      if (z != null) {
         this.rightTime[2] = z;
      }

   }

   public void setBezierRightValue(@Nullable Float x, @Nullable Float y, @Nullable Float z) {
      if (x != null) {
         this.rightValue[0] = x;
      }

      if (y != null) {
         this.rightValue[1] = y;
      }

      if (z != null) {
         this.rightValue[2] = z;
      }

   }

   public abstract T getValue(int var1, IAnimationProperty var2);

   public float[] getLeftTime() {
      return this.leftTime;
   }

   public float[] getLeftValue() {
      return this.leftValue;
   }

   public float[] getRightTime() {
      return this.rightTime;
   }

   public float[] getRightValue() {
      return this.rightValue;
   }

   @NotNull
   public String getInterpolation() {
      return this.interpolation;
   }

   public void setInterpolation(@NotNull String interpolation) {
      this.interpolation = interpolation;
   }
}
