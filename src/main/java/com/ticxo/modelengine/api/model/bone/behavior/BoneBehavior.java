package com.ticxo.modelengine.api.model.bone.behavior;

import com.ticxo.modelengine.api.model.bone.ModelBone;
import com.ticxo.modelengine.api.utils.data.io.DataIO;
import com.ticxo.modelengine.api.utils.data.io.SavedData;
import org.jetbrains.annotations.Nullable;

public interface BoneBehavior extends DataIO {
   ModelBone getBone();

   BoneBehaviorType<?> getType();

   BoneBehaviorData getData();

   default void onApply() {
   }

   default void onRemove() {
   }

   default void onParentSwap(@Nullable ModelBone parent) {
   }

   default void preAnimation() {
   }

   default void onAnimation() {
   }

   default void postAnimation() {
   }

   default void preGlobalCalculation() {
   }

   default void onGlobalCalculation() {
   }

   default void postGlobalCalculation() {
   }

   default void preChildCalculation() {
   }

   default void postChildCalculation() {
   }

   default void onFinalize() {
   }

   default void preRender() {
   }

   default void onRender() {
   }

   default void postRender() {
   }

   default boolean isHidden() {
      return false;
   }

   default void save(SavedData data) {
   }

   default void load(SavedData data) {
   }
}
