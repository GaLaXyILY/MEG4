package com.ticxo.modelengine.api.model.bone.manager;

import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehavior;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehaviorType;

public interface BehaviorManager<T extends BoneBehavior> {
   ActiveModel getActiveModel();

   BoneBehaviorType<T> getType();

   default void onCreate() {
   }

   default void onDestroy() {
   }

   default void preBoneTick() {
   }

   default void postBoneTick() {
   }

   default void preScriptTick() {
   }

   default void postScriptTick() {
   }

   default void preBoneRender() {
   }

   default void postBoneRender() {
   }
}
