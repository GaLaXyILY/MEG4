package com.ticxo.modelengine.core.mythic.mechanics.vfx;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.vfx.VFX;
import com.ticxo.modelengine.core.mythic.MythicUtils;
import com.ticxo.modelengine.core.mythic.utils.MythicMechanic;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.INoTargetSkill;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import io.lumine.mythic.core.skills.placeholders.PlaceholderMeta;
import java.util.UUID;

@MythicMechanic(
   name = "vfxchangemodel",
   aliases = {}
)
public class VFXChangeModelMechanic implements ITargetedEntitySkill, INoTargetSkill {
   private final PlaceholderString modelId;
   private final PlaceholderString partId;

   public VFXChangeModelMechanic(MythicLineConfig mlc) {
      this.modelId = mlc.getPlaceholderString(new String[]{"m", "mid", "model", "modelid"}, (String)null, new String[0]);
      this.partId = mlc.getPlaceholderString(new String[]{"p", "pid", "part", "partid"}, (String)null, new String[0]);
   }

   public SkillResult castAtEntity(SkillMetadata meta, AbstractEntity target) {
      String modelId = MythicUtils.getOrNullLowercase(this.modelId, meta, target);
      String partId = MythicUtils.getOrNullLowercase(this.partId, meta, target);
      if (modelId != null && partId != null) {
         VFX vfx = ModelEngineAPI.getVFX(target.getBukkitEntity());
         if (vfx == null) {
            return SkillResult.INVALID_TARGET;
         } else {
            vfx.useModel(modelId, partId);
            return SkillResult.SUCCESS;
         }
      } else {
         return SkillResult.INVALID_CONFIG;
      }
   }

   public SkillResult cast(SkillMetadata meta) {
      String modelId = MythicUtils.getOrNullLowercase(this.modelId, (PlaceholderMeta)meta);
      String partId = MythicUtils.getOrNullLowercase(this.partId, (PlaceholderMeta)meta);
      if (modelId != null && partId != null) {
         UUID uuid = MythicUtils.getVFXUniqueId(meta);
         VFX vfx = ModelEngineAPI.getAPI().getVFXUpdater().getVFX(uuid);
         if (vfx == null) {
            return SkillResult.INVALID_TARGET;
         } else {
            vfx.useModel(modelId, partId);
            return SkillResult.SUCCESS;
         }
      } else {
         return SkillResult.INVALID_CONFIG;
      }
   }
}
