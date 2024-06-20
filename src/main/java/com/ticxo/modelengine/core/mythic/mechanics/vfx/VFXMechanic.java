package com.ticxo.modelengine.core.mythic.mechanics.vfx;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.entity.BaseEntity;
import com.ticxo.modelengine.api.nms.entity.wrapper.BodyRotationController;
import com.ticxo.modelengine.api.vfx.VFX;
import com.ticxo.modelengine.core.mythic.MythicUtils;
import com.ticxo.modelengine.core.mythic.compatibility.ProjectileEntity;
import com.ticxo.modelengine.core.mythic.utils.MythicMechanic;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.INoTargetSkill;
import io.lumine.mythic.api.skills.IParentSkill;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderInt;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import io.lumine.mythic.core.skills.placeholders.PlaceholderMeta;
import io.lumine.mythic.core.skills.projectiles.ProjectileBulletableTracker;
import java.util.UUID;
import org.bukkit.Color;

@MythicMechanic(
   name = "vfx",
   aliases = {}
)
public class VFXMechanic implements ITargetedEntitySkill, INoTargetSkill {
   private final PlaceholderString modelId;
   private final PlaceholderString partId;
   private final boolean remove;
   private final PlaceholderInt radius;
   private final PlaceholderString color;
   private final boolean enchant;
   private final boolean visible;
   private final boolean baseVisible;

   public VFXMechanic(MythicLineConfig mlc) {
      this.modelId = mlc.getPlaceholderString(new String[]{"m", "mid", "model", "modelid"}, (String)null, new String[0]);
      this.partId = mlc.getPlaceholderString(new String[]{"p", "pid", "part", "partid"}, (String)null, new String[0]);
      this.remove = mlc.getBoolean(new String[]{"r", "remove"}, false);
      this.radius = mlc.getPlaceholderInteger(new String[]{"rad", "radius"}, 0, new String[0]);
      this.color = mlc.getPlaceholderString(new String[]{"c", "color"}, "FFFFFF", new String[0]);
      this.enchant = mlc.getBoolean(new String[]{"en", "enchant"}, false);
      this.visible = mlc.getBoolean(new String[]{"v", "visible"}, true);
      this.baseVisible = mlc.getBoolean(new String[]{"bv", "bvisible"}, false);
   }

   public SkillResult castAtEntity(SkillMetadata meta, AbstractEntity target) {
      if (this.remove) {
         VFX vfx = ModelEngineAPI.getAPI().getVFXUpdater().getVFX(target.getUniqueId());
         if (vfx == null) {
            return SkillResult.CONDITION_FAILED;
         } else {
            vfx.markRemoved();
            return SkillResult.SUCCESS;
         }
      } else {
         String modelId = MythicUtils.getOrNullLowercase(this.modelId, meta, target);
         String partId = MythicUtils.getOrNullLowercase(this.partId, meta, target);
         if (modelId != null && partId != null) {
            int radius = this.radius.get(meta, target);
            Color color = MythicUtils.getColor(MythicUtils.getOrNull(this.color, meta, target));
            ModelEngineAPI.createVFX(target.getBukkitEntity(), (vfxx) -> {
               BodyRotationController brc = vfxx.getBase().getBodyRotationController();
               brc.setYBodyRot(brc.getYHeadRot());
               vfxx.useModel(modelId, partId);
               if (radius > 0) {
                  vfxx.getBase().setRenderRadius(radius);
               }

               vfxx.setColor(color);
               vfxx.setEnchanted(this.enchant);
               vfxx.setVisible(this.visible);
               vfxx.setBaseEntityVisible(this.baseVisible);
            });
            return SkillResult.SUCCESS;
         } else {
            return SkillResult.INVALID_CONFIG;
         }
      }
   }

   public SkillResult cast(SkillMetadata meta) {
      IParentSkill var3 = meta.getCallingEvent();
      if (var3 instanceof ProjectileBulletableTracker) {
         ProjectileBulletableTracker projectileTracker = (ProjectileBulletableTracker)var3;
         if (this.remove) {
            UUID uuid = MythicUtils.getVFXUniqueId(meta);
            VFX vfx = ModelEngineAPI.getAPI().getVFXUpdater().getVFX(uuid);
            if (vfx == null) {
               return SkillResult.CONDITION_FAILED;
            } else {
               vfx.markRemoved();
               return SkillResult.SUCCESS;
            }
         } else {
            String modelId = MythicUtils.getOrNullLowercase(this.modelId, (PlaceholderMeta)meta);
            String partId = MythicUtils.getOrNullLowercase(this.partId, (PlaceholderMeta)meta);
            if (modelId != null && partId != null) {
               int radius = this.radius.get(meta);
               Color color = MythicUtils.getColor(MythicUtils.getOrNull(this.color, (PlaceholderMeta)meta));
               MythicUtils.castProjectileEntity(projectileTracker).ifPresent((tracker) -> {
                  ModelEngineAPI.createVFX((BaseEntity)(new ProjectileEntity(tracker)), (vfx) -> {
                     BodyRotationController brc = vfx.getBase().getBodyRotationController();
                     brc.setYBodyRot(brc.getYHeadRot());
                     vfx.useModel(modelId, partId);
                     if (radius > 0) {
                        vfx.getBase().setRenderRadius(radius);
                     }

                     vfx.setColor(color);
                     vfx.setEnchanted(this.enchant);
                     vfx.setVisible(this.visible);
                     vfx.setBaseEntityVisible(this.baseVisible);
                  });
               });
               return SkillResult.SUCCESS;
            } else {
               return SkillResult.INVALID_CONFIG;
            }
         }
      } else {
         return SkillResult.INVALID_TARGET;
      }
   }
}
