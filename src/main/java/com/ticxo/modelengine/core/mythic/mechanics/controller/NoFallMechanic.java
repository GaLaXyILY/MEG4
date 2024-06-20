package com.ticxo.modelengine.core.mythic.mechanics.controller;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.nms.entity.wrapper.MoveController;
import com.ticxo.modelengine.core.mythic.utils.MythicMechanic;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;

@MythicMechanic(
   name = "nofall",
   aliases = {"cancelfall", "cancelfalldamage"}
)
public class NoFallMechanic implements ITargetedEntitySkill {
   public SkillResult castAtEntity(SkillMetadata meta, AbstractEntity target) {
      Object controller = meta.getMetadata("meg:move_controller").orElseGet(() -> {
         ModeledEntity model = ModelEngineAPI.getModeledEntity(target.getUniqueId());
         return model == null ? null : model.getBase().getMoveController();
      });
      if (controller instanceof MoveController) {
         MoveController moveController = (MoveController)controller;
         moveController.nullifyFallDistance();
      }

      return SkillResult.SUCCESS;
   }
}
