package com.ticxo.modelengine.core.mythic.mechanics.controller;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.nms.entity.wrapper.MoveController;
import com.ticxo.modelengine.core.mythic.utils.MythicMechanic;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;

@MythicMechanic(
   name = "move",
   aliases = {}
)
public class MoveMechanic implements ITargetedEntitySkill {
   private final PlaceholderDouble x;
   private final PlaceholderDouble y;
   private final PlaceholderDouble z;
   private final PlaceholderDouble speedModifier;
   private final boolean global;
   private final boolean queue;

   public MoveMechanic(MythicLineConfig mlc) {
      this.x = mlc.getPlaceholderDouble(new String[]{"side", "x"}, 0.0D, new String[0]);
      this.y = mlc.getPlaceholderDouble(new String[]{"up", "y"}, 0.0D, new String[0]);
      this.z = mlc.getPlaceholderDouble(new String[]{"front", "z"}, 0.0D, new String[0]);
      this.speedModifier = mlc.getPlaceholderDouble(new String[]{"speed"}, 1.0D, new String[0]);
      this.global = mlc.getBoolean(new String[]{"global", "g"}, false);
      this.queue = mlc.getBoolean(new String[]{"queue", "q"}, false);
   }

   public SkillResult castAtEntity(SkillMetadata meta, AbstractEntity target) {
      Object controller = meta.getMetadata("meg:move_controller").orElseGet(() -> {
         ModeledEntity model = ModelEngineAPI.getModeledEntity(target.getUniqueId());
         return model == null ? null : model.getBase().getMoveController();
      });
      if (controller instanceof MoveController) {
         MoveController moveController = (MoveController)controller;
         float x = (float)this.x.get(meta, target);
         float y = (float)this.y.get(meta, target);
         float z = (float)this.z.get(meta, target);
         float speed = (float)this.speedModifier.get(meta, target);
         if (this.global) {
            if (this.queue) {
               moveController.queuePostTick(() -> {
                  moveController.globalMove(x, y, z, speed);
               });
            } else {
               moveController.globalMove(x, y, z, speed);
            }
         } else if (this.queue) {
            moveController.queuePostTick(() -> {
               moveController.move(x, y, z, speed);
            });
         } else {
            moveController.move(x, y, z, speed);
         }
      }

      return SkillResult.SUCCESS;
   }
}
