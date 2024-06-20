package com.ticxo.modelengine.api.animation;

import com.ticxo.modelengine.api.animation.interpolator.KeyframeInterpolator;
import com.ticxo.modelengine.api.animation.keyframe.KeyframeType;
import com.ticxo.modelengine.api.animation.keyframe.KeyframeTypes;
import com.ticxo.modelengine.api.animation.keyframe.type.AbstractKeyframe;
import com.ticxo.modelengine.api.animation.keyframe.type.ScriptKeyframe;
import com.ticxo.modelengine.api.animation.property.IAnimationProperty;
import com.ticxo.modelengine.api.generator.blueprint.ModelBlueprint;
import com.ticxo.modelengine.api.model.bone.ModelBone;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class BlueprintAnimation {
   private final ModelBlueprint modelBlueprint;
   private final Map<String, Timeline> timelines = new HashMap();
   private final String name;
   private final Timeline globalTimeline = new Timeline(this, false);
   private double length;
   private BlueprintAnimation.LoopMode loopMode;
   private boolean override;

   public Vector3f getPosition(ModelBone bone, IAnimationProperty property) {
      Timeline timeline = (Timeline)this.timelines.get(bone.getBoneId());
      return timeline == null ? null : (Vector3f)timeline.getInterpolator(KeyframeTypes.POSITION).interpolate(bone, property);
   }

   public Vector3f getRotation(ModelBone bone, IAnimationProperty property) {
      Timeline timeline = (Timeline)this.timelines.get(bone.getBoneId());
      return timeline == null ? null : (Vector3f)timeline.getInterpolator(KeyframeTypes.ROTATION).interpolate(bone, property);
   }

   public Vector3f getScale(ModelBone bone, IAnimationProperty property) {
      Timeline timeline = (Timeline)this.timelines.get(bone.getBoneId());
      return timeline == null ? null : (Vector3f)timeline.getInterpolator(KeyframeTypes.SCALE).interpolate(bone, property);
   }

   public List<ScriptKeyframe.Script> getScript(IAnimationProperty property) {
      return (List)this.globalTimeline.getInterpolator(KeyframeTypes.SCRIPT).interpolate((ModelBone)null, property);
   }

   public String getInterpolation(KeyframeType<?, ?> type, String bone, float time) {
      Timeline timeline = (Timeline)this.timelines.get(bone);
      if (timeline == null) {
         return null;
      } else {
         KeyframeInterpolator<? extends AbstractKeyframe<?>, ?> interpolator = timeline.getInterpolator(type);
         AbstractKeyframe<?> frame = (AbstractKeyframe)interpolator.get(time);
         if (frame != null) {
            return frame.getInterpolation();
         } else {
            frame = (AbstractKeyframe)interpolator.get(interpolator.getLowerKey(time));
            return frame == null ? null : frame.getInterpolation();
         }
      }
   }

   public ModelBlueprint getModelBlueprint() {
      return this.modelBlueprint;
   }

   public Map<String, Timeline> getTimelines() {
      return this.timelines;
   }

   public String getName() {
      return this.name;
   }

   public Timeline getGlobalTimeline() {
      return this.globalTimeline;
   }

   public double getLength() {
      return this.length;
   }

   public BlueprintAnimation.LoopMode getLoopMode() {
      return this.loopMode;
   }

   public boolean isOverride() {
      return this.override;
   }

   public BlueprintAnimation(ModelBlueprint modelBlueprint, String name) {
      this.modelBlueprint = modelBlueprint;
      this.name = name;
   }

   public void setLength(double length) {
      this.length = length;
   }

   public void setLoopMode(BlueprintAnimation.LoopMode loopMode) {
      this.loopMode = loopMode;
   }

   public void setOverride(boolean override) {
      this.override = override;
   }

   public static enum LoopMode {
      ONCE,
      HOLD,
      LOOP;

      public static BlueprintAnimation.LoopMode get(String mode) {
         try {
            return valueOf(mode.toUpperCase(Locale.ENGLISH));
         } catch (IllegalArgumentException var2) {
            return ONCE;
         }
      }

      @Nullable
      public static BlueprintAnimation.LoopMode getOrNull(String mode) {
         if (mode == null) {
            return null;
         } else {
            try {
               return valueOf(mode.toUpperCase(Locale.ENGLISH));
            } catch (IllegalArgumentException var2) {
               return null;
            }
         }
      }

      // $FF: synthetic method
      private static BlueprintAnimation.LoopMode[] $values() {
         return new BlueprintAnimation.LoopMode[]{ONCE, HOLD, LOOP};
      }
   }
}
