package com.ticxo.modelengine.api.animation.keyframe.data;

import com.ticxo.modelengine.api.animation.property.IAnimationProperty;

public class DoubleData implements IKeyframeData {
   private final double data;

   public double getValue(IAnimationProperty property) {
      return this.data;
   }

   public DoubleData(double data) {
      this.data = data;
   }
}
