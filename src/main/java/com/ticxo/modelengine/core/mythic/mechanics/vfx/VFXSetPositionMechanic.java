package com.ticxo.modelengine.core.mythic.mechanics.vfx;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.utils.logger.TLogger;
import com.ticxo.modelengine.api.vfx.VFX;
import com.ticxo.modelengine.core.mythic.MythicUtils;
import com.ticxo.modelengine.core.mythic.utils.MythicMechanic;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.INoTargetSkill;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import java.util.UUID;
import org.joml.Vector3f;

@MythicMechanic(
   name = "vfxpos",
   aliases = {}
)
public class VFXSetPositionMechanic implements ITargetedEntitySkill, INoTargetSkill {
   private final boolean relative;
   private PlaceholderDouble x;
   private PlaceholderDouble y;
   private PlaceholderDouble z;

   public VFXSetPositionMechanic(MythicLineConfig mlc) {
      String coords = mlc.getString(new String[]{"p", "pos", "position"}, (String)null, new String[0]);
      if (coords != null) {
         String[] split = coords.split(",");

         try {
            this.x = PlaceholderDouble.of(split[0]);
            this.y = PlaceholderDouble.of(split[1]);
            this.z = PlaceholderDouble.of(split[2]);
         } catch (Exception var5) {
            TLogger.error("The 'position' attribute must be in the format p=x,y,z.");
            this.x = PlaceholderDouble.of("0");
            this.y = PlaceholderDouble.of("0");
            this.z = PlaceholderDouble.of("0");
         }
      } else {
         this.x = mlc.getPlaceholderDouble("x", 0.0D);
         this.y = mlc.getPlaceholderDouble("y", 0.0D);
         this.z = mlc.getPlaceholderDouble("z", 0.0D);
      }

      this.relative = mlc.getBoolean(new String[]{"r", "rel", "relative"}, false);
   }

   public SkillResult castAtEntity(SkillMetadata meta, AbstractEntity target) {
      VFX vfx = ModelEngineAPI.getVFX(target.getBukkitEntity());
      if (vfx == null) {
         return SkillResult.INVALID_TARGET;
      } else {
         if (this.relative) {
            vfx.getPosition().add((float)this.x.get(meta, target), (float)this.y.get(meta, target), (float)this.z.get(meta, target));
         } else {
            vfx.setPosition(new Vector3f((float)this.x.get(meta, target), (float)this.y.get(meta, target), (float)this.z.get(meta, target)));
         }

         return SkillResult.SUCCESS;
      }
   }

   public SkillResult cast(SkillMetadata meta) {
      UUID uuid = MythicUtils.getVFXUniqueId(meta);
      VFX vfx = ModelEngineAPI.getAPI().getVFXUpdater().getVFX(uuid);
      if (vfx == null) {
         return SkillResult.INVALID_TARGET;
      } else {
         if (this.relative) {
            vfx.getPosition().add((float)this.x.get(meta), (float)this.y.get(meta), (float)this.z.get(meta));
         } else {
            vfx.setPosition(new Vector3f((float)this.x.get(meta), (float)this.y.get(meta), (float)this.z.get(meta)));
         }

         return SkillResult.SUCCESS;
      }
   }
}
