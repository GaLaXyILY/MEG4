package com.ticxo.modelengine.core.mythic.mechanics.disguise;

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
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

@MythicMechanic(
   name = "undisguise",
   aliases = {"modelundisguise"}
)
public class UndisguiseMechanic implements ITargetedEntitySkill {
   private final PlaceholderString modelId;

   public UndisguiseMechanic(MythicLineConfig mlc) {
      this.modelId = mlc.getPlaceholderString(new String[]{"m", "mid", "model", "modelid"}, (String)null, new String[0]);
   }

   public SkillResult castAtEntity(SkillMetadata meta, AbstractEntity target) {
      if (!target.isPlayer()) {
         return SkillResult.INVALID_TARGET;
      } else {
         Player player = (Player)target.getBukkitEntity();
         ModeledEntity modeledEntity = ModelEngineAPI.getModeledEntity((Entity)player);
         if (modeledEntity != null) {
            String modelId = MythicUtils.getOrNullLowercase(this.modelId, meta, target);
            if (modelId == null) {
               modeledEntity.markRemoved();
               ModelEngineAPI.getEntityHandler().setForcedInvisible(player, false);
               ModelEngineAPI.getEntityHandler().forceSpawn(player);
            } else {
               modeledEntity.removeModel(modelId).ifPresent(ActiveModel::destroy);
               if (modeledEntity.getModels().isEmpty()) {
                  modeledEntity.markRemoved();
                  ModelEngineAPI.getEntityHandler().setForcedInvisible(player, false);
                  ModelEngineAPI.getEntityHandler().forceSpawn(player);
               }
            }
         }

         return SkillResult.SUCCESS;
      }
   }

   public boolean getTargetsCreatives() {
      return true;
   }
}
