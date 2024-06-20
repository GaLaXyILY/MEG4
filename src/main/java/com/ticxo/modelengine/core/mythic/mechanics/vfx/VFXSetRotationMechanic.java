package com.ticxo.modelengine.core.mythic.mechanics.vfx;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.utils.logger.TLogger;
import com.ticxo.modelengine.api.utils.math.TMath;
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
import org.joml.Quaternionf;
import org.joml.Vector3f;

@MythicMechanic(
   name = "vfxrot",
   aliases = {}
)
public class VFXSetRotationMechanic implements ITargetedEntitySkill, INoTargetSkill {
   private final boolean relative;
   private final boolean newOrigin;
   private PlaceholderDouble x;
   private PlaceholderDouble y;
   private PlaceholderDouble z;

   public VFXSetRotationMechanic(MythicLineConfig mlc) {
      String coords = mlc.getString(new String[]{"r", "rot", "rotation"}, (String)null, new String[0]);
      if (coords != null) {
         String[] split = coords.split(",");

         try {
            this.x = PlaceholderDouble.of(split[0]);
            this.y = PlaceholderDouble.of(split[1]);
            this.z = PlaceholderDouble.of(split[2]);
         } catch (Exception var5) {
            TLogger.error("The 'rotation' attribute must be in the format r=x,y,z.");
            this.x = PlaceholderDouble.of("0");
            this.y = PlaceholderDouble.of("0");
            this.z = PlaceholderDouble.of("0");
         }
      } else {
         this.x = mlc.getPlaceholderDouble("x", 0.0D);
         this.y = mlc.getPlaceholderDouble("y", 0.0D);
         this.z = mlc.getPlaceholderDouble("z", 0.0D);
      }

      this.relative = mlc.getBoolean(new String[]{"rel", "relative"}, false);
      this.newOrigin = mlc.getBoolean(new String[]{"neworigin", "origin", "o"}, false);
   }

   public SkillResult castAtEntity(SkillMetadata meta, AbstractEntity target) {
      VFX vfx = ModelEngineAPI.getVFX(target.getBukkitEntity());
      if (vfx == null) {
         return SkillResult.INVALID_TARGET;
      } else {
         if (this.relative) {
            Vector3f r = vfx.getRotation();
            Quaternionf original = (new Quaternionf()).rotationZYX(r.z, r.y, r.x);
            Quaternionf delta = (new Quaternionf()).rotationZYX((float)this.z.get(meta, target) * 0.017453292F, (float)this.y.get(meta, target) * 0.017453292F, (float)this.x.get(meta, target) * 0.017453292F);
            if (this.newOrigin) {
               original.mul(delta);
               TMath.getEulerAnglesZYX(original, r);
            } else {
               delta.mul(original);
               TMath.getEulerAnglesZYX(delta, r);
            }
         } else {
            vfx.setRotation(new Vector3f((float)this.x.get(meta, target) * 0.017453292F, (float)this.y.get(meta, target) * 0.017453292F, (float)this.z.get(meta, target) * 0.017453292F));
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
            Vector3f r = vfx.getRotation();
            Quaternionf original = (new Quaternionf()).rotationZYX(r.z, r.y, r.x);
            Quaternionf delta = (new Quaternionf()).rotationZYX((float)this.z.get(meta) * 0.017453292F, (float)this.y.get(meta) * 0.017453292F, (float)this.x.get(meta) * 0.017453292F);
            if (this.newOrigin) {
               original.mul(delta);
               TMath.getEulerAnglesZYX(original, r);
            } else {
               delta.mul(original);
               TMath.getEulerAnglesZYX(delta, r);
            }
         } else {
            vfx.setRotation(new Vector3f((float)this.x.get(meta) * 0.017453292F, (float)this.y.get(meta) * 0.017453292F, (float)this.z.get(meta) * 0.017453292F));
         }

         return SkillResult.SUCCESS;
      }
   }
}
