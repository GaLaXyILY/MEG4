package com.ticxo.modelengine.core.mythic.mechanics.model;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.core.mythic.MythicUtils;
import com.ticxo.modelengine.core.mythic.utils.MythicMechanic;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;

@MythicMechanic(
   name = "scale",
   aliases = {}
)
public class ScaleMechanic implements ITargetedEntitySkill {
   private final PlaceholderString modelId;
   private final PlaceholderDouble scale;
   private final boolean isHitbox;

   public ScaleMechanic(MythicLineConfig mlc) {
      this.modelId = mlc.getPlaceholderString(new String[]{"m", "mid", "model", "modelid"}, (String)null, new String[0]);
      this.scale = mlc.getPlaceholderDouble(new String[]{"s", "scale"}, 1.0D, new String[0]);
      this.isHitbox = mlc.getBoolean(new String[]{"h", "hitbox"}, false);
   }

   public SkillResult castAtEntity(SkillMetadata meta, AbstractEntity target) {
      ModeledEntity model = ModelEngineAPI.getModeledEntity(target.getUniqueId());
      if (model == null) {
         return SkillResult.CONDITION_FAILED;
      } else {
         double scale = this.scale.get(meta, target);
         String modelId = MythicUtils.getOrNullLowercase(this.modelId, meta, target);
         MythicUtils.executeOptModelId(model, modelId, (activeModel) -> {
            if (this.isHitbox) {
               activeModel.setHitboxScale(scale);
            } else {
               activeModel.setScale(scale);
            }

         });
         return SkillResult.SUCCESS;
      }
   }
}
