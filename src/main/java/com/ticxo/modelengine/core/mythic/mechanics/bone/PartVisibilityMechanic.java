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
import java.util.LinkedList;
import java.util.Map.Entry;

@MythicMechanic(
   name = "partvisibility",
   aliases = {"partvis"}
)
public class PartVisibilityMechanic implements ITargetedEntitySkill {
   private final PlaceholderString modelId;
   private final PlaceholderString partId;
   private final boolean visible;
   private final boolean exactMatch;
   private final boolean child;

   public PartVisibilityMechanic(MythicLineConfig mlc) {
      this.modelId = mlc.getPlaceholderString(new String[]{"m", "mid", "model", "modelid"}, (String)null, new String[0]);
      this.partId = mlc.getPlaceholderString(new String[]{"p", "pid", "part", "partid"}, "", new String[0]);
      this.visible = mlc.getBoolean(new String[]{"v", "visible", "visibility"}, false);
      this.exactMatch = mlc.getBoolean(new String[]{"em", "exact", "match", "exactmatch"}, true);
      this.child = mlc.getBoolean(new String[]{"c", "child"}, false);
   }

   public SkillResult castAtEntity(SkillMetadata meta, AbstractEntity target) {
      ModeledEntity model = ModelEngineAPI.getModeledEntity(target.getUniqueId());
      if (model == null) {
         return SkillResult.CONDITION_FAILED;
      } else {
         String partId = MythicUtils.getOrNullLowercase(this.partId, meta, target);
         String modelId = MythicUtils.getOrNullLowercase(this.modelId, meta, target);
         MythicUtils.executeOptModelId(model, modelId, (activeModel) -> {
            this.partVis(activeModel, partId);
         });
         return SkillResult.SUCCESS;
      }
   }

   private void partVis(ActiveModel activeModel, String partId) {
      if (this.exactMatch) {
         activeModel.getBone(partId).ifPresent(this::setVis);
      } else {
         Iterator var3 = activeModel.getBones().entrySet().iterator();

         while(var3.hasNext()) {
            Entry<String, ModelBone> bone = (Entry)var3.next();
            if (((String)bone.getKey()).contains(partId)) {
               this.setVis((ModelBone)bone.getValue());
            }
         }
      }

   }

   private void setVis(ModelBone bone) {
      bone.setVisible(this.visible);
      if (this.child) {
         LinkedList queue = new LinkedList(bone.getChildren().values());

         while(!queue.isEmpty()) {
            ModelBone childBone = (ModelBone)queue.pop();
            childBone.setVisible(this.visible);
            queue.addAll(childBone.getChildren().values());
         }

      }
   }
}
