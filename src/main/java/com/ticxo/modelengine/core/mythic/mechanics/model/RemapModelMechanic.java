package com.ticxo.modelengine.core.mythic.mechanics.model;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.generator.blueprint.BlueprintBone;
import com.ticxo.modelengine.api.generator.blueprint.ModelBlueprint;
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
import java.util.Iterator;
import java.util.Set;

@MythicMechanic(
   name = "remapmodel",
   aliases = {"remap"}
)
public class RemapModelMechanic implements ITargetedEntitySkill {
   private final PlaceholderString modelId;
   private final PlaceholderString newModelId;
   private final PlaceholderString map;

   public RemapModelMechanic(MythicLineConfig mlc) {
      this.modelId = mlc.getPlaceholderString(new String[]{"m", "mid", "model", "modelid"}, (String)null, new String[0]);
      this.newModelId = mlc.getPlaceholderString(new String[]{"n", "nid", "newmodel", "newmodelid"}, (String)null, new String[0]);
      this.map = mlc.getPlaceholderString(new String[]{"map"}, (String)null, new String[0]);
   }

   public SkillResult castAtEntity(SkillMetadata meta, AbstractEntity target) {
      ModeledEntity model = ModelEngineAPI.getModeledEntity(target.getUniqueId());
      if (model == null) {
         return SkillResult.CONDITION_FAILED;
      } else {
         String modelId = MythicUtils.getOrNullLowercase(this.modelId, meta, target);
         ActiveModel activeModel = MythicUtils.getActiveModelOrNull(model, modelId);
         if (activeModel == null) {
            return SkillResult.INVALID_CONFIG;
         } else {
            String newModelId = MythicUtils.getOrNullLowercase(this.newModelId, meta, target);
            ModelBlueprint blueprint = MythicUtils.getBlueprintOrNull(newModelId);
            if (blueprint == null) {
               return SkillResult.INVALID_CONFIG;
            } else {
               if (this.map != null) {
                  String mapId = MythicUtils.getOrNullLowercase(this.map, meta, target);
                  ModelBlueprint mapBlueprint = MythicUtils.getBlueprintOrNull(mapId);
                  if (mapBlueprint == null) {
                     return SkillResult.INVALID_CONFIG;
                  }

                  Iterator var10 = mapBlueprint.getFlatMap().keySet().iterator();

                  while(var10.hasNext()) {
                     String bone = (String)var10.next();
                     BlueprintBone blueprintBone = (BlueprintBone)blueprint.getFlatMap().get(bone);
                     if (blueprintBone != null && blueprintBone.isRenderer()) {
                        activeModel.getBone(bone).ifPresent((replaced) -> {
                           if (replaced.isRenderer()) {
                              replaced.setModel(blueprintBone.getDataId());
                           }

                        });
                     }
                  }
               } else {
                  Set<String> bones = blueprint.getFlatMap().size() < activeModel.getBones().size() ? blueprint.getFlatMap().keySet() : activeModel.getBones().keySet();
                  Iterator var16 = bones.iterator();

                  while(var16.hasNext()) {
                     String bone = (String)var16.next();
                     BlueprintBone blueprintBone = (BlueprintBone)blueprint.getFlatMap().get(bone);
                     if (blueprintBone != null && blueprintBone.isRenderer()) {
                        activeModel.getBone(bone).ifPresent((replaced) -> {
                           if (replaced.isRenderer()) {
                              replaced.setModelScale(blueprintBone.getScale());
                              replaced.setModel(blueprintBone.getDataId());
                           }

                        });
                     }
                  }
               }

               return null;
            }
         }
      }
   }
}
