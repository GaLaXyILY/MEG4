package com.ticxo.modelengine.core.mythic.mechanics.leash;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.model.bone.manager.LeashManager;
import com.ticxo.modelengine.core.mythic.MythicUtils;
import com.ticxo.modelengine.core.mythic.utils.MythicMechanic;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;

@MythicMechanic(
   name = "leash",
   aliases = {}
)
public class LeashMechanic implements ITargetedEntitySkill {
   private final PlaceholderString modelId;
   private final PlaceholderString sourceId;
   private final boolean flag;

   public LeashMechanic(MythicLineConfig mlc) {
      this.modelId = mlc.getPlaceholderString(new String[]{"m", "mid", "model", "modelid"}, (String)null, new String[0]);
      this.sourceId = mlc.getPlaceholderString(new String[]{"p", "pid", "part", "partid"}, (String)null, new String[0]);
      this.flag = mlc.getBoolean(new String[]{"l", "leash"}, true);
   }

   public SkillResult castAtEntity(SkillMetadata meta, AbstractEntity target) {
      AbstractEntity caster = meta.getCaster().getEntity();
      ModeledEntity model = ModelEngineAPI.getModeledEntity(caster.getUniqueId());
      if (model == null) {
         return SkillResult.CONDITION_FAILED;
      } else {
         String modelId = MythicUtils.getOrNullLowercase(this.modelId, meta, target);
         model.getModel(modelId).ifPresent((activeModel) -> {
            activeModel.getLeashManager().ifPresent((leashManager) -> {
               String sourceId = MythicUtils.getOrNullLowercase(this.sourceId, meta, target);
               if (this.flag) {
                  ((LeashManager)leashManager).connectLeash(target.getBukkitEntity(), sourceId);
               } else {
                  ((LeashManager)leashManager).disconnect(sourceId);
               }

            });
         });
         return SkillResult.SUCCESS;
      }
   }

   public boolean getTargetsCreatives() {
      return true;
   }
}
