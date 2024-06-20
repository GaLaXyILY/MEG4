package com.ticxo.modelengine.core.mythic.mechanics.entity;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.nms.entity.wrapper.BodyRotationController;
import com.ticxo.modelengine.core.mythic.utils.MythicMechanic;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderFloat;
import io.lumine.mythic.api.skills.placeholders.PlaceholderInt;

@MythicMechanic(
   name = "bodyrotation",
   aliases = {"bodyclamp"}
)
public class BodyRotationMechanic implements ITargetedEntitySkill {
   private final MythicLineConfig config;

   public BodyRotationMechanic(MythicLineConfig mlc) {
      this.config = mlc;
   }

   public SkillResult castAtEntity(SkillMetadata meta, AbstractEntity target) {
      ModeledEntity model = ModelEngineAPI.getModeledEntity(target.getUniqueId());
      if (model == null) {
         return SkillResult.CONDITION_FAILED;
      } else {
         BodyRotationController controller = model.getBase().getBodyRotationController();
         boolean headUneven = this.config.getBoolean(new String[]{"hu", "head", "headuneven"}, controller.isHeadClampUneven());
         PlaceholderFloat maxHeadAngle = this.config.getPlaceholderFloat(new String[]{"mh", "mxh", "maxhead"}, controller.getMaxHeadAngle(), new String[0]);
         PlaceholderFloat minHeadAngle = this.config.getPlaceholderFloat(new String[]{"mnh", "minhead"}, controller.getMinHeadAngle(), new String[0]);
         boolean bodyUneven = this.config.getBoolean(new String[]{"bu", "body", "bodyuneven"}, controller.isBodyClampUneven());
         PlaceholderFloat maxBodyAngle = this.config.getPlaceholderFloat(new String[]{"mb", "mxb", "maxbody"}, controller.getMaxBodyAngle(), new String[0]);
         PlaceholderFloat minBodyAngle = this.config.getPlaceholderFloat(new String[]{"mnb", "minbody"}, controller.getMinBodyAngle(), new String[0]);
         boolean mode = this.config.getBoolean(new String[]{"m", "mode", "player", "playermode"}, controller.isPlayerMode());
         PlaceholderFloat stableAngle = this.config.getPlaceholderFloat(new String[]{"s", "stable"}, controller.getStableAngle(), new String[0]);
         PlaceholderInt rotationDelay = this.config.getPlaceholderInteger(new String[]{"rde", "rdelay"}, controller.getRotationDelay(), new String[0]);
         PlaceholderInt rotationDuration = this.config.getPlaceholderInteger(new String[]{"rdu", "rduration"}, controller.getRotationDuration(), new String[0]);
         controller.setHeadClampUneven(headUneven);
         controller.setMaxHeadAngle(maxHeadAngle.get(meta, target));
         controller.setMinHeadAngle(minHeadAngle.get(meta, target));
         controller.setBodyClampUneven(bodyUneven);
         controller.setMaxBodyAngle(maxBodyAngle.get(meta, target));
         controller.setMinBodyAngle(minBodyAngle.get(meta, target));
         controller.setPlayerMode(mode);
         controller.setStableAngle(stableAngle.get(meta, target));
         controller.setRotationDelay(rotationDelay.get(meta, target));
         controller.setRotationDuration(rotationDuration.get(meta, target));
         return SkillResult.SUCCESS;
      }
   }
}
