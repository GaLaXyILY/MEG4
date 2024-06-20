package com.ticxo.modelengine.core.model.bone.behavior;

import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.model.bone.ModelBone;
import com.ticxo.modelengine.api.model.bone.behavior.AbstractBoneBehavior;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehaviorData;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehaviorType;
import com.ticxo.modelengine.api.model.bone.type.Head;
import com.ticxo.modelengine.api.utils.math.TMath;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

public class HeadImpl extends AbstractBoneBehavior<HeadImpl> implements Head {
   protected boolean shouldRotate;
   protected boolean inherited;
   protected boolean local;

   public HeadImpl(ModelBone bone, BoneBehaviorType<HeadImpl> type, BoneBehaviorData data) {
      super(bone, type, data);
      this.onParentSwap(bone.getParent());
      this.local = (Boolean)data.get("local", false);
      this.inherited = (Boolean)data.get("inherited", false);
   }

   public void onParentSwap(@Nullable ModelBone parent) {
      for(this.shouldRotate = true; parent != null; parent = parent.getParent()) {
         Optional<HeadImpl> maybeBehavior = parent.getBoneBehavior(this.type);
         this.shouldRotate = maybeBehavior.isEmpty() || maybeBehavior.get() instanceof HeadForcedImpl;
         if (!this.shouldRotate) {
            return;
         }
      }

   }

   public void postGlobalCalculation() {
      if (this.shouldRotate) {
         ActiveModel activeModel = this.bone.getActiveModel();
         ModeledEntity modeledEntity = activeModel.getModeledEntity();
         this.bone.setYaw(activeModel.getYHeadRot());
         float yaw = TMath.degreeDifference(modeledEntity.getYBodyRot(), activeModel.getYHeadRot());
         float pitch = activeModel.getXHeadRot();
         Quaternionf q = (new Quaternionf()).rotateY(-yaw * 0.017453292F).rotateX(-pitch * 0.017453292F);
         if (this.local) {
            this.bone.getGlobalLeftRotation().mul(q);
         } else {
            q.mul(this.bone.getGlobalLeftRotation(), this.bone.getGlobalLeftRotation());
         }

      }
   }

   public boolean isInherited() {
      return this.inherited;
   }

   public void setInherited(boolean inherited) {
      this.inherited = inherited;
   }

   public boolean isLocal() {
      return this.local;
   }

   public void setLocal(boolean local) {
      this.local = local;
   }
}
