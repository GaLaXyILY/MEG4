package com.ticxo.modelengine.api.animation.keyframe.type;

import com.ticxo.modelengine.api.animation.keyframe.data.IKeyframeData;

public interface IVectorType {
   IVectorType setX(IKeyframeData var1);

   IVectorType setY(IKeyframeData var1);

   IVectorType setZ(IKeyframeData var1);

   IVectorType setPostX(IKeyframeData var1);

   IVectorType setPostY(IKeyframeData var1);

   IVectorType setPostZ(IKeyframeData var1);

   IVectorType setXFactor(float var1);

   IVectorType setYFactor(float var1);

   IVectorType setZFactor(float var1);
}
