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
import java.util.Iterator;

@MythicMechanic(
   name = "modifystate",
   aliases = {"modifyanimation", "modstate"}
)
public class ModifyStateMechanic implements ITargetedEntitySkill {
   private final MythicLineConfig mlc;
   private final PlaceholderString modelId;
   private final PlaceholderString state;
   private final PlaceholderInt priority;

   public ModifyStateMechanic(MythicLineConfig mlc) {
      this.mlc = mlc;
      this.modelId = mlc.getPlaceholderString(new String[]{"m", "mid", "model", "modelid"}, (String)null, new String[0]);
      this.state = mlc.getPlaceholderString(new String[]{"s", "state"}, (String)null, new String[0]);
      this.priority = mlc.getPlaceholderInteger(new String[]{"p", "pr", "priority"}, 1, new String[0]);
   }

   public SkillResult castAtEntity(SkillMetadata meta, AbstractEntity target) {
      ModeledEntity model = ModelEngineAPI.getModeledEntity(target.getUniqueId());
      if (model == null) {
         return SkillResult.CONDITION_FAILED;
      } else {
         String modelId = MythicUtils.getOrNullLowercase(this.modelId, meta, target);
         String state = MythicUtils.getOrNullLowercase(this.state, meta, target);
         model.getModel(modelId).ifPresentOrElse((activeModel) -> {
            this.configureAnimation(state, activeModel, meta, target);
         }, () -> {
            Iterator var5 = model.getModels().values().iterator();

            while(var5.hasNext()) {
               ActiveModel activeModel = (ActiveModel)var5.next();
               this.configureAnimation(state, activeModel, meta, target);
            }

         });
         return SkillResult.SUCCESS;
      }
   }

   private void configureAnimation(String state, ActiveModel activeModel, SkillMetadata meta, AbstractEntity target) {
      AnimationHandler handler = activeModel.getAnimationHandler();
      IAnimationProperty property;
      if (handler instanceof IStateMachineHandler) {
         IStateMachineHandler stateMachineHandler = (IStateMachineHandler)handler;
         int priority = this.priority.get(meta, target);
         property = stateMachineHandler.getAnimation(priority, state);
      } else {
         property = handler.getAnimation(state);
      }

      if (property != null) {
         PlaceholderDouble speedPlaceholder = this.mlc.getPlaceholderDouble(new String[]{"sp", "speed"}, property.getSpeed(), new String[0]);
         PlaceholderInt lerpInPlaceholder = this.mlc.getPlaceholderInteger(new String[]{"li", "lerpin"}, (int)(property.getLerpIn() * 20.0D), new String[0]);
         PlaceholderInt lerpOutPlaceholder = this.mlc.getPlaceholderInteger(new String[]{"lo", "lerpout"}, (int)(property.getLerpOut() * 20.0D), new String[0]);
         BlueprintAnimation.LoopMode loopMode = property.getForceLoopMode();
         PlaceholderString loopModePlaceholder = this.mlc.getPlaceholderString(new String[]{"l", "loop"}, loopMode == null ? null : loopMode.name(), new String[0]);
         loopMode = BlueprintAnimation.LoopMode.getOrNull(MythicUtils.getOrNull(loopModePlaceholder, meta, target));
         boolean override = this.mlc.getBoolean(new String[]{"ov", "override"}, property.isForceOverride());
         property.setSpeed(speedPlaceholder.get(meta, target));
         property.setLerpInTime((double)lerpInPlaceholder.get(meta, target) * 0.05D);
         property.setLerpOutTime((double)lerpOutPlaceholder.get(meta, target) * 0.05D);
         property.setForceLoopMode(loopMode);
         property.setForceOverride(override);
      }
   }
}
