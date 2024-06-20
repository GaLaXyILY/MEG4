package com.ticxo.modelengine.core.model.bone.behavior;

import com.ticxo.modelengine.api.model.bone.ModelBone;
import com.ticxo.modelengine.api.model.bone.behavior.AbstractBoneBehavior;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehaviorData;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehaviorType;

public class Dummy extends AbstractBoneBehavior<Dummy> {
   public Dummy(ModelBone bone, BoneBehaviorType<Dummy> type, BoneBehaviorData data) {
      super(bone, type, data);
   }
}
