package com.ticxo.modelengine.api.animation;

import com.ticxo.modelengine.api.animation.handler.AnimationHandler;
import com.ticxo.modelengine.api.animation.property.IAnimationProperty;
import com.ticxo.modelengine.api.utils.data.io.SavedData;
import com.ticxo.modelengine.api.utils.registry.TUnaryRegistry;
import java.util.function.BiFunction;

public class AnimationPropertyRegistry extends TUnaryRegistry<BiFunction<AnimationHandler, SavedData, IAnimationProperty>> {
   public IAnimationProperty createAnimationProperty(AnimationHandler handler, SavedData data) {
      String id = data.getString("id");
      return id != null ? (IAnimationProperty)((BiFunction)this.get(id)).apply(handler, data) : null;
   }
}
