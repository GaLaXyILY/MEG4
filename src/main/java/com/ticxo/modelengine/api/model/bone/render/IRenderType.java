package com.ticxo.modelengine.api.model.bone.render;

import com.ticxo.modelengine.api.model.ActiveModel;
import org.jetbrains.annotations.Nullable;

public interface IRenderType {
   @Nullable
   BehaviorRenderer createBehaviorRenderer(ActiveModel var1);
}
