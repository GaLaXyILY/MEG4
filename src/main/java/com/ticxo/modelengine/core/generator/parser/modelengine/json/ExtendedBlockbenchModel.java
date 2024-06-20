package com.ticxo.modelengine.core.generator.parser.modelengine.json;

import com.ticxo.modelengine.api.generator.blueprint.BlueprintBone;
import com.ticxo.modelengine.api.generator.blueprint.ModelBlueprint;
import com.ticxo.modelengine.core.generator.parser.blockbench.json.BlockbenchModel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

public class ExtendedBlockbenchModel extends BlockbenchModel {
   private final Map<UUID, UUID> dupers = new HashMap();
   private final Map<UUID, BlueprintBone> flapBlueprintBoneMap = new HashMap();
   private ModelEngineExtraData extraData;

   protected void finalizeOptions(BlockbenchModel.Bone bone, BlueprintBone blueprintBone) {
      ModelEngineExtraData.BoneProperty data = (ModelEngineExtraData.BoneProperty)this.extraData.getBoneProperties().get(bone.getUuid());
      if (data != null) {
         data.behaviors.forEach((behavior) -> {
            behavior.populate(bone);
         });
         data.customOptions.forEach((customOption) -> {
            customOption.populate(bone);
         });
         if (data.boneScale != null) {
            blueprintBone.setModelScale(data.boneScale);
         }

         if (data.dupeTarget != null) {
            this.dupers.put(bone.getUuid(), data.dupeTarget);
         }

         blueprintBone.setRenderByDefault(data.renderByDefault);
         this.flapBlueprintBoneMap.put(bone.getUuid(), blueprintBone);
      }
   }

   public void populateBlueprint(ModelBlueprint blueprint) {
      super.populateBlueprint(blueprint);
      Iterator var2 = this.dupers.entrySet().iterator();

      while(var2.hasNext()) {
         Entry<UUID, UUID> entry = (Entry)var2.next();
         BlueprintBone dupeBone = (BlueprintBone)this.flapBlueprintBoneMap.get(entry.getKey());
         BlueprintBone targetBone = (BlueprintBone)this.flapBlueprintBoneMap.get(entry.getValue());
         if (dupeBone != null && targetBone != null) {
            dupeBone.setDupeTarget(targetBone);
         }
      }

      this.dupers.clear();
      this.flapBlueprintBoneMap.clear();
   }

   protected boolean shouldGenerate(BlockbenchModel.Bone bbBone) {
      ModelEngineExtraData.BoneProperty data = (ModelEngineExtraData.BoneProperty)this.extraData.getBoneProperties().get(bbBone.getUuid());
      return data == null || data.dupeTarget == null;
   }

   public ModelEngineExtraData getExtraData() {
      return this.extraData;
   }

   public void setExtraData(ModelEngineExtraData extraData) {
      this.extraData = extraData;
   }
}
