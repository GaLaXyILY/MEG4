package com.ticxo.modelengine.api.animation.property;

import com.ticxo.modelengine.api.animation.BlueprintAnimation;
import com.ticxo.modelengine.api.animation.Timeline;
import com.ticxo.modelengine.api.animation.handler.AnimationHandler;
import com.ticxo.modelengine.api.animation.keyframe.KeyframeType;
import com.ticxo.modelengine.api.animation.keyframe.type.ScriptKeyframe;
import com.ticxo.modelengine.api.generator.blueprint.ModelBlueprint;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.bone.ModelBone;
import com.ticxo.modelengine.api.utils.data.io.SavedData;
import com.ticxo.modelengine.api.utils.math.TMath;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class SimpleProperty implements IAnimationProperty {
   private final ActiveModel model;
   private final BlueprintAnimation blueprintAnimation;
   private final double lerpIn;
   private final double lerpOut;
   private double lerpInTime;
   private double lerpOutTime;
   private double lastTime;
   private double time;
   private double speed;
   @NotNull
   private IAnimationProperty.Phase phase;
   private BlueprintAnimation.LoopMode forceLoopMode;
   private boolean forceOverride;
   private boolean stopping;
   private boolean ended;

   public SimpleProperty(ActiveModel model, BlueprintAnimation blueprintAnimation) {
      this(model, blueprintAnimation, 0.0D, 0.0D, 1.0D);
   }

   public SimpleProperty(ActiveModel model, BlueprintAnimation blueprintAnimation, double lerpIn, double lerpOut, double speed) {
      this.lerpInTime = 0.0D;
      this.lerpOutTime = 0.0D;
      this.lastTime = -1.0D;
      this.time = -1.0D;
      this.phase = IAnimationProperty.Phase.LERPIN;
      this.forceLoopMode = null;
      this.forceOverride = false;
      this.model = model;
      this.blueprintAnimation = blueprintAnimation;
      this.lerpIn = lerpIn;
      this.lerpOut = lerpOut;
      this.speed = speed;
   }

   public boolean update() {
      this.lastTime = this.time;
      boolean var10000;
      switch(this.phase) {
      case LERPIN:
         var10000 = this.updateLerpIn();
         break;
      case PLAY:
         var10000 = this.updateTime();
         break;
      case LERPOUT:
         var10000 = this.updateLerpOut();
         break;
      default:
         throw new IncompatibleClassChangeError();
      }

      return var10000;
   }

   private boolean updateLerpIn() {
      if (this.lerpInTime >= this.lerpIn - 1.0E-5D) {
         this.time = 0.0D;
         return this.stopping ? this.updateLerpOut() : this.updateTime();
      } else {
         this.lerpInTime += this.speed * 0.05D;
         return this.playingOrLerpOut();
      }
   }

   private boolean updateTime() {
      if (this.phase == IAnimationProperty.Phase.LERPIN) {
         this.phase = IAnimationProperty.Phase.PLAY;
         return this.playingOrLerpOut();
      } else {
         BlueprintAnimation.LoopMode mode = this.getLoopMode();
         switch(mode) {
         case ONCE:
            if (this.time < this.blueprintAnimation.getLength()) {
               this.time = Math.min(this.time + this.speed * 0.05D, this.blueprintAnimation.getLength());
               return this.playingOrLerpOut();
            }

            return this.updateLerpOut();
         case HOLD:
            this.time = Math.min(this.time + this.speed * 0.05D, this.blueprintAnimation.getLength());
            return this.playingOrLerpOut();
         case LOOP:
            this.time = (this.time + this.speed * 0.05D) % (this.blueprintAnimation.getLength() + 0.05D);
            return this.playingOrLerpOut();
         default:
            return false;
         }
      }
   }

   private boolean updateLerpOut() {
      if (this.phase != IAnimationProperty.Phase.LERPOUT && this.lerpOut > 1.0E-5D) {
         this.phase = IAnimationProperty.Phase.LERPOUT;
         return true;
      } else if (this.lerpOutTime >= this.lerpOut - 1.0E-5D) {
         this.ended = true;
         return false;
      } else {
         this.lerpOutTime += this.speed * 0.05D;
         return true;
      }
   }

   private boolean playingOrLerpOut() {
      return !this.stopping || this.updateLerpOut();
   }

   public void stop() {
      this.stopping = true;
   }

   public boolean canReplace() {
      return this.stopping || this.phase == IAnimationProperty.Phase.LERPOUT || this.ended;
   }

   public String getName() {
      return this.blueprintAnimation.getName();
   }

   public boolean containsKeyframe(KeyframeType<?, ?> type, String bone) {
      Timeline timeline = (Timeline)this.blueprintAnimation.getTimelines().get(bone);
      if (timeline == null) {
         return false;
      } else {
         return timeline.hasInterpolator(type) && !timeline.getInterpolator(type).isEmpty();
      }
   }

   public Vector3f getPositionFrame(ModelBone bone) {
      return this.blueprintAnimation.getPosition(bone, this);
   }

   public Vector3f getRotationFrame(ModelBone bone) {
      return this.blueprintAnimation.getRotation(bone, this);
   }

   public Vector3f getScaleFrame(ModelBone bone) {
      return this.blueprintAnimation.getScale(bone, this);
   }

   public List<ScriptKeyframe.Script> getScriptFrame() {
      return this.blueprintAnimation.getScript(this);
   }

   public double getLerpInRatio() {
      return TMath.clamp(this.lerpInTime / this.lerpIn, 0.0D, 1.0D);
   }

   public double getLerpOutRatio() {
      return TMath.clamp(this.lerpOutTime / this.lerpOut, 0.0D, 1.0D);
   }

   public boolean isFinished() {
      return this.phase == IAnimationProperty.Phase.LERPOUT || this.time >= this.blueprintAnimation.getLength();
   }

   public BlueprintAnimation.LoopMode getLoopMode() {
      return this.forceLoopMode == null ? this.blueprintAnimation.getLoopMode() : this.forceLoopMode;
   }

   public boolean isOverride() {
      return this.blueprintAnimation.isOverride() || this.forceOverride;
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.lerpIn, this.lerpOut, this.time, this.speed, this.phase, this.forceLoopMode, this.forceOverride});
   }

   public void save(SavedData data) {
      data.putString("id", "simple");
      data.putString("name", this.getName());
      data.putDouble("lerp_in", this.lerpIn);
      data.putDouble("lerp_out", this.lerpOut);
      data.putDouble("lerp_in_time", this.lerpInTime);
      data.putDouble("lerp_out_time", this.lerpOutTime);
      data.putDouble("last_time", this.lastTime);
      data.putDouble("time", this.time);
      data.putDouble("speed", this.speed);
      data.putString("phase", this.phase.name());
      if (this.forceLoopMode != null) {
         data.putString("force_loop_mode", this.forceLoopMode.name());
      }

      data.putBoolean("force_override", this.forceOverride);
   }

   public void load(SavedData data) {
      this.lerpInTime = data.getDouble("lerp_in_time");
      this.lerpOutTime = data.getDouble("lerp_out_time");
      this.lastTime = data.getDouble("last_time");
      this.time = data.getDouble("time");
      this.phase = IAnimationProperty.Phase.valueOf(data.getString("phase"));
      data.loadIfExist("force_loop_mode", SavedData::getString, (val) -> {
         this.forceLoopMode = BlueprintAnimation.LoopMode.getOrNull(val);
      });
      this.forceOverride = data.getBoolean("force_override");
   }

   public static SimpleProperty create(AnimationHandler handler, SavedData data) {
      ActiveModel model = handler.getActiveModel();
      ModelBlueprint blueprint = model.getBlueprint();
      BlueprintAnimation animation = (BlueprintAnimation)blueprint.getAnimations().get(data.getString("name"));
      SimpleProperty property = new SimpleProperty(model, animation, data.getDouble("lerp_in", 0.0D), data.getDouble("lerp_out", 0.0D), data.getDouble("speed", 1.0D));
      property.load(data);
      return property;
   }

   public String toString() {
      ActiveModel var10000 = this.getModel();
      return "SimpleProperty(model=" + var10000 + ", blueprintAnimation=" + this.getBlueprintAnimation() + ", lerpIn=" + this.getLerpIn() + ", lerpOut=" + this.getLerpOut() + ", lerpInTime=" + this.getLerpInTime() + ", lerpOutTime=" + this.getLerpOutTime() + ", lastTime=" + this.getLastTime() + ", time=" + this.getTime() + ", speed=" + this.getSpeed() + ", phase=" + this.getPhase() + ", forceLoopMode=" + this.getForceLoopMode() + ", forceOverride=" + this.isForceOverride() + ", stopping=" + this.isStopping() + ", ended=" + this.isEnded() + ")";
   }

   public ActiveModel getModel() {
      return this.model;
   }

   public BlueprintAnimation getBlueprintAnimation() {
      return this.blueprintAnimation;
   }

   public double getLerpIn() {
      return this.lerpIn;
   }

   public double getLerpOut() {
      return this.lerpOut;
   }

   public double getLerpInTime() {
      return this.lerpInTime;
   }

   public double getLerpOutTime() {
      return this.lerpOutTime;
   }

   public double getLastTime() {
      return this.lastTime;
   }

   public double getTime() {
      return this.time;
   }

   public double getSpeed() {
      return this.speed;
   }

   @NotNull
   public IAnimationProperty.Phase getPhase() {
      return this.phase;
   }

   public BlueprintAnimation.LoopMode getForceLoopMode() {
      return this.forceLoopMode;
   }

   public boolean isForceOverride() {
      return this.forceOverride;
   }

   public boolean isStopping() {
      return this.stopping;
   }

   public boolean isEnded() {
      return this.ended;
   }

   public void setLerpInTime(double lerpInTime) {
      this.lerpInTime = lerpInTime;
   }

   public void setLerpOutTime(double lerpOutTime) {
      this.lerpOutTime = lerpOutTime;
   }

   public void setSpeed(double speed) {
      this.speed = speed;
   }

   public void setPhase(@NotNull IAnimationProperty.Phase phase) {
      this.phase = phase;
   }

   public void setForceLoopMode(BlueprintAnimation.LoopMode forceLoopMode) {
      this.forceLoopMode = forceLoopMode;
   }

   public void setForceOverride(boolean forceOverride) {
      this.forceOverride = forceOverride;
   }
}
