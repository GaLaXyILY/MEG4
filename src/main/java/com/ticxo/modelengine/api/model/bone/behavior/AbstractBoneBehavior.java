package com.ticxo.modelengine.api.model.bone.behavior;

import com.ticxo.modelengine.api.model.bone.ModelBone;

public abstract class AbstractBoneBehavior<T extends BoneBehavior> implements BoneBehavior {
   protected final ModelBone bone;
   protected final BoneBehaviorType<T> type;
   protected final BoneBehaviorData data;

   public ModelBone getBone() {
      return this.bone;
   }

   public BoneBehaviorType<T> getType() {
      return this.type;
   }

   public BoneBehaviorData getData() {
      return this.data;
   }

   public AbstractBoneBehavior(ModelBone bone, BoneBehaviorType<T> type, BoneBehaviorData data) {
      this.bone = bone;
      this.type = type;
      this.data = data;
   }
}
