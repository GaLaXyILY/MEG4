package com.ticxo.modelengine.core.mythic.mechanics.model;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.core.mythic.MythicUtils;
import com.ticxo.modelengine.core.mythic.utils.MythicMechanic;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;

@MythicMechanic(
   name = "rendererinit",
   aliases = {"renderinit"}
)
public class RendererInitMechanic implements ITargetedEntitySkill {
   private final PlaceholderString modelId;

   public RendererInitMechanic(MythicLineConfig mlc) {
      this.modelId = mlc.getPlaceholderString(new String[]{"m", "mid", "model", "modelid"}, (String)null, new String[0]);
   }

   public SkillResult castAtEntity(SkillMetadata meta, AbstractEntity target) {
      ModeledEntity model = ModelEngineAPI.getModeledEntity(target.getUniqueId());
      if (model == null) {
         return SkillResult.CONDITION_FAILED;
      } else {
         String modelId = MythicUtils.getOrNullLowercase(this.modelId, meta, target);
         ActiveModel activeModel = MythicUtils.getActiveModelOrNull(model, modelId);
         if (activeModel == null) {
            return SkillResult.CONDITION_FAILED;
         } else {
            activeModel.initializeRenderer();
            return SkillResult.SUCCESS;
         }
      }
   }
}
