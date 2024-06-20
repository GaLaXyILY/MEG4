package com.ticxo.modelengine.core.mythic.mechanics.mounting;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.model.bone.manager.MountData;
import com.ticxo.modelengine.api.model.bone.manager.MountManager;
import com.ticxo.modelengine.core.mythic.MythicUtils;
import com.ticxo.modelengine.core.mythic.utils.MythicMechanic;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import java.util.Optional;
import org.bukkit.entity.Entity;

@MythicMechanic(
   name = "dismountmodel",
   aliases = {}
)
public class DismountModelMechanic implements ITargetedEntitySkill {
   private final boolean driver;
   private PlaceholderString modelId;
   private PlaceholderString pBone;

   public DismountModelMechanic(MythicLineConfig mlc) {
      this.driver = mlc.getBoolean(new String[]{"d", "drive", "driver"}, true);
      if (!this.driver) {
         this.modelId = mlc.getPlaceholderString(new String[]{"m", "mid", "model", "modelid"}, (String)null, new String[0]);
         this.pBone = mlc.getPlaceholderString(new String[]{"p", "pbone", "seat"}, (String)null, new String[0]);
      }

   }

   public SkillResult castAtEntity(SkillMetadata meta, AbstractEntity target) {
      ModeledEntity model = ModelEngineAPI.getModeledEntity(meta.getCaster().getEntity().getUniqueId());
      if (model == null) {
         return SkillResult.CONDITION_FAILED;
      } else if (this.driver) {
         ((MountManager)((MountData)model.getMountData()).getMainMountManager()).dismountDriver();
         return SkillResult.SUCCESS;
      } else {
         String modelId = MythicUtils.getOrNullLowercase(this.modelId, meta, target);
         model.getModel(modelId).ifPresent((activeModel) -> {
            activeModel.getMountManager().ifPresent((mountManager) -> {
               Entity entity = target.getBukkitEntity();
               String part = MythicUtils.getOrNullLowercase(this.pBone, meta, target);
               if (part == null) {
                  ((MountManager)mountManager).dismountRider(entity);
               } else {
                  String[] seats = part.split(",");
                  Optional maybeMount = ((MountManager)mountManager).getMount(entity);
                  maybeMount.ifPresent((mount) -> {
                     if (this.contains(mount.getBone().getUniqueBoneId(), seats)) {
                        ((MountManager)mountManager).dismountRider(entity);
                     }

                  });
               }
            });
         });
         return SkillResult.SUCCESS;
      }
   }

   public boolean getTargetsCreatives() {
      return true;
   }

   private boolean contains(String seat, String... seats) {
      String[] var3 = seats;
      int var4 = seats.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         String s = var3[var5];
         if (seat.equals(s)) {
            return true;
         }
      }

      return false;
   }
}
