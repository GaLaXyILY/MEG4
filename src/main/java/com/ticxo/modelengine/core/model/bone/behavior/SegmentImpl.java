package com.ticxo.modelengine.core.model.bone.behavior;

import com.ticxo.modelengine.api.model.bone.ModelBone;
import com.ticxo.modelengine.api.model.bone.behavior.AbstractBoneBehavior;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehaviorData;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehaviorType;
import com.ticxo.modelengine.api.model.bone.type.Segment;
import org.bukkit.Location;
import org.joml.Vector3f;

public class SegmentImpl extends AbstractBoneBehavior<SegmentImpl> implements Segment {
   private Vector3f originalLocation;

   public SegmentImpl(ModelBone bone, BoneBehaviorType<SegmentImpl> type, BoneBehaviorData data) {
      super(bone, type, data);
   }

   public void onApply() {
      this.originalLocation = this.getWorldLocation();
   }

   public void onGlobalCalculation() {
      this.bone.calculateGlobalTransform();
   }

   public void onFinalize() {
      Vector3f worldLocation = this.getWorldLocation();
      this.originalLocation.sub(worldLocation, worldLocation);
      this.bone.setGlobalPosition(worldLocation.rotateY((this.bone.getYaw() - 180.0F) * 0.017453292F));
   }

   private Vector3f getWorldLocation() {
      Vector3f globalPosition = this.bone.getGlobalPosition().rotateY((180.0F - this.bone.getYaw()) * 0.017453292F, new Vector3f());
      Location location = this.bone.getActiveModel().getModeledEntity().getBase().getLocation();
      return globalPosition.add((float)location.getX(), (float)location.getY(), (float)location.getZ());
   }
}
