package com.ticxo.modelengine.core.mythic.mechanics.entity;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.entity.data.BukkitEntityData;
import com.ticxo.modelengine.api.entity.data.IEntityData;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.core.mythic.utils.MythicMechanic;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

@MythicMechanic(
   name = "pairmodel",
   aliases = {}
)
public class PairModelMechanic implements ITargetedEntitySkill {
   private final boolean hidden;
   private final boolean remove;

   public PairModelMechanic(MythicLineConfig mlc) {
      this.hidden = mlc.getBoolean(new String[]{"h", "hidden"}, false);
      this.remove = mlc.getBoolean(new String[]{"r", "remove"}, false);
   }

   public SkillResult castAtEntity(SkillMetadata meta, AbstractEntity target) {
      ModeledEntity model = ModelEngineAPI.getModeledEntity(meta.getCaster().getEntity().getUniqueId());
      if (model == null) {
         return SkillResult.CONDITION_FAILED;
      } else {
         IEntityData var6 = model.getBase().getData();
         if (var6 instanceof BukkitEntityData) {
            BukkitEntityData data = (BukkitEntityData)var6;
            Entity var7 = target.getBukkitEntity();
            if (var7 instanceof Player) {
               Player player = (Player)var7;
               if (this.hidden) {
                  if (this.remove) {
                     data.getTracked().removeForcedHidden(player);
                  } else {
                     data.getTracked().addForcedHidden(player);
                  }
               } else if (this.remove) {
                  data.getTracked().removeForcedPairing(player);
               } else {
                  data.getTracked().addForcedPairing(player);
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
