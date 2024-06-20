package com.ticxo.modelengine.core.mythic.mechanics.entity;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.core.mythic.utils.MythicMechanic;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;

@MythicMechanic(
   name = "lockmodel",
   aliases = {"lockrotation"}
)
public class LockModelRotationMechanic implements ITargetedEntitySkill {
   private final boolean lock;

   public LockModelRotationMechanic(MythicLineConfig mlc) {
      this.lock = mlc.getBoolean(new String[]{"l", "lock"}, true);
   }

   public SkillResult castAtEntity(SkillMetadata meta, AbstractEntity target) {
      ModeledEntity model = ModelEngineAPI.getModeledEntity(target.getUniqueId());
      if (model == null) {
         return SkillResult.CONDITION_FAILED;
      } else {
         model.setModelRotationLocked(this.lock);
         return SkillResult.SUCCESS;
      }
   }
}
