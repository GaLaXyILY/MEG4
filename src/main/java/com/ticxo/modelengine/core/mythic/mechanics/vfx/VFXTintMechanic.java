package com.ticxo.modelengine.core.mythic.mechanics.vfx;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.entity.BaseEntity;
import com.ticxo.modelengine.api.entity.BukkitEntity;
import com.ticxo.modelengine.api.utils.scheduling.PlatformScheduler;
import com.ticxo.modelengine.api.utils.scheduling.PlatformTask;
import com.ticxo.modelengine.api.vfx.VFX;
import com.ticxo.modelengine.core.mythic.MythicUtils;
import com.ticxo.modelengine.core.mythic.utils.MythicMechanic;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.INoTargetSkill;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderInt;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import io.lumine.mythic.core.skills.placeholders.PlaceholderMeta;
import java.util.UUID;
import org.bukkit.Color;
import org.bukkit.entity.Entity;

@MythicMechanic(
   name = "vfxtint",
   aliases = {"vfxcolor"}
)
public class VFXTintMechanic implements ITargetedEntitySkill, INoTargetSkill {
   private final PlaceholderInt duration;
   private final PlaceholderString colorA;
   private final PlaceholderString colorB;

   public VFXTintMechanic(MythicLineConfig mlc) {
      this.duration = mlc.getPlaceholderInteger(new String[]{"duration", "d"}, 0, new String[0]);
      this.colorA = mlc.getPlaceholderString(new String[]{"c", "color", "ca", "colora"}, "FFFFFF", new String[0]);
      this.colorB = mlc.getPlaceholderString(new String[]{"cb", "colorb"}, "FFFFFF", new String[0]);
   }

   public SkillResult castAtEntity(SkillMetadata meta, AbstractEntity target) {
      VFX vfx = ModelEngineAPI.getVFX(target.getBukkitEntity());
      if (vfx == null) {
         return SkillResult.INVALID_TARGET;
      } else {
         int duration = this.duration.get(meta, target);
         Color colorA = MythicUtils.getColor(MythicUtils.getOrNull(this.colorA, meta, target));
         if (duration <= 0) {
            vfx.setColor(colorA);
         } else {
            Color colorB = MythicUtils.getColor(MythicUtils.getOrNull(this.colorB, meta, target));
            new VFXTintMechanic.Animator(vfx, duration, colorA, colorB);
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
         int duration = this.duration.get(meta);
         Color colorA = MythicUtils.getColor(MythicUtils.getOrNull(this.colorA, (PlaceholderMeta)meta));
         if (duration <= 0) {
            vfx.setColor(colorA);
         } else {
            Color colorB = MythicUtils.getColor(MythicUtils.getOrNull(this.colorB, (PlaceholderMeta)meta));
            new VFXTintMechanic.Animator(vfx, duration, colorA, colorB);
         }

         return SkillResult.SUCCESS;
      }
   }

   private static class Animator implements Runnable {
      private final VFX target;
      private final double duration;
      private final VFXTintMechanic.FloatColor colorA;
      private final VFXTintMechanic.FloatColor colorB;
      private final PlatformTask task;
      private double iteration;

      public Animator(VFX entity, int duration, Color colorA, Color colorB) {
         this.duration = (double)duration;
         PlatformScheduler scheduler = ModelEngineAPI.getAPI().getScheduler();
         BaseEntity var7 = entity.getBase();
         if (var7 instanceof BukkitEntity) {
            BukkitEntity bukkitEntity = (BukkitEntity)var7;
            this.task = scheduler.scheduleRepeating(ModelEngineAPI.getAPI(), (Entity)bukkitEntity.getOriginal(), this, 0L, 1L);
         } else {
            this.task = scheduler.scheduleRepeating(ModelEngineAPI.getAPI(), this, 0L, 1L);
         }

         this.colorA = new VFXTintMechanic.FloatColor(colorA);
         this.colorB = new VFXTintMechanic.FloatColor(colorB);
         this.target = entity;
      }

      public void run() {
         if (this.iteration > this.duration) {
            this.task.cancel();
         } else {
            this.target.setColor(this.lerpColor(this.iteration / this.duration));
            ++this.iteration;
         }

      }

      private Color lerpColor(double ratio) {
         int r = (int)(Math.sqrt((1.0D - ratio) * (double)this.colorA.r + ratio * (double)this.colorB.r) * 255.0D);
         int g = (int)(Math.sqrt((1.0D - ratio) * (double)this.colorA.g + ratio * (double)this.colorB.g) * 255.0D);
         int b = (int)(Math.sqrt((1.0D - ratio) * (double)this.colorA.b + ratio * (double)this.colorB.b) * 255.0D);
         return Color.fromRGB(r, g, b);
      }
   }

   private static class FloatColor {
      protected final float r;
      protected final float g;
      protected final float b;

      public FloatColor(Color color) {
         this.r = (float)(color.getRed() * color.getRed()) / 65025.0F;
         this.g = (float)(color.getGreen() * color.getGreen()) / 65025.0F;
         this.b = (float)(color.getBlue() * color.getBlue()) / 65025.0F;
      }
   }
}
