package com.ticxo.modelengine.core.mythic.mechanics.vfx;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.vfx.VFX;
import com.ticxo.modelengine.core.mythic.MythicUtils;
import com.ticxo.modelengine.core.mythic.utils.MythicMechanic;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.skills.INoTargetSkill;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import java.util.UUID;
import org.joml.Vector3f;

@MythicMechanic(
   name = "vfxposreset",
   aliases = {}
)
public class VFXResetPositionMechanic implements ITargetedEntitySkill, INoTargetSkill {
   public SkillResult castAtEntity(SkillMetadata meta, AbstractEntity target) {
      VFX vfx = ModelEngineAPI.getVFX(target.getBukkitEntity());
      if (vfx == null) {
         return SkillResult.INVALID_TARGET;
      } else {
         vfx.setPosition(new Vector3f());
         return SkillResult.SUCCESS;
      }
   }

   public SkillResult cast(SkillMetadata meta) {
      UUID uuid = MythicUtils.getVFXUniqueId(meta);
      VFX vfx = ModelEngineAPI.getAPI().getVFXUpdater().getVFX(uuid);
      if (vfx == null) {
         return SkillResult.INVALID_TARGET;
      } else {
         vfx.setPosition(new Vector3f());
         return SkillResult.SUCCESS;
      }
   }
}
