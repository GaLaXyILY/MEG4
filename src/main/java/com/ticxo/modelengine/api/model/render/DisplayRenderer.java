package com.ticxo.modelengine.api.model.render;

import com.ticxo.modelengine.api.model.bone.render.renderer.RenderQueues;
import com.ticxo.modelengine.api.utils.data.tracker.CollectionDataTracker;
import com.ticxo.modelengine.api.utils.data.tracker.DataTracker;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.entity.ItemDisplay.ItemDisplayTransform;
import org.bukkit.inventory.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public interface DisplayRenderer extends ModelRenderer, RenderQueues<DisplayRenderer.Bone> {
   int getTick();

   DisplayRenderer.Pivot getPivot();

   DisplayRenderer.Hitbox getHitbox();

   void pushFullUpdate(Player var1);

   boolean pollFullUpdate(Player var1);

   public interface Hitbox {
      int getPivotId();

      UUID getPivotUuid();

      int getHitboxId();

      UUID getHitboxUuid();

      int getShadowId();

      UUID getShadowUuid();

      void clearDirty();

      DataTracker<Vector3f> getPosition();

      DataTracker<Float> getWidth();

      DataTracker<Float> getHeight();

      DataTracker<Float> getShadowRadius();

      DataTracker<Boolean> getHitboxVisible();

      DataTracker<Boolean> getShadowVisible();

      default boolean isHitboxDirty() {
         return this.getWidth().isDirty() || this.getHeight().isDirty();
      }

      default boolean isHitboxVisible() {
         return (Boolean)this.getHitboxVisible().get();
      }

      default boolean isShadowVisible() {
         return (Boolean)this.getShadowVisible().get();
      }

      default boolean isPivotVisible() {
         return this.isHitboxVisible() || this.isShadowVisible();
      }
   }

   public interface Bone {
      int getId();

      UUID getUuid();

      default boolean isDirty() {
         return this.isTransformDirty() || this.isRenderDirty();
      }

      default boolean isTransformDirty() {
         return this.getPosition().isDirty() || this.getLeftRotation().isDirty() || this.getScale().isDirty() || this.getRightRotation().isDirty();
      }

      default boolean isRenderDirty() {
         return this.getModel().isDirty() || this.getDisplay().isDirty() || this.getVisibility().isDirty() || this.getGlowing().isDirty() || this.getGlowColor().isDirty() || this.getBrightness().isDirty() || this.getStep().isDirty();
      }

      default void clearDirty() {
         this.getStep().clearDirty();
         this.getPosition().clearDirty();
         this.getLeftRotation().clearDirty();
         this.getScale().clearDirty();
         this.getRightRotation().clearDirty();
         this.getModel().clearDirty();
         this.getDisplay().clearDirty();
         this.getVisibility().clearDirty();
         this.getGlowing().clearDirty();
         this.getGlowColor().clearDirty();
         this.getBrightness().clearDirty();
      }

      DataTracker<Boolean> getRender();

      DataTracker<Boolean> getStep();

      DataTracker<Vector3f> getPosition();

      DataTracker<Quaternionf> getLeftRotation();

      DataTracker<Vector3f> getScale();

      DataTracker<Quaternionf> getRightRotation();

      DataTracker<ItemStack> getModel();

      DataTracker<ItemDisplayTransform> getDisplay();

      DataTracker<Boolean> getVisibility();

      DataTracker<Boolean> getGlowing();

      DataTracker<Integer> getGlowColor();

      DataTracker<Integer> getBrightness();
   }

   public interface Pivot {
      int getId();

      UUID getUuid();

      void clearDirty();

      DataTracker<Vector3f> getPosition();

      CollectionDataTracker<Integer> getPassengers();
   }
}
