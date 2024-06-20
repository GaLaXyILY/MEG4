package com.ticxo.modelengine.core.mythic.mechanics.mounting;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.model.bone.manager.MountManager;
import com.ticxo.modelengine.core.mythic.MythicUtils;
import com.ticxo.modelengine.core.mythic.utils.MythicMechanic;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;

@MythicMechanic(
   name = "dismountallmodel",
   aliases = {"dismountall"}
)
public class DismountAllMechanic implements ITargetedEntitySkill {
   private final PlaceholderString modelId;
   private final PlaceholderString pBone;

   public DismountAllMechanic(MythicLineConfig mlc) {
      this.modelId = mlc.getPlaceholderString(new String[]{"m", "mid", "model", "modelid"}, (String)null, new String[0]);
      this.pBone = mlc.getPlaceholderString(new String[]{"p", "pbone", "seat"}, (String)null, new String[0]);
   }

   public SkillResult castAtEntity(SkillMetadata meta, AbstractEntity target) {
      ModeledEntity model = ModelEngineAPI.getModeledEntity(target.getUniqueId());
      if (model == null) {
         return SkillResult.CONDITION_FAILED;
      } else {
         String modelId = MythicUtils.getOrNullLowercase(this.modelId, meta, target);
         if (modelId == null) {
            return SkillResult.INVALID_CONFIG;
         } else {
            model.getModel(modelId).ifPresent((activeModel) -> {
               activeModel.getMountManager().ifPresent((mountManager) -> {
                  String parts = MythicUtils.getOrNullLowercase(this.pBone, meta, target);
                  if (parts != null) {
                     String[] var5 = parts.split(",");
                     int var6 = var5.length;

                     for(int var7 = 0; var7 < var6; ++var7) {
                        String seat = var5[var7];
                        ((MountManager)mountManager).dismountPassengers(seat);
                     }
                  } else {
                     ((MountManager)mountManager).dismountAll();
                  }

               });
            });
            return SkillResult.SUCCESS;
         }
      }
   }
}
