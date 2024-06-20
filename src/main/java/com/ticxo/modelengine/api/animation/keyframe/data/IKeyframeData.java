package com.ticxo.modelengine.api.animation.keyframe.data;

import com.ticxo.modelengine.api.animation.property.IAnimationProperty;

public interface IKeyframeData {
   IKeyframeData EMPTY = (property) -> {
      return 0.0D;
   };

   double getValue(IAnimationProperty var1);
}
