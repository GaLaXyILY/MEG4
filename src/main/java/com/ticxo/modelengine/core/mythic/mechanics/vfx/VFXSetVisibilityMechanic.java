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
import java.util.UUID;

@MythicMechanic(
   name = "vfxsetvis",
   aliases = {}
)
public class VFXSetVisibilityMechanic implements ITargetedEntitySkill, INoTargetSkill {
   private final boolean vis;

   public VFXSetVisibilityMechanic(MythicLineConfig mlc) {
      this.vis = mlc.getBoolean(new String[]{"v", "visible"}, false);
   }

   public SkillResult castAtEntity(SkillMetadata meta, AbstractEntity target) {
      VFX vfx = ModelEngineAPI.getVFX(target.getBukkitEntity());
      if (vfx == null) {
         return SkillResult.INVALID_TARGET;
      } else {
         vfx.setVisible(this.vis);
         return SkillResult.SUCCESS;
      }
   }

   public SkillResult cast(SkillMetadata meta) {
      UUID uuid = MythicUtils.getVFXUniqueId(meta);
      VFX vfx = ModelEngineAPI.getAPI().getVFXUpdater().getVFX(uuid);
      if (vfx == null) {
         return SkillResult.INVALID_TARGET;
      } else {
         vfx.setVisible(this.vis);
         return SkillResult.SUCCESS;
      }
   }
}
