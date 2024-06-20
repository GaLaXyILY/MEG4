package com.ticxo.modelengine.api.animation;

import com.google.common.collect.Maps;
import com.ticxo.modelengine.api.animation.interpolator.KeyframeInterpolator;
import com.ticxo.modelengine.api.animation.keyframe.KeyframeType;
import com.ticxo.modelengine.api.animation.keyframe.type.AbstractKeyframe;
import java.util.Map;

public class Timeline {
   private final BlueprintAnimation animation;
   private final boolean globalRotation;
   private final Map<KeyframeType<?, ?>, KeyframeInterpolator<?, ?>> interpolators = Maps.newConcurrentMap();

   public boolean hasInterpolator(KeyframeType<?, ?> type) {
      return this.interpolators.containsKey(type);
   }

   public <KEY extends AbstractKeyframe<DATA>, DATA> KeyframeInterpolator<KEY, DATA> getInterpolator(KeyframeType<KEY, DATA> type) {
      return (KeyframeInterpolator)this.interpolators.computeIfAbsent(type, (keyframeType) -> {
         return keyframeType.createInterpolator(this);
      });
   }

   public <KEY extends AbstractKeyframe<DATA>, DATA> KEY getKeyframe(float time, KeyframeType<KEY, DATA> type) {
      KeyframeInterpolator<KEY, DATA> interpolator = this.getInterpolator(type);
      return (AbstractKeyframe)interpolator.computeIfAbsent(time, (t) -> {
         return type.createKeyframe();
      });
   }

   public Timeline(BlueprintAnimation animation, boolean globalRotation) {
      this.animation = animation;
      this.globalRotation = globalRotation;
   }

   public BlueprintAnimation getAnimation() {
      return this.animation;
   }

   public boolean isGlobalRotation() {
      return this.globalRotation;
   }
}
