package com.ticxo.modelengine.api.animation.keyframe.type;

import com.ticxo.modelengine.api.animation.keyframe.data.IKeyframeData;
import com.ticxo.modelengine.api.animation.property.IAnimationProperty;
import org.joml.Vector3f;

public class VectorKeyframe extends AbstractKeyframe<Vector3f> implements IVectorType {
   private final IKeyframeData[] preVector = new IKeyframeData[3];
   private final IKeyframeData[] postVector = new IKeyframeData[3];
   private boolean isDiscontinuous;
   private float xFactor = 1.0F;
   private float yFactor = 1.0F;
   private float zFactor = 1.0F;

   public VectorKeyframe setX(IKeyframeData x) {
      this.preVector[0] = x;
      return this;
   }

   public VectorKeyframe setY(IKeyframeData y) {
      this.preVector[1] = y;
      return this;
   }

   public VectorKeyframe setZ(IKeyframeData z) {
      this.preVector[2] = z;
      return this;
   }

   public VectorKeyframe setPostX(IKeyframeData x) {
      this.postVector[0] = x;
      return this;
   }

   public VectorKeyframe setPostY(IKeyframeData y) {
      this.postVector[1] = y;
      return this;
   }

   public VectorKeyframe setPostZ(IKeyframeData z) {
      this.postVector[2] = z;
      return this;
   }

   public IVectorType setXFactor(float x) {
      this.xFactor = x;
      return this;
   }

   public IVectorType setYFactor(float y) {
      this.yFactor = y;
      return this;
   }

   public IVectorType setZFactor(float z) {
      this.zFactor = z;
      return this;
   }

   public Vector3f getValue(int index, IAnimationProperty property) {
      return index != 0 && this.isDiscontinuous ? new Vector3f((float)this.postVector[0].getValue(property) * this.xFactor, (float)this.postVector[1].getValue(property) * this.yFactor, (float)this.postVector[2].getValue(property) * this.zFactor) : new Vector3f((float)this.preVector[0].getValue(property) * this.xFactor, (float)this.preVector[1].getValue(property) * this.yFactor, (float)this.preVector[2].getValue(property) * this.zFactor);
   }

   public IKeyframeData[] getPreVector() {
      return this.preVector;
   }

   public IKeyframeData[] getPostVector() {
      return this.postVector;
   }

   public boolean isDiscontinuous() {
      return this.isDiscontinuous;
   }

   public float getXFactor() {
      return this.xFactor;
   }

   public float getYFactor() {
      return this.yFactor;
   }

   public float getZFactor() {
      return this.zFactor;
   }

   public void setDiscontinuous(boolean isDiscontinuous) {
      this.isDiscontinuous = isDiscontinuous;
   }
}
