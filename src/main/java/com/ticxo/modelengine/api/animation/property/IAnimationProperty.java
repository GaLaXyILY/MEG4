package com.ticxo.modelengine.api.animation.property;

import com.ticxo.modelengine.api.animation.BlueprintAnimation;
import com.ticxo.modelengine.api.animation.keyframe.KeyframeType;
import com.ticxo.modelengine.api.animation.keyframe.type.ScriptKeyframe;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.bone.ModelBone;
import com.ticxo.modelengine.api.utils.data.io.DataIO;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public interface IAnimationProperty extends DataIO {
   BlueprintAnimation getBlueprintAnimation();

   boolean update();

   void stop();

   boolean isEnded();

   boolean canReplace();

   String getName();

   boolean containsKeyframe(KeyframeType<?, ?> var1, String var2);

   Vector3f getPositionFrame(ModelBone var1);

   Vector3f getRotationFrame(ModelBone var1);

   Vector3f getScaleFrame(ModelBone var1);

   List<ScriptKeyframe.Script> getScriptFrame();

   double getLerpInRatio();

   double getLerpOutRatio();

   boolean isFinished();

   ActiveModel getModel();

   double getLerpIn();

   double getLerpOut();

   double getLerpInTime();

   void setLerpInTime(double var1);

   double getLerpOutTime();

   void setLerpOutTime(double var1);

   double getLastTime();

   double getTime();

   double getSpeed();

   void setSpeed(double var1);

   @NotNull
   IAnimationProperty.Phase getPhase();

   BlueprintAnimation.LoopMode getForceLoopMode();

   void setForceLoopMode(BlueprintAnimation.LoopMode var1);

   BlueprintAnimation.LoopMode getLoopMode();

   boolean isOverride();

   boolean isForceOverride();

   void setForceOverride(boolean var1);

   public static enum Phase {
      LERPIN,
      PLAY,
      LERPOUT;

      // $FF: synthetic method
      private static IAnimationProperty.Phase[] $values() {
         return new IAnimationProperty.Phase[]{LERPIN, PLAY, LERPOUT};
      }
   }
}
