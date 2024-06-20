package com.ticxo.modelengine.core.mythic.mechanics.bone;

import com.google.common.collect.Lists;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.generator.blueprint.BlueprintBone;
import com.ticxo.modelengine.api.generator.blueprint.ModelBlueprint;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.model.bone.ModelBone;
import com.ticxo.modelengine.api.utils.scheduling.PlatformTask;
import com.ticxo.modelengine.core.mythic.MythicUtils;
import com.ticxo.modelengine.core.mythic.utils.MythicMechanic;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import io.lumine.mythic.bukkit.utils.terminable.Terminable;
import java.util.Arrays;
import java.util.List;
import org.bukkit.entity.Entity;

@MythicMechanic(
   name = "cycleparts",
   aliases = {}
)
public class CyclePartsMechanic implements ITargetedEntitySkill {
   private final PlaceholderString modelId;
   private final PlaceholderString partId;
   private final PlaceholderString nModelId;
   private final List<String> cycledParts = Lists.newArrayList();
   private final int startingFrame = 0;
   private final int interval;
   private final int duration;

   public CyclePartsMechanic(MythicLineConfig mlc) {
      this.modelId = mlc.getPlaceholderString(new String[]{"m", "mid", "model", "modelid"}, (String)null, new String[0]);
      this.partId = mlc.getPlaceholderString(new String[]{"p", "pid", "part", "partid"}, (String)null, new String[0]);
      this.nModelId = mlc.getPlaceholderString(new String[]{"nm", "nmid", "newmodel", "newmodelid"}, (String)null, new String[0]);
      String nPartId = mlc.getString(new String[]{"np", "npid", "newpart", "newpartid", "newparts", "cycledparts"}, "", new String[0]);
      this.cycledParts.addAll(Arrays.asList(nPartId.split(",")));
      this.interval = mlc.getInteger(new String[]{"interval", "i"}, 1);
      this.duration = mlc.getInteger(new String[]{"duration", "d"}, this.cycledParts.size() - 1);
   }

   public SkillResult castAtEntity(SkillMetadata meta, AbstractEntity target) {
      ModeledEntity model = ModelEngineAPI.getModeledEntity(target.getUniqueId());
      if (model == null) {
         return SkillResult.CONDITION_FAILED;
      } else {
         String modelId = MythicUtils.getOrNullLowercase(this.modelId, meta, target);
         String partId = MythicUtils.getOrNullLowercase(this.partId, meta, target);
         String nModelId = MythicUtils.getOrNullLowercase(this.nModelId, meta, target);
         model.getModel(modelId).flatMap((activeModel) -> {
            return activeModel.getBone(partId);
         }).ifPresent((bone) -> {
            if (bone.isRenderer()) {
               ModelBlueprint blueprint = ModelEngineAPI.getBlueprint(nModelId);
               if (blueprint != null) {
                  new CyclePartsMechanic.Animator(meta, target, bone, blueprint);
               }
            }
         });
         return SkillResult.SUCCESS;
      }
   }

   private class Animator implements Runnable, Terminable {
      private final SkillMetadata data;
      private final AbstractEntity target;
      private final ModelBone bone;
      private final ModelBlueprint modelBlueprint;
      private final PlatformTask task;
      private int frame = 0;
      private int iteration = 0;

      public Animator(SkillMetadata data, AbstractEntity target, ModelBone bone, ModelBlueprint modelBlueprint) {
         this.data = data;
         this.target = target;
         this.bone = bone;
         this.modelBlueprint = modelBlueprint;
         this.task = ModelEngineAPI.getAPI().getScheduler().scheduleRepeating(ModelEngineAPI.getAPI(), (Entity)target.getBukkitEntity(), this, 0L, (long)CyclePartsMechanic.this.interval);
      }

      public void run() {
         if (this.target.isDead()) {
            this.terminate();
         } else if (this.iteration++ > CyclePartsMechanic.this.duration) {
            this.terminate();
         } else {
            String nextPart = (String)CyclePartsMechanic.this.cycledParts.get(this.frame);
            BlueprintBone data = (BlueprintBone)this.modelBlueprint.getFlatMap().get(nextPart);
            if (data == null) {
               this.terminate();
            } else {
               this.bone.setModel(data.getDataId());
               this.bone.setModelScale(data.getScale());
               this.frame = (this.frame + 1) % CyclePartsMechanic.this.cycledParts.size();
            }
         }
      }

      public void close() {
         this.task.cancel();
      }
   }
}
