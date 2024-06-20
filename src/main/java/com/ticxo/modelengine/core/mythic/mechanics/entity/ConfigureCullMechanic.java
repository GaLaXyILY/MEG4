package com.ticxo.modelengine.core.mythic.mechanics.entity;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.entity.CullType;
import com.ticxo.modelengine.api.entity.data.IEntityData;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.utils.math.TMath;
import com.ticxo.modelengine.core.mythic.utils.MythicMechanic;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import java.util.function.Function;

@MythicMechanic(
   name = "cullconfig",
   aliases = {"cull"}
)
public class ConfigureCullMechanic implements ITargetedEntitySkill {
   private final MythicLineConfig config;

   public ConfigureCullMechanic(MythicLineConfig mlc) {
      this.config = mlc;
   }

   public SkillResult castAtEntity(SkillMetadata meta, AbstractEntity target) {
      ModeledEntity model = ModelEngineAPI.getModeledEntity(target.getUniqueId());
      if (model == null) {
         return SkillResult.CONDITION_FAILED;
      } else {
         IEntityData data = model.getBase().getData();
         Integer cullInterval = (Integer)this.getOrDefault(meta, target, Integer::parseInt, data.getCullInterval(), "ci", "cullinterval");
         Boolean verticalCull = (Boolean)this.getOrDefault(meta, target, Boolean::parseBoolean, data.getVerticalCull(), "vc", "verticalcull");
         Double verticalCullDistance = (Double)this.getOrDefault(meta, target, Double::parseDouble, data.getVerticalCullDistance(), "vcd", "verticalculldistance");
         CullType verticalCullType = (CullType)this.getOrDefault(meta, target, CullType::get, data.getVerticalCullType(), "vct", "verticalculltype");
         Boolean backCull = (Boolean)this.getOrDefault(meta, target, Boolean::parseBoolean, data.getBackCull(), "bkc", "backcull");
         Double backCullAngle = (Double)this.getOrDefault(meta, target, (val) -> {
            double degree = Double.parseDouble(val);
            return Math.cos(TMath.clamp(degree, 0.0D, 360.0D) * 0.5D * 0.01745329238474369D);
         }, data.getBackCullAngle(), "bkca", "backcullangle");
         Double backCullIgnoreRadius = (Double)this.getOrDefault(meta, target, Double::parseDouble, data.getBackCullIgnoreRadius(), "bkcr", "backcullignoreradius");
         CullType backCullType = (CullType)this.getOrDefault(meta, target, CullType::get, data.getBackCullType(), "bkct", "backculltype");
         Boolean blockedCull = (Boolean)this.getOrDefault(meta, target, Boolean::parseBoolean, data.getBlockedCull(), "blc", "blockedcull");
         Double blockedCullIgnoreRadius = (Double)this.getOrDefault(meta, target, Double::parseDouble, data.getBlockedCullIgnoreRadius(), "blcr", "blockedcullignoreradius");
         CullType blockedCullType = (CullType)this.getOrDefault(meta, target, CullType::get, data.getBlockedCullType(), "blct", "blockedculltype");
         data.setCullInterval(cullInterval);
         data.setVerticalCull(verticalCull);
         data.setVerticalCullDistance(verticalCullDistance);
         data.setVerticalCullType(verticalCullType);
         data.setBackCull(backCull);
         data.setBackCullAngle(backCullAngle);
         data.setBackCullIgnoreRadius(backCullIgnoreRadius);
         data.setBackCullType(backCullType);
         data.setBlockedCull(blockedCull);
         data.setBlockedCullIgnoreRadius(blockedCullIgnoreRadius);
         data.setBlockedCullType(blockedCullType);
         return SkillResult.SUCCESS;
      }
   }

   private <T> T getOrDefault(SkillMetadata meta, AbstractEntity target, Function<String, T> function, T def, String... alias) {
      PlaceholderString placeholder = this.config.getPlaceholderString(alias, (String)null, new String[0]);
      if (placeholder != null && placeholder.isPresent()) {
         try {
            return function.apply(placeholder.get(meta, target));
         } catch (Exception var8) {
            return def;
         }
      } else {
         return def;
      }
   }
}
