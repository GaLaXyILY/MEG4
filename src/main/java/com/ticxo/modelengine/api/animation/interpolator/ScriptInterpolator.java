package com.ticxo.modelengine.api.animation.interpolator;

import com.ticxo.modelengine.api.animation.keyframe.type.AbstractKeyframe;
import com.ticxo.modelengine.api.animation.property.IAnimationProperty;
import com.ticxo.modelengine.api.model.bone.ModelBone;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import org.jetbrains.annotations.Nullable;

public class ScriptInterpolator<IN extends AbstractKeyframe<OUT>, OUT> extends KeyframeInterpolator<IN, OUT> {
   private final Supplier<OUT> supplier;
   private final BiConsumer<OUT, OUT> combiner;

   @Nullable
   public OUT interpolate(ModelBone bone, IAnimationProperty property) {
      if (this.isEmpty()) {
         return null;
      } else {
         OUT combined = this.supplier.get();
         float lastTime = (float)property.getLastTime();
         float currTime = (float)property.getTime();
         float frame = lastTime;
         float lastFrame = (Float)this.lastKey();
         Object data;
         if (lastTime > currTime) {
            while(frame < lastFrame && (frame = this.getHigherKey(frame)) <= lastFrame) {
               data = ((AbstractKeyframe)this.get(frame)).getValue(0, property);
               this.combiner.accept(combined, data);
            }

            frame = -1.0F;
         }

         while(frame < lastFrame && (frame = this.getHigherKey(frame)) <= currTime) {
            data = ((AbstractKeyframe)this.get(frame)).getValue(0, property);
            this.combiner.accept(combined, data);
         }

         return combined;
      }
   }

   public ScriptInterpolator(Supplier<OUT> supplier, BiConsumer<OUT, OUT> combiner) {
      this.supplier = supplier;
      this.combiner = combiner;
   }
}
