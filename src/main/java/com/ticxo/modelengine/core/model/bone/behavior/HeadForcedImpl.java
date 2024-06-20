package com.ticxo.modelengine.core.model.bone.behavior;

import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.model.bone.ModelBone;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehaviorData;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehaviorType;
import com.ticxo.modelengine.api.utils.math.TMath;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

public class HeadForcedImpl extends HeadImpl {
   protected boolean inherited;

   public HeadForcedImpl(ModelBone bone, BoneBehaviorType<HeadImpl> type, BoneBehaviorData data) {
      super(bone, type, data);
      this.onParentSwap(bone.getParent());
   }

   public void onParentSwap(@Nullable ModelBone parent) {
      this.shouldRotate = false;
      if (parent != null) {
         Optional<HeadImpl> maybeBehavior = parent.getBoneBehavior(this.type);
         maybeBehavior.ifPresent((head) -> {
            this.inherited = head.inherited;
            if (!this.inherited && !(head instanceof HeadForcedImpl)) {
               this.shouldRotate = true;
               this.local = head.isLocal();
            }
         });
      }
   }

   public void postGlobalCalculation() {
      if (this.shouldRotate) {
         ActiveModel activeModel = this.bone.getActiveModel();
         ModeledEntity modeledEntity = activeModel.getModeledEntity();
         this.bone.setYaw(modeledEntity.getYBodyRot());
         float yaw = -TMath.degreeDifference(modeledEntity.getYBodyRot(), activeModel.getYHeadRot());
         float pitch = -activeModel.getXHeadRot();
         Quaternionf q = (new Quaternionf()).rotateX(-pitch * 0.017453292F).rotateY(-yaw * 0.017453292F);
         if (this.local) {
            this.bone.getGlobalLeftRotation().mul(q);
         } else {
            q.mul(this.bone.getGlobalLeftRotation(), this.bone.getGlobalLeftRotation());
         }

      }
   }

   public boolean isHidden() {
      return true;
   }

   public boolean isInherited() {
      return this.inherited;
   }

   public void setInherited(boolean inherited) {
      this.inherited = inherited;
   }
}
