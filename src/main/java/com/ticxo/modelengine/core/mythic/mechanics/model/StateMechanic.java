package com.ticxo.modelengine.core.mythic.mechanics.model;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.animation.BlueprintAnimation;
import com.ticxo.modelengine.api.animation.handler.AnimationHandler;
import com.ticxo.modelengine.api.animation.handler.IStateMachineHandler;
import com.ticxo.modelengine.api.animation.property.IAnimationProperty;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.core.mythic.MythicUtils;
import com.ticxo.modelengine.core.mythic.utils.MythicMechanic;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import io.lumine.mythic.api.skills.placeholders.PlaceholderInt;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;

@MythicMechanic(
   name = "state",
   aliases = {"animation"}
)
public class StateMechanic implements ITargetedEntitySkill {
   private final PlaceholderString modelId;
   private final PlaceholderString state;
   private final PlaceholderString forceLoopMode;
   private final boolean remove;
   private final boolean ignoreLerp;
   private final boolean force;
   private final PlaceholderInt lerpIn;
   private final PlaceholderInt lerpOut;
   private final PlaceholderInt priority;
   private final PlaceholderDouble speed;
   private final Boolean forceOverride;

   public StateMechanic(MythicLineConfig mlc) {
      this.modelId = mlc.getPlaceholderString(new String[]{"m", "mid", "model", "modelid"}, (String)null, new String[0]);
      this.state = mlc.getPlaceholderString(new String[]{"s", "state"}, (String)null, new String[0]);
      this.remove = mlc.getBoolean(new String[]{"r", "remove"}, false);
      this.speed = mlc.getPlaceholderDouble(new String[]{"sp", "speed"}, 1.0D, new String[0]);
      this.lerpIn = mlc.getPlaceholderInteger(new String[]{"li", "lerpin"}, 0, new String[0]);
      this.lerpOut = mlc.getPlaceholderInteger(new String[]{"lo", "lerpout"}, 0, new String[0]);
      this.ignoreLerp = mlc.getBoolean(new String[]{"i", "ignorelerp"}, false);
      this.force = mlc.getBoolean(new String[]{"f", "force"}, true);
      this.priority = mlc.getPlaceholderInteger(new String[]{"p", "pr", "priority"}, 1, new String[0]);
      this.forceLoopMode = mlc.getPlaceholderString(new String[]{"l", "loop"}, (String)null, new String[0]);
      String override = mlc.getString(new String[]{"ov", "override"}, (String)null, new String[0]);
      this.forceOverride = override == null ? null : Boolean.parseBoolean(override);
   }

   public SkillResult castAtEntity(SkillMetadata meta, AbstractEntity target) {
      return this.remove ? this.removeAnimation(meta, target) : this.addAnimation(meta, target);
   }

   private SkillResult removeAnimation(SkillMetadata meta, AbstractEntity target) {
      ModeledEntity model = ModelEngineAPI.getModeledEntity(target.getUniqueId());
      if (model == null) {
         return SkillResult.CONDITION_FAILED;
      } else {
         String state = MythicUtils.getOrNullLowercase(this.state, meta, target);
         if (state == null) {
            return SkillResult.INVALID_CONFIG;
         } else {
            String modelId = MythicUtils.getOrNullLowercase(this.modelId, meta, target);
            MythicUtils.executeOptModelId(model, modelId, (activeModel) -> {
               this.removeAnimation(activeModel, state, meta, target);
            });
            return SkillResult.SUCCESS;
         }
      }
   }

   private void removeAnimation(ActiveModel activeModel, String state, SkillMetadata meta, AbstractEntity target) {
      AnimationHandler handler = activeModel.getAnimationHandler();
      if (handler instanceof IStateMachineHandler) {
         IStateMachineHandler stateMachineHandler = (IStateMachineHandler)handler;
         int priority = this.priority.get(meta, target);
         if (this.ignoreLerp) {
            stateMachineHandler.forceStopAnimation(priority, state);
         } else {
            stateMachineHandler.stopAnimation(priority, state);
         }
      } else if (this.ignoreLerp) {
         handler.forceStopAnimation(state);
      } else {
         handler.stopAnimation(state);
      }

   }

   private SkillResult addAnimation(SkillMetadata meta, AbstractEntity target) {
      ModeledEntity model = ModelEngineAPI.getModeledEntity(target.getUniqueId());
      if (model == null) {
         return SkillResult.CONDITION_FAILED;
      } else {
         String state = MythicUtils.getOrNullLowercase(this.state, meta, target);
         if (state == null) {
            return SkillResult.INVALID_CONFIG;
         } else {
            String modelId = MythicUtils.getOrNullLowercase(this.modelId, meta, target);
            MythicUtils.executeOptModelId(model, modelId, (activeModel) -> {
               this.addAnimation(activeModel, state, meta, target);
            });
            return SkillResult.SUCCESS;
         }
      }
   }

   private void addAnimation(ActiveModel activeModel, String state, SkillMetadata meta, AbstractEntity target) {
      int lerpIn = this.lerpIn.get(meta, target);
      int lerpOut = this.lerpOut.get(meta, target);
      double speed = this.speed.get(meta, target);
      BlueprintAnimation.LoopMode loopMode = BlueprintAnimation.LoopMode.getOrNull(MythicUtils.getOrNull(this.forceLoopMode, meta, target));
      AnimationHandler handler = activeModel.getAnimationHandler();
      if (handler instanceof IStateMachineHandler) {
         IStateMachineHandler stateMachineHandler = (IStateMachineHandler)handler;
         int priority = this.priority.get(meta, target);
         IAnimationProperty property = stateMachineHandler.playAnimation(priority, state, (double)lerpIn * 0.05D, (double)lerpOut * 0.05D, speed, this.force);
         if (property != null) {
            property.setForceLoopMode(loopMode);
            if (this.forceOverride != null) {
               property.setForceOverride(this.forceOverride);
            }
         }
      } else {
         IAnimationProperty property = handler.playAnimation(state, (double)lerpIn * 0.05D, (double)lerpOut * 0.05D, speed, this.force);
         if (property != null) {
            property.setForceLoopMode(loopMode);
            if (this.forceOverride != null) {
               property.setForceOverride(this.forceOverride);
            }
         }
      }

   }
}
