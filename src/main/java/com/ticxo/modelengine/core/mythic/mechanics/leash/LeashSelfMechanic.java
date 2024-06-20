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
   name = "leashself",
   aliases = {}
)
public class LeashSelfMechanic implements ITargetedEntitySkill {
   private final PlaceholderString modelId;
   private final PlaceholderString sourceId;
   private final PlaceholderString destId;

   public LeashSelfMechanic(MythicLineConfig mlc) {
      this.modelId = mlc.getPlaceholderString(new String[]{"m", "mid", "model", "modelid"}, (String)null, new String[0]);
      this.sourceId = mlc.getPlaceholderString(new String[]{"s", "sid", "src", "source", "sourceid"}, (String)null, new String[0]);
      this.destId = mlc.getPlaceholderString(new String[]{"d", "did", "dest", "destid"}, (String)null, new String[0]);
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
               String destId = MythicUtils.getOrNullLowercase(this.destId, meta, target);
               ((LeashManager)leashManager).connectLeash(destId, sourceId);
            });
         });
         return SkillResult.SUCCESS;
      }
   }
}
