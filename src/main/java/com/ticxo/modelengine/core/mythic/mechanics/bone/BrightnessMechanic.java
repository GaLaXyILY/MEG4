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
import io.lumine.mythic.api.skills.placeholders.PlaceholderInt;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import java.util.Iterator;
import java.util.Map.Entry;

@MythicMechanic(
   name = "brightness",
   aliases = {"light"}
)
public class BrightnessMechanic implements ITargetedEntitySkill {
   private final PlaceholderString modelId;
   private final PlaceholderString partId;
   private final PlaceholderInt blockLight;
   private final PlaceholderInt skyLight;
   private final boolean exactMatch;

   public BrightnessMechanic(MythicLineConfig mlc) {
      this.modelId = mlc.getPlaceholderString(new String[]{"m", "mid", "model", "modelid"}, (String)null, new String[0]);
      this.partId = mlc.getPlaceholderString(new String[]{"p", "pid", "part", "partid"}, "", new String[0]);
      this.blockLight = mlc.getPlaceholderInteger(new String[]{"block", "b"}, -1, new String[0]);
      this.skyLight = mlc.getPlaceholderInteger(new String[]{"sky", "s"}, -1, new String[0]);
      this.exactMatch = mlc.getBoolean(new String[]{"em", "exact", "match", "exactmatch"}, true);
   }

   public SkillResult castAtEntity(SkillMetadata meta, AbstractEntity target) {
      ModeledEntity model = ModelEngineAPI.getModeledEntity(target.getUniqueId());
      if (model == null) {
         return SkillResult.CONDITION_FAILED;
      } else {
         String partId = MythicUtils.getOrNullLowercase(this.partId, meta, target);
         String modelId = MythicUtils.getOrNullLowercase(this.modelId, meta, target);
         int block = this.blockLight.get(meta, target);
         int sky = this.skyLight.get(meta, target);
         MythicUtils.executeOptModelId(model, modelId, (activeModel) -> {
            this.light(activeModel, partId, block, sky);
         });
         return SkillResult.SUCCESS;
      }
   }

   private void light(ActiveModel activeModel, String partId, int block, int sky) {
      if (partId.isBlank()) {
         activeModel.setBlockLight(block);
         activeModel.setSkyLight(sky);
      } else if (this.exactMatch) {
         activeModel.getBone(partId).ifPresent((bone) -> {
            bone.setBlockLight(block);
            bone.setSkyLight(sky);
         });
      } else {
         Iterator var5 = activeModel.getBones().entrySet().iterator();

         while(var5.hasNext()) {
            Entry<String, ModelBone> entry = (Entry)var5.next();
            if (((String)entry.getKey()).contains(partId)) {
               ((ModelBone)entry.getValue()).setBlockLight(block);
               ((ModelBone)entry.getValue()).setSkyLight(sky);
            }
         }

      }
   }
}
