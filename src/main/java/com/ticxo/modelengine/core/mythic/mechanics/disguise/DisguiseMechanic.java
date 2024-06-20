package com.ticxo.modelengine.core.mythic.mechanics.disguise;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.entity.data.BukkitEntityData;
import com.ticxo.modelengine.api.entity.data.IEntityData;
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
import org.bukkit.entity.Player;

@MythicMechanic(
   name = "disguise",
   aliases = {"modeldisguise"}
)
public class DisguiseMechanic implements ITargetedEntitySkill {
   private final PlaceholderString modelId;
   private final boolean viewSelf;

   public DisguiseMechanic(MythicLineConfig mlc) {
      this.modelId = mlc.getPlaceholderString(new String[]{"m", "mid", "model", "modelid"}, (String)null, new String[0]);
      this.viewSelf = mlc.getBoolean(new String[]{"s", "see", "seeself"}, true);
   }

   public SkillResult castAtEntity(SkillMetadata meta, AbstractEntity target) {
      if (!target.isPlayer()) {
         return SkillResult.INVALID_TARGET;
      } else {
         Player player = (Player)target.getBukkitEntity();
         String modelId = MythicUtils.getOrNullLowercase(this.modelId, meta, target);
         ModelBlueprint blueprint = MythicUtils.getBlueprintOrNull(modelId);
         if (blueprint == null) {
            return SkillResult.INVALID_CONFIG;
         } else {
            ModeledEntity modeledEntity = ModelEngineAPI.getOrCreateModeledEntity(player);
            modeledEntity.getBase().getBodyRotationController().setPlayerMode(true);
            modeledEntity.setBaseEntityVisible(false);
            IEntityData var8 = modeledEntity.getBase().getData();
            if (var8 instanceof BukkitEntityData) {
               BukkitEntityData data = (BukkitEntityData)var8;
               if (this.viewSelf) {
                  data.getTracked().addForcedPairing(player);
               }
            }

            ModelEngineAPI.getEntityHandler().setForcedInvisible(player, true);
            if (modeledEntity.getModel(modelId).isEmpty()) {
               ActiveModel activeModel = ModelEngineAPI.createActiveModel(blueprint);
               modeledEntity.addModel(activeModel, false).ifPresent(ActiveModel::destroy);
            }

            return SkillResult.SUCCESS;
         }
      }
   }

   public boolean getTargetsCreatives() {
      return true;
   }
}
