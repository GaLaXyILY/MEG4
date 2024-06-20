package com.ticxo.modelengine.core.mythic.mechanics.bone;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.generator.blueprint.BlueprintBone;
import com.ticxo.modelengine.api.generator.blueprint.ModelBlueprint;
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
   name = "changepart",
   aliases = {}
)
public class ChangePartMechanic implements ITargetedEntitySkill {
   private final PlaceholderString modelId;
   private final PlaceholderString partId;
   private final PlaceholderString nModelId;
   private final PlaceholderString nPartId;

   public ChangePartMechanic(MythicLineConfig mlc) {
      this.modelId = mlc.getPlaceholderString(new String[]{"m", "mid", "model", "modelid"}, (String)null, new String[0]);
      this.partId = mlc.getPlaceholderString(new String[]{"p", "pid", "part", "partid"}, (String)null, new String[0]);
      this.nModelId = mlc.getPlaceholderString(new String[]{"nm", "nmid", "newmodel", "newmodelid"}, (String)null, new String[0]);
      this.nPartId = mlc.getPlaceholderString(new String[]{"np", "npid", "newpart", "newpartid"}, (String)null, new String[0]);
   }

   public SkillResult castAtEntity(SkillMetadata meta, AbstractEntity target) {
      ModeledEntity model = ModelEngineAPI.getModeledEntity(target.getUniqueId());
      if (model == null) {
         return SkillResult.CONDITION_FAILED;
      } else {
         String modelId = MythicUtils.getOrNullLowercase(this.modelId, meta, target);
         String partId = MythicUtils.getOrNullLowercase(this.partId, meta, target);
         String nModelId = MythicUtils.getOrNullLowercase(this.nModelId, meta, target);
         String nPartId = MythicUtils.getOrNullLowercase(this.nPartId, meta, target);
         if (modelId != null && partId != null && nModelId != null && nPartId != null) {
            model.getModel(modelId).flatMap((activeModel) -> {
               return activeModel.getBone(partId);
            }).ifPresent((bone) -> {
               if (bone.isRenderer()) {
                  ModelBlueprint blueprint = ModelEngineAPI.getBlueprint(nModelId);
                  if (blueprint != null) {
                     BlueprintBone blueprintBone = (BlueprintBone)blueprint.getFlatMap().get(nPartId);
                     if (blueprintBone != null && blueprintBone.isRenderer()) {
                        bone.setModelScale(blueprintBone.getScale());
                        bone.setModel(blueprintBone.getDataId());
                     }
                  }
               }
            });
            return SkillResult.SUCCESS;
         } else {
            return SkillResult.INVALID_CONFIG;
         }
      }
   }
}
