package com.ticxo.modelengine.core.mythic.mechanics.nametag;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.model.bone.BoneBehaviorTypes;
import com.ticxo.modelengine.api.model.bone.type.NameTag;
import com.ticxo.modelengine.core.mythic.MythicUtils;
import com.ticxo.modelengine.core.mythic.utils.MythicMechanic;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;

@MythicMechanic(
   name = "setmodeltagvisible",
   aliases = {}
)
public class SetNametagVisibilityMechanic implements ITargetedEntitySkill {
   private final PlaceholderString modelId;
   private final PlaceholderString partId;
   private final boolean visible;

   public SetNametagVisibilityMechanic(MythicLineConfig mlc) {
      this.modelId = mlc.getPlaceholderString(new String[]{"m", "mid", "model", "modelid"}, (String)null, new String[0]);
      this.partId = mlc.getPlaceholderString(new String[]{"b", "bone", "p", "pid", "part", "partid"}, (String)null, new String[0]);
      this.visible = mlc.getBoolean(new String[]{"v", "visible"}, true);
   }

   public SkillResult castAtEntity(SkillMetadata meta, AbstractEntity target) {
      ModeledEntity model = ModelEngineAPI.getModeledEntity(target.getUniqueId());
      if (model == null) {
         return SkillResult.CONDITION_FAILED;
      } else {
         String modelId = MythicUtils.getOrNullLowercase(this.modelId, meta, target);
         model.getModel(modelId).ifPresent((activeModel) -> {
            String partId = MythicUtils.getOrNullLowercase(this.partId, meta, target);
            activeModel.getBone(partId).flatMap((modelBone) -> {
               return modelBone.getBoneBehavior(BoneBehaviorTypes.NAMETAG);
            }).ifPresent((nameTag) -> {
               ((NameTag)nameTag).setVisible(this.visible);
            });
         });
         return SkillResult.SUCCESS;
      }
   }
}
