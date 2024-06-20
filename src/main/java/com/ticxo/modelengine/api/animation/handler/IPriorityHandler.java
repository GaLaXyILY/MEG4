package com.ticxo.modelengine.api.animation.handler;

import com.ticxo.modelengine.api.animation.ModelState;
import com.ticxo.modelengine.api.animation.property.IAnimationProperty;
import java.util.function.BiConsumer;

public interface IPriorityHandler extends AnimationHandler {
   void forEachProperty(BiConsumer<String, IAnimationProperty> var1);

   void playState(ModelState var1);

   default String getId() {
      return "priority";
   }
}
