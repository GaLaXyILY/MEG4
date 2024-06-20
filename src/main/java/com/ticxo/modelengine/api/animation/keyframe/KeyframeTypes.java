package com.ticxo.modelengine.api.animation.keyframe;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.animation.handler.AnimationHandler;
import com.ticxo.modelengine.api.animation.handler.IPriorityHandler;
import com.ticxo.modelengine.api.animation.handler.IStateMachineHandler;
import com.ticxo.modelengine.api.animation.interpolator.KeyframeInterpolator;
import com.ticxo.modelengine.api.animation.interpolator.PrePostInterpolator;
import com.ticxo.modelengine.api.animation.interpolator.ScriptInterpolator;
import com.ticxo.modelengine.api.animation.keyframe.type.ScriptKeyframe;
import com.ticxo.modelengine.api.animation.keyframe.type.VectorKeyframe;
import com.ticxo.modelengine.api.animation.property.IAnimationProperty;
import com.ticxo.modelengine.api.animation.script.ScriptReader;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.utils.StepFlag;
import com.ticxo.modelengine.api.utils.math.TMath;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.joml.Vector3f;

public final class KeyframeTypes {
   public static final KeyframeType<VectorKeyframe, Vector3f> POSITION = KeyframeType.Builder.of("position", VectorKeyframe::new).interpolator((timeline) -> {
      return new PrePostInterpolator((ctx, prev, next, ratio) -> {
         return standard(ctx, prev, next, ratio, StepFlag.POSITION);
      }, (ctx, vectorKeyframe) -> {
         markStep(ctx, vectorKeyframe, StepFlag.POSITION);
      });
   }).registerBoneUpdater(IPriorityHandler.class, (handler, bone, data) -> {
      IAnimationProperty property = (IAnimationProperty)data[0];
      Vector3f output = bone.getCachedPosition();
      Vector3f val = property.getPositionFrame(bone);
      if (!property.isOverride()) {
         if (val == null) {
            val = new Vector3f();
         }

         switch(property.getPhase()) {
         case LERPOUT:
            val = TMath.lerp(val, new Vector3f(), property.getLerpOutRatio());
            break;
         case LERPIN:
            val = TMath.lerp(new Vector3f(), val, property.getLerpInRatio());
         }

         output.add(val);
      } else if (val != null) {
         switch(property.getPhase()) {
         case PLAY:
            output.set(val);
            break;
         case LERPOUT:
            output.set(TMath.lerp(val, output, property.getLerpOutRatio()));
            break;
         case LERPIN:
            output.set(TMath.lerp(output, val, property.getLerpInRatio()));
         }
      }

   }).registerBoneUpdater(IStateMachineHandler.class, (handler, bone, data) -> {
      IAnimationProperty currProperty = (IAnimationProperty)data[0];
      if (currProperty != null) {
         Vector3f currVal = new Vector3f(bone.getCachedPosition());
         Vector3f valx = currProperty.getPositionFrame(bone);
         if (valx != null) {
            if (!currProperty.isOverride()) {
               currVal.add(valx);
            } else {
               currVal.set(valx);
            }
         }

         switch(currProperty.getPhase()) {
         case PLAY:
            bone.setCachedPosition(currVal);
            return;
         case LERPOUT:
            bone.setCachedPosition(TMath.lerp(currVal, bone.getCachedPosition(), currProperty.getLerpOutRatio()));
            return;
         default:
            IAnimationProperty lastProperty = (IAnimationProperty)data[1];
            if (lastProperty == null) {
               bone.setCachedPosition(TMath.lerp(bone.getCachedPosition(), currVal, currProperty.getLerpInRatio()));
            } else {
               Vector3f lastVal = new Vector3f(bone.getCachedPosition());
               Vector3f val = lastProperty.getPositionFrame(bone);
               if (val != null) {
                  if (!lastProperty.isOverride()) {
                     lastVal.add(val);
                  } else {
                     lastVal.set(val);
                  }
               }

               bone.setCachedPosition(TMath.lerp(lastVal, currVal, currProperty.getLerpInRatio()));
            }
         }
      }
   }).build();
   public static final KeyframeType<VectorKeyframe, Vector3f> ROTATION = KeyframeType.Builder.of("rotation", VectorKeyframe::new).interpolator((timeline) -> {
      return new PrePostInterpolator((ctx, prev, next, ratio) -> {
         return standard(ctx, prev, next, ratio, StepFlag.ROTATION);
      }, (ctx, vectorKeyframe) -> {
         markStep(ctx, vectorKeyframe, StepFlag.ROTATION);
      });
   }).registerBoneUpdater(IPriorityHandler.class, (handler, bone, data) -> {
      IAnimationProperty property = (IAnimationProperty)data[0];
      Vector3f output = bone.getCachedLeftRotation();
      Vector3f val = property.getRotationFrame(bone);
      if (!property.isOverride()) {
         if (val == null) {
            val = new Vector3f();
         }

         switch(property.getPhase()) {
         case LERPOUT:
            val = TMath.slerp(val, new Vector3f(), property.getLerpOutRatio());
            break;
         case LERPIN:
            val = TMath.slerp(new Vector3f(), val, property.getLerpInRatio());
         }

         output.add(val);
      } else if (val != null) {
         switch(property.getPhase()) {
         case PLAY:
            output.set(val);
            break;
         case LERPOUT:
            output.set(TMath.slerp(val, output, property.getLerpOutRatio()));
            break;
         case LERPIN:
            output.set(TMath.slerp(output, val, property.getLerpInRatio()));
         }
      }

   }).registerBoneUpdater(IStateMachineHandler.class, (handler, bone, data) -> {
      IAnimationProperty currProperty = (IAnimationProperty)data[0];
      if (currProperty != null) {
         Vector3f currVal = new Vector3f(bone.getCachedLeftRotation());
         Vector3f valx = currProperty.getRotationFrame(bone);
         if (valx != null) {
            if (!currProperty.isOverride()) {
               currVal.add(valx);
            } else {
               currVal.set(valx);
            }
         }

         switch(currProperty.getPhase()) {
         case PLAY:
            bone.setCachedLeftRotation(currVal);
            return;
         case LERPOUT:
            bone.setCachedLeftRotation(TMath.slerp(currVal, bone.getCachedLeftRotation(), currProperty.getLerpOutRatio()));
            return;
         default:
            IAnimationProperty lastProperty = (IAnimationProperty)data[1];
            if (lastProperty == null) {
               bone.setCachedLeftRotation(TMath.slerp(bone.getCachedLeftRotation(), currVal, currProperty.getLerpInRatio()));
            } else {
               Vector3f lastVal = new Vector3f(bone.getCachedLeftRotation());
               Vector3f val = lastProperty.getRotationFrame(bone);
               if (val != null) {
                  if (!lastProperty.isOverride()) {
                     lastVal.add(val);
                  } else {
                     lastVal.set(val);
                  }
               }

               bone.setCachedLeftRotation(TMath.slerp(lastVal, currVal, currProperty.getLerpInRatio()));
            }
         }
      }
   }).build();
   public static final KeyframeType<VectorKeyframe, Vector3f> SCALE = KeyframeType.Builder.of("scale", VectorKeyframe::new).interpolator((timeline) -> {
      return new PrePostInterpolator((ctx, prev, next, ratio) -> {
         return standard(ctx, prev, next, ratio, StepFlag.SCALE);
      }, (ctx, vectorKeyframe) -> {
         markStep(ctx, vectorKeyframe, StepFlag.SCALE);
      });
   }).registerBoneUpdater(IPriorityHandler.class, (handler, bone, data) -> {
      IAnimationProperty property = (IAnimationProperty)data[0];
      Vector3f output = bone.getCachedScale();
      Vector3f val = property.getScaleFrame(bone);
      if (!property.isOverride()) {
         if (val == null) {
            val = new Vector3f(1.0F);
         }

         switch(property.getPhase()) {
         case LERPOUT:
            val = TMath.lerp(val, new Vector3f(1.0F), property.getLerpOutRatio());
            break;
         case LERPIN:
            val = TMath.lerp(new Vector3f(1.0F), val, property.getLerpInRatio());
         }

         output.mul(val);
      } else if (val != null) {
         switch(property.getPhase()) {
         case PLAY:
            output.set(val);
            break;
         case LERPOUT:
            output.set(TMath.lerp(val, output, property.getLerpOutRatio()));
            break;
         case LERPIN:
            output.set(TMath.lerp(output, val, property.getLerpInRatio()));
         }
      }

   }).registerBoneUpdater(IStateMachineHandler.class, (handler, bone, data) -> {
      IAnimationProperty currProperty = (IAnimationProperty)data[0];
      if (currProperty != null) {
         Vector3f currVal = new Vector3f(bone.getCachedScale());
         Vector3f valx = currProperty.getScaleFrame(bone);
         if (valx != null) {
            if (!currProperty.isOverride()) {
               currVal.mul(valx);
            } else {
               currVal.set(valx);
            }
         }

         switch(currProperty.getPhase()) {
         case PLAY:
            bone.setCachedScale(currVal);
            return;
         case LERPOUT:
            bone.setCachedScale(TMath.lerp(currVal, bone.getCachedScale(), currProperty.getLerpOutRatio()));
            return;
         default:
            IAnimationProperty lastProperty = (IAnimationProperty)data[1];
            if (lastProperty == null) {
               bone.setCachedScale(TMath.lerp(bone.getCachedScale(), currVal, currProperty.getLerpInRatio()));
            } else {
               Vector3f lastVal = new Vector3f(bone.getCachedScale());
               Vector3f val = lastProperty.getScaleFrame(bone);
               if (val != null) {
                  if (!lastProperty.isOverride()) {
                     lastVal.mul(val);
                  } else {
                     lastVal.set(val);
                  }
               }

               bone.setCachedScale(TMath.lerp(lastVal, currVal, currProperty.getLerpInRatio()));
            }
         }
      }
   }).build();
   public static final KeyframeType<ScriptKeyframe, List<ScriptKeyframe.Script>> SCRIPT = KeyframeType.Builder.of("script", ScriptKeyframe::new).interpolator((blueprintAnimation) -> {
      return new ScriptInterpolator(ArrayList::new, List::addAll);
   }).registerModelUpdater(IPriorityHandler.class, KeyframeTypes::standardScript).registerModelUpdater(IStateMachineHandler.class, KeyframeTypes::standardScript).global().build();

   private static Vector3f standard(KeyframeInterpolator.Context<VectorKeyframe, Vector3f> ctx, Vector3f prev, Vector3f next, float ratio, StepFlag stepFlag) {
      String prevMode = ((VectorKeyframe)ctx.interpolator.get(ctx.prevKey)).getInterpolation();
      if (prevMode.equals("step")) {
         ctx.bone.markStep(stepFlag);
         return prev;
      } else {
         String nextMode = ((VectorKeyframe)ctx.interpolator.get(ctx.nextKey)).getInterpolation();
         if (!prevMode.equals("catmullrom") && !nextMode.equals("catmullrom")) {
            return prev.lerp(next, ratio, new Vector3f());
         } else {
            float nNextKey = ctx.interpolator.getHigherKey(ctx.nextKey);
            float pPrevKey = ctx.interpolator.getLowerKey(ctx.prevKey);
            VectorKeyframe nextControlVector = (VectorKeyframe)ctx.interpolator.get(nNextKey);
            VectorKeyframe lastControlVector = (VectorKeyframe)ctx.interpolator.get(pPrevKey);
            return TMath.smoothLerp(lastControlVector.getValue(0, ctx.property), prev, next, nextControlVector.getValue(0, ctx.property), ratio);
         }
      }
   }

   private static void markStep(KeyframeInterpolator.Context<VectorKeyframe, Vector3f> ctx, VectorKeyframe frame, StepFlag stepFlag) {
      if ("step".equals(frame.getInterpolation())) {
         ctx.bone.markStep(stepFlag);
      }

   }

   private static void standardScript(AnimationHandler handler, ActiveModel model, Object... data) {
      IAnimationProperty property = (IAnimationProperty)data[0];
      List<ScriptKeyframe.Script> scripts = property.getScriptFrame();
      if (scripts != null && !scripts.isEmpty()) {
         Iterator var5 = scripts.iterator();

         while(var5.hasNext()) {
            ScriptKeyframe.Script script = (ScriptKeyframe.Script)var5.next();
            ScriptReader reader = (ScriptReader)ModelEngineAPI.getAPI().getScriptReaderRegistry().get(script.reader());
            if (reader != null) {
               reader.read(property, script.script());
            }
         }

      }
   }
}
