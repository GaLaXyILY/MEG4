package com.ticxo.modelengine.api.animation.keyframe;

import com.ticxo.modelengine.api.utils.registry.TUnaryRegistry;
import java.util.LinkedHashSet;
import java.util.Set;

public class KeyframeTypeRegistry extends TUnaryRegistry<KeyframeType<?, ?>> {
   private final Set<String> keys = new LinkedHashSet();

   public void registerKeyframeType(KeyframeType<?, ?> type) {
      this.keys.add(type.getId());
      this.register(type.getId(), type);
   }

   public Set<String> getKeys() {
      return this.keys;
   }
}
