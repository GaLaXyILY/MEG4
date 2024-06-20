package com.ticxo.modelengine.core.mythic;

import com.google.common.collect.Lists;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.generator.blueprint.ModelBlueprint;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.mount.controller.MountControllerSupplier;
import com.ticxo.modelengine.core.ModelEngine;
import com.ticxo.modelengine.core.mythic.compatibility.MythicMountController;
import com.ticxo.modelengine.core.mythic.compatibility.ProjectileEntity;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.packs.Pack;
import io.lumine.mythic.api.skills.IParentSkill;
import io.lumine.mythic.api.skills.Skill;
import io.lumine.mythic.api.skills.SkillCaster;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.skills.placeholders.PlaceholderMeta;
import io.lumine.mythic.core.skills.projectiles.ProjectileBulletableTracker;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import org.bukkit.Color;
import org.jetbrains.annotations.Nullable;

public class MythicUtils {
   public static String getOrNull(@Nullable PlaceholderString placeholderString) {
      return placeholderString == null ? null : placeholderString.get();
   }

   public static String getOrNull(@Nullable PlaceholderString placeholderString, PlaceholderMeta meta) {
      return placeholderString == null ? null : placeholderString.get(meta);
   }

   public static String getOrNull(@Nullable PlaceholderString placeholderString, AbstractEntity entity) {
      return placeholderString == null ? null : placeholderString.get(entity);
   }

   public static String getOrNull(@Nullable PlaceholderString placeholderString, PlaceholderMeta meta, AbstractEntity entity) {
      return placeholderString == null ? null : placeholderString.get(meta, entity);
   }

   public static String getOrNull(@Nullable PlaceholderString placeholderString, SkillCaster caster) {
      return placeholderString == null ? null : placeholderString.get(caster);
   }

   public static String getOrNullLowercase(@Nullable PlaceholderString placeholderString) {
      return placeholderString == null ? null : placeholderString.get().toLowerCase(Locale.ENGLISH);
   }

   public static String getOrNullLowercase(@Nullable PlaceholderString placeholderString, PlaceholderMeta meta) {
      return placeholderString == null ? null : placeholderString.get(meta).toLowerCase(Locale.ENGLISH);
   }

   public static String getOrNullLowercase(@Nullable PlaceholderString placeholderString, AbstractEntity entity) {
      return placeholderString == null ? null : placeholderString.get(entity).toLowerCase(Locale.ENGLISH);
   }

   public static String getOrNullLowercase(@Nullable PlaceholderString placeholderString, PlaceholderMeta meta, AbstractEntity entity) {
      return placeholderString == null ? null : placeholderString.get(meta, entity).toLowerCase(Locale.ENGLISH);
   }

   public static String getOrNullLowercase(@Nullable PlaceholderString placeholderString, SkillCaster caster) {
      return placeholderString == null ? null : placeholderString.get(caster).toLowerCase(Locale.ENGLISH);
   }

   public static Color getColor(@Nullable String colorString) {
      if (colorString == null) {
         return Color.WHITE;
      } else {
         if (colorString.startsWith("#")) {
            colorString = colorString.substring(1);
         }

         return Color.fromRGB(Integer.parseInt(colorString, 16));
      }
   }

   public static ActiveModel getActiveModelOrNull(ModeledEntity entity, @Nullable String modelId) {
      return modelId == null ? null : (ActiveModel)entity.getModel(modelId.toLowerCase(Locale.ENGLISH)).orElse((Object)null);
   }

   public static ModelBlueprint getBlueprintOrNull(@Nullable String modelid) {
      return modelid == null ? null : ModelEngineAPI.getBlueprint(modelid);
   }

   public static ProjectileEntity<?> getProjectileEntity(SkillMetadata meta) {
      return (ProjectileEntity)castProjectileEntity(meta).map((tracker) -> {
         return (ProjectileEntity)ModelEngine.CORE.getMythicCompatibility().getMythicSupport().getTrackers().get(tracker);
      }).orElse((Object)null);
   }

   public static <T extends ProjectileBulletableTracker & IParentSkill> Optional<T> castProjectileEntity(SkillMetadata metadata) {
      IParentSkill var2 = metadata.getCallingEvent();
      Optional var10000;
      if (var2 instanceof ProjectileBulletableTracker) {
         ProjectileBulletableTracker tracker = (ProjectileBulletableTracker)var2;
         var10000 = castProjectileEntity(tracker);
      } else {
         var10000 = Optional.empty();
      }

      return var10000;
   }

   public static <T extends ProjectileBulletableTracker & IParentSkill> Optional<T> castProjectileEntity(ProjectileBulletableTracker tracker) {
      return tracker instanceof IParentSkill ? Optional.of(tracker) : Optional.empty();
   }

   public static UUID getVFXUniqueId(SkillMetadata meta) {
      ProjectileEntity<?> base = getProjectileEntity(meta);
      return base != null ? base.getUUID() : meta.getCaster().getEntity().getUniqueId();
   }

   public static void executeOptModelId(ModeledEntity entity, String modelId, Consumer<ActiveModel> consumer) {
      if (modelId == null) {
         Iterator var3 = entity.getModels().values().iterator();

         while(var3.hasNext()) {
            ActiveModel activeModel = (ActiveModel)var3.next();
            consumer.accept(activeModel);
         }
      } else {
         ActiveModel activeModel = getActiveModelOrNull(entity, modelId);
         if (activeModel != null) {
            consumer.accept(activeModel);
         }
      }

   }

   public static MountControllerSupplier createControllerSupplier(Skill skill, SkillMetadata metadata) {
      return (entity, mount) -> {
         return new MythicMountController(entity, mount, skill, metadata);
      };
   }

   public static List<File> getPackModelFiles() {
      List<File> files = Lists.newArrayList();
      Iterator var1 = MythicBukkit.inst().getPackManager().getPacks().iterator();

      while(var1.hasNext()) {
         Pack pack = (Pack)var1.next();
         File folder = pack.getPackFolder("Models");
         if (folder.exists()) {
            addFilesRecursively(folder, files);
         }
      }

      return files;
   }

   private static void addFilesRecursively(File folder, List<File> files) {
      File[] fileList = folder.listFiles();
      if (fileList != null) {
         File[] var3 = fileList;
         int var4 = fileList.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            File file = var3[var5];
            if (file.isFile() && file.getName().endsWith(".bbmodel")) {
               files.add(file);
            } else if (file.isDirectory()) {
               addFilesRecursively(file, files);
            }
         }
      }

   }
}
