package com.ticxo.modelengine.core.mythic.mechanics.bone;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.model.bone.ModelBone;
import com.ticxo.modelengine.core.mythic.MythicUtils;
import com.ticxo.modelengine.core.mythic.utils.MythicMechanic;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import java.util.Iterator;
import java.util.Map.Entry;
import org.bukkit.Color;

@MythicMechanic(
   name = "tint",
   aliases = {"color"}
)
public class TintMechanic implements ITargetedEntitySkill {
   private final PlaceholderString modelId;
   private final PlaceholderString partId;
   private final PlaceholderString color;
   private final boolean damageTint;
   private final boolean exactMatch;

   public TintMechanic(MythicLineConfig mlc) {
      this.modelId = mlc.getPlaceholderString(new String[]{"m", "mid", "model", "modelid"}, (String)null, new String[0]);
      this.partId = mlc.getPlaceholderString(new String[]{"p", "pid", "part", "partid"}, "", new String[0]);
      this.damageTint = mlc.getBoolean(new String[]{"d", "dmg", "damage", "damagetint"}, false);
      this.color = mlc.getPlaceholderString(new String[]{"c", "color"}, this.damageTint ? "FF6666" : "FFFFFF", new String[0]);
      this.exactMatch = mlc.getBoolean(new String[]{"em", "exact", "match", "exactmatch"}, true);
   }

   public SkillResult castAtEntity(SkillMetadata meta, AbstractEntity target) {
      ModeledEntity model = ModelEngineAPI.getModeledEntity(target.getUniqueId());
      if (model == null) {
         return SkillResult.CONDITION_FAILED;
      } else {
         String colorString = MythicUtils.getOrNull(this.color, meta, target);
         if (colorString.startsWith("#")) {
            colorString = colorString.substring(1);
         }

         Color color = Color.fromRGB(Integer.parseInt(colorString, 16));
         String partId = MythicUtils.getOrNullLowercase(this.partId, meta, target);
         String modelId = MythicUtils.getOrNullLowercase(this.modelId, meta, target);
         MythicUtils.executeOptModelId(model, modelId, (activeModel) -> {
            this.color(activeModel, partId, color);
         });
         return SkillResult.SUCCESS;
      }
   }

   private void color(ActiveModel activeModel, String partId, Color color) {
      if (partId.isBlank()) {
         if (this.damageTint) {
            activeModel.setDamageTint(color);
         } else {
            activeModel.setDefaultTint(color);
         }

      } else if (this.exactMatch) {
         activeModel.getBone(partId).ifPresent((bone) -> {
            if (this.damageTint) {
               bone.setDamageTint(color);
            } else {
               bone.setDefaultTint(color);
            }

         });
      } else {
         Iterator var4 = activeModel.getBones().entrySet().iterator();

         while(var4.hasNext()) {
            Entry<String, ModelBone> entry = (Entry)var4.next();
            if (((String)entry.getKey()).contains(partId)) {
               if (this.damageTint) {
                  ((ModelBone)entry.getValue()).setDamageTint(color);
               } else {
                  ((ModelBone)entry.getValue()).setDefaultTint(color);
               }
            }
         }

      }
   }
}
