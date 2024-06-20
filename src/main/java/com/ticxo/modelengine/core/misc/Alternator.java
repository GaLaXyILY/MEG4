package com.ticxo.modelengine.core.misc;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Alternator {
   private static final Quaternionf IDENTITY = new Quaternionf();
   private final int tick;
   private final ItemDisplay displayA;
   private final ItemDisplay displayB;
   private boolean isAVisible;
   private boolean queueNextTickToggle;
   private Vector3f position = new Vector3f();
   private boolean positionStep;
   private Quaternionf rotation = new Quaternionf();
   private boolean rotationStep;
   private Vector3f scale = new Vector3f(1.0F, 1.0F, 1.0F);
   private boolean scaleStep;

   public Alternator(Location location, int tick) {
      this.tick = tick;
      this.displayA = (ItemDisplay)location.getWorld().spawn(location, ItemDisplay.class, (itemDisplay) -> {
         itemDisplay.setInterpolationDuration(tick);
         itemDisplay.setItemStack(new ItemStack(Material.CARVED_PUMPKIN));
      });
      this.displayB = (ItemDisplay)location.getWorld().spawn(location, ItemDisplay.class, (itemDisplay) -> {
         itemDisplay.setInterpolationDuration(tick);
         itemDisplay.setItemStack(new ItemStack(Material.FURNACE));
         itemDisplay.setViewRange(0.0F);
      });
      this.isAVisible = true;
   }

   public void tick() {
      this.tickSwap();
      ItemDisplay visible = this.getVisible();
      ItemDisplay hidden = this.getHidden();
      if (!this.positionStep && !this.rotationStep && !this.scaleStep) {
         Transformation transformation = new Transformation(this.position, this.rotation, this.scale, IDENTITY);
         visible.setTransformation(transformation);
         visible.setInterpolationDelay(0);
         visible.setInterpolationDuration(this.tick);
      } else {
         Vector3f dPos = this.positionStep ? visible.getTransformation().getTranslation() : this.position;
         Quaternionf dRot = this.rotationStep ? visible.getTransformation().getLeftRotation() : this.rotation;
         Vector3f dSca = this.scaleStep ? visible.getTransformation().getScale() : this.scale;
         Transformation visibleTransform = new Transformation(dPos, dRot, dSca, IDENTITY);
         Transformation hiddenTransform = new Transformation(this.position, this.rotation, this.scale, IDENTITY);
         if (!visible.getTransformation().equals(visibleTransform)) {
            visible.setTransformation(visibleTransform);
            visible.setInterpolationDelay(0);
            visible.setInterpolationDuration(this.tick);
         }

         if (!hidden.getTransformation().equals(hiddenTransform)) {
            hidden.setTransformation(hiddenTransform);
            hidden.setInterpolationDelay(0);
            hidden.setInterpolationDuration(0);
         }

         this.queueNextTickToggle = true;
         this.positionStep = false;
         this.rotationStep = false;
         this.scaleStep = false;
      }

   }

   public void tickSwap() {
      if (this.queueNextTickToggle) {
         this.swapVisibility();
         this.queueNextTickToggle = false;
      }

   }

   public ItemDisplay getVisible() {
      return this.isAVisible ? this.displayA : this.displayB;
   }

   public ItemDisplay getHidden() {
      return !this.isAVisible ? this.displayA : this.displayB;
   }

   public void swapVisibility() {
      this.getVisible().setViewRange(0.0F);
      this.getHidden().setViewRange(1.0F);
      this.isAVisible = !this.isAVisible;
   }

   public boolean isDead() {
      return this.displayA.isDead() || this.displayB.isDead();
   }

   public Alternator lerpPosition(Vector3f position) {
      this.position = position;
      this.positionStep = false;
      return this;
   }

   public Alternator lerpRotation(Quaternionf rotation) {
      this.rotation = rotation;
      this.rotationStep = false;
      return this;
   }

   public Alternator lerpScale(Vector3f scale) {
      this.scale = scale;
      this.scaleStep = false;
      return this;
   }

   public Alternator stepPosition(Vector3f position) {
      this.position = position;
      this.positionStep = true;
      return this;
   }

   public Alternator stepRotation(Quaternionf rotation) {
      this.rotation = rotation;
      this.rotationStep = true;
      return this;
   }

   public Alternator stepScale(Vector3f scale) {
      this.scale = scale;
      this.scaleStep = true;
      return this;
   }
}
