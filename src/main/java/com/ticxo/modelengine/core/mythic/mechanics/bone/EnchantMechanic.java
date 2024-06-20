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

@MythicMechanic(
   name = "enchant",
   aliases = {}
)
public class EnchantMechanic implements ITargetedEntitySkill {
   private final PlaceholderString modelId;
   private final PlaceholderString partId;
   private final boolean enchant;
   private final boolean exactMatch;

   public EnchantMechanic(MythicLineConfig mlc) {
      this.modelId = mlc.getPlaceholderString(new String[]{"m", "mid", "model", "modelid"}, (String)null, new String[0]);
      this.partId = mlc.getPlaceholderString(new String[]{"p", "pid", "part", "partid"}, "", new String[0]);
      this.enchant = mlc.getBoolean(new String[]{"en", "enchant"}, true);
      this.exactMatch = mlc.getBoolean(new String[]{"em", "exact", "match", "exactmatch"}, true);
   }

   public SkillResult castAtEntity(SkillMetadata meta, AbstractEntity target) {
      ModeledEntity model = ModelEngineAPI.getModeledEntity(target.getUniqueId());
      if (model == null) {
         return SkillResult.CONDITION_FAILED;
      } else {
         String modelId = MythicUtils.getOrNullLowercase(this.modelId, meta, target);
         String partId = MythicUtils.getOrNullLowercase(this.partId, meta, target);
         MythicUtils.executeOptModelId(model, modelId, (activeModel) -> {
            this.enchant(activeModel, partId);
         });
         return SkillResult.SUCCESS;
      }
   }

   private void enchant(ActiveModel activeModel, String partId) {
      Iterator var3;
      if (!partId.isBlank()) {
         if (this.exactMatch) {
            activeModel.getBone(partId).ifPresent((bone) -> {
               bone.setEnchanted(this.enchant);
            });
         } else {
            var3 = activeModel.getBones().entrySet().iterator();

            while(var3.hasNext()) {
               Entry<String, ModelBone> entry = (Entry)var3.next();
               if (((String)entry.getKey()).contains(partId)) {
                  ((ModelBone)entry.getValue()).setEnchanted(this.enchant);
               }
            }

         }
      } else {
         var3 = activeModel.getBones().values().iterator();

         while(var3.hasNext()) {
            ModelBone value = (ModelBone)var3.next();
            value.setEnchanted(this.enchant);
         }

      }
   }
}
