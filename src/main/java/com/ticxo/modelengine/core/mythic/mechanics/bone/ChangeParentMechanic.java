package com.ticxo.modelengine.core.mythic.mechanics.bone;

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
   name = "changeparent",
   aliases = {}
)
public class ChangeParentMechanic implements ITargetedEntitySkill {
   private final PlaceholderString modelId;
   private final PlaceholderString parentPart;
   private final PlaceholderString childPart;

   public ChangeParentMechanic(MythicLineConfig mlc) {
      this.modelId = mlc.getPlaceholderString(new String[]{"m", "mid", "model", "modelid"}, (String)null, new String[0]);
      this.parentPart = mlc.getPlaceholderString(new String[]{"p", "parent"}, (String)null, new String[0]);
      this.childPart = mlc.getPlaceholderString(new String[]{"c", "child"}, (String)null, new String[0]);
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
            String parentPart = MythicUtils.getOrNullLowercase(this.parentPart, meta, target);
            String childPart = MythicUtils.getOrNullLowercase(this.childPart, meta, target);
            activeModel.getBone(parentPart).ifPresent((parent) -> {
               activeModel.getBone(childPart).ifPresent((child) -> {
                  child.setParent(parent);
               });
            });
            return SkillResult.SUCCESS;
         }
      }
   }
}
