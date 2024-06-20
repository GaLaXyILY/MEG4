package com.ticxo.modelengine.api.vfx.render;

import com.ticxo.modelengine.api.utils.data.tracker.DataTracker;
import java.util.UUID;
import org.bukkit.inventory.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public interface VFXDisplayRenderer extends VFXRenderer {
   boolean isRespawnRequired();

   void setRespawnRequired(boolean var1);

   VFXDisplayRenderer.VFXModel getVFXModel();

   public interface VFXModel {
      int getPivotId();

      UUID getPivotUuid();

      int getModelId();

      UUID getModelUuid();

      default void clearModelDirty() {
         this.getPosition().clearDirty();
         this.getLeftRotation().clearDirty();
         this.getScale().clearDirty();
         this.getModel().clearDirty();
      }

      default boolean isModelDirty() {
         return this.getPosition().isDirty() || this.getLeftRotation().isDirty() || this.getScale().isDirty() || this.getModel().isDirty();
      }

      DataTracker<Vector3f> getOrigin();

      DataTracker<Vector3f> getPosition();

      DataTracker<Quaternionf> getLeftRotation();

      DataTracker<Vector3f> getScale();

      DataTracker<ItemStack> getModel();
   }
}
