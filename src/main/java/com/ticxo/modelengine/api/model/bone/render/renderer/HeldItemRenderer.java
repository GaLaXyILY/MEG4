package com.ticxo.modelengine.api.model.bone.render.renderer;

import com.ticxo.modelengine.api.model.bone.render.BehaviorRenderer;
import com.ticxo.modelengine.api.utils.data.tracker.CollectionDataTracker;
import com.ticxo.modelengine.api.utils.data.tracker.DataTracker;
import java.util.UUID;
import org.bukkit.entity.ItemDisplay.ItemDisplayTransform;
import org.bukkit.inventory.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public interface HeldItemRenderer extends BehaviorRenderer, RenderQueues<HeldItemRenderer.Item> {
   int getId();

   UUID getUuid();

   CollectionDataTracker<Integer> getPassengers();

   public interface Item {
      int getId();

      UUID getUuid();

      DataTracker<Vector3f> getPosition();

      DataTracker<Vector3f> getScale();

      DataTracker<Quaternionf> getRotation();

      DataTracker<ItemStack> getModel();

      DataTracker<ItemDisplayTransform> getDisplay();

      DataTracker<Boolean> getGlowing();

      DataTracker<Integer> getGlowColor();

      default boolean isTransformDirty() {
         return this.getPosition().isDirty() || this.getScale().isDirty() || this.getRotation().isDirty();
      }

      default boolean isRenderDirty() {
         return this.getModel().isDirty() || this.getDisplay().isDirty() || this.getGlowing().isDirty() || this.getGlowColor().isDirty();
      }

      default boolean isDirty() {
         return this.isTransformDirty() || this.isRenderDirty();
      }

      default void clearDirty() {
         this.getPosition().clearDirty();
         this.getScale().clearDirty();
         this.getRotation().clearDirty();
         this.getModel().clearDirty();
         this.getDisplay().clearDirty();
         this.getGlowing().clearDirty();
         this.getGlowColor().clearDirty();
      }
   }
}
