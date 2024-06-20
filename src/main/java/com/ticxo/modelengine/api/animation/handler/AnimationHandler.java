package com.ticxo.modelengine.api.animation.handler;

import com.ticxo.modelengine.api.animation.BlueprintAnimation;
import com.ticxo.modelengine.api.animation.ModelState;
import com.ticxo.modelengine.api.animation.property.IAnimationProperty;
import com.ticxo.modelengine.api.animation.property.SimpleProperty;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.bone.ModelBone;
import com.ticxo.modelengine.api.utils.data.io.DataIO;
import com.ticxo.modelengine.api.utils.data.io.SavedData;
import java.util.Iterator;
import java.util.Map;
import org.jetbrains.annotations.Nullable;

public interface AnimationHandler extends DataIO {
   ActiveModel getActiveModel();

   void prepare();

   void updateBone(ModelBone var1);

   boolean hasFinishedAllAnimations();

   void setDefaultProperty(AnimationHandler.DefaultProperty var1);

   AnimationHandler.DefaultProperty getDefaultProperty(ModelState var1);

   void tickGlobal();

   @Nullable
   IAnimationProperty playAnimation(String var1, double var2, double var4, double var6, boolean var8);

   boolean playAnimation(IAnimationProperty var1, boolean var2);

   boolean isPlayingAnimation(String var1);

   void stopAnimation(String var1);

   void forceStopAnimation(String var1);

   void forceStopAllAnimations();

   @Nullable
   IAnimationProperty getAnimation(String var1);

   Map<String, IAnimationProperty> getAnimations();

   String getId();

   default void save(SavedData data) {
      SavedData defaultData = new SavedData();
      ModelState[] var3 = ModelState.values();
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         ModelState state = var3[var5];
         AnimationHandler.DefaultProperty property = this.getDefaultProperty(state);
         SavedData propertyData = new SavedData();
         propertyData.putDouble("lerp_in", property.lerpIn);
         propertyData.putDouble("lerp_out", property.lerpOut);
         propertyData.putDouble("speed", property.speed);
         defaultData.putData(state.name(), propertyData);
      }

      data.putData("defaults", defaultData);
      data.putString("id", this.getId());
   }

   default void load(SavedData data) {
      data.getData("defaults").ifPresent((defaultData) -> {
         Iterator var2 = defaultData.keySet().iterator();

         while(var2.hasNext()) {
            String key = (String)var2.next();
            ModelState state = ModelState.get(key);
            defaultData.getData(key).ifPresent((propertyData) -> {
               this.setDefaultProperty(new AnimationHandler.DefaultProperty(state, propertyData.getDouble("lerp_in"), propertyData.getDouble("lerp_out"), propertyData.getDouble("speed")));
            });
         }

      });
   }

   public static class DefaultProperty {
      private final ModelState state;
      private final String animation;
      private final double lerpIn;
      private final double lerpOut;
      private final double speed;

      public DefaultProperty(ModelState state, double lerpIn, double lerpOut, double speed) {
         this(state, state.getString(), lerpIn, lerpOut, speed);
      }

      public DefaultProperty(ModelState state, String animation, double lerpIn, double lerpOut, double speed) {
         this.state = state;
         this.animation = animation;
         this.lerpIn = lerpIn;
         this.lerpOut = lerpOut;
         this.speed = speed;
      }

      public IAnimationProperty build(ActiveModel model) {
         return this.build(model, this.lerpIn, this.lerpOut, this.speed);
      }

      public IAnimationProperty build(ActiveModel model, double lerpIn, double lerpOut, double speed) {
         BlueprintAnimation blueprintAnimation = (BlueprintAnimation)model.getBlueprint().getAnimations().get(this.animation);
         return blueprintAnimation == null ? null : new SimpleProperty(model, blueprintAnimation, lerpIn, lerpOut, speed);
      }

      public ModelState getState() {
         return this.state;
      }

      public String getAnimation() {
         return this.animation;
      }

      public double getLerpIn() {
         return this.lerpIn;
      }

      public double getLerpOut() {
         return this.lerpOut;
      }

      public double getSpeed() {
         return this.speed;
      }

      public boolean equals(Object o) {
         if (o == this) {
            return true;
         } else if (!(o instanceof AnimationHandler.DefaultProperty)) {
            return false;
         } else {
            AnimationHandler.DefaultProperty other = (AnimationHandler.DefaultProperty)o;
            if (!other.canEqual(this)) {
               return false;
            } else if (Double.compare(this.getLerpIn(), other.getLerpIn()) != 0) {
               return false;
            } else if (Double.compare(this.getLerpOut(), other.getLerpOut()) != 0) {
               return false;
            } else if (Double.compare(this.getSpeed(), other.getSpeed()) != 0) {
               return false;
            } else {
               Object this$state = this.getState();
               Object other$state = other.getState();
               if (this$state == null) {
                  if (other$state != null) {
                     return false;
                  }
               } else if (!this$state.equals(other$state)) {
                  return false;
               }

               Object this$animation = this.getAnimation();
               Object other$animation = other.getAnimation();
               if (this$animation == null) {
                  if (other$animation != null) {
                     return false;
                  }
               } else if (!this$animation.equals(other$animation)) {
                  return false;
               }

               return true;
            }
         }
      }

      protected boolean canEqual(Object other) {
         return other instanceof AnimationHandler.DefaultProperty;
      }

      public int hashCode() {
         int PRIME = true;
         int result = 1;
         long $lerpIn = Double.doubleToLongBits(this.getLerpIn());
         int result = result * 59 + (int)($lerpIn >>> 32 ^ $lerpIn);
         long $lerpOut = Double.doubleToLongBits(this.getLerpOut());
         result = result * 59 + (int)($lerpOut >>> 32 ^ $lerpOut);
         long $speed = Double.doubleToLongBits(this.getSpeed());
         result = result * 59 + (int)($speed >>> 32 ^ $speed);
         Object $state = this.getState();
         result = result * 59 + ($state == null ? 43 : $state.hashCode());
         Object $animation = this.getAnimation();
         result = result * 59 + ($animation == null ? 43 : $animation.hashCode());
         return result;
      }

      public String toString() {
         ModelState var10000 = this.getState();
         return "AnimationHandler.DefaultProperty(state=" + var10000 + ", animation=" + this.getAnimation() + ", lerpIn=" + this.getLerpIn() + ", lerpOut=" + this.getLerpOut() + ", speed=" + this.getSpeed() + ")";
      }
   }
}
