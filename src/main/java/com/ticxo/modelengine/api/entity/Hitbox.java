package com.ticxo.modelengine.api.entity;

import com.ticxo.modelengine.api.utils.MiscUtils;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public class Hitbox {
   private final double width;
   private final double height;
   private final double depth;
   private final double eyeHeight;

   public Hitbox clone() {
      return new Hitbox(this.width, this.height, this.depth, this.eyeHeight);
   }

   public double getMaxWidth() {
      return Math.max(this.width, this.depth);
   }

   public BoundingBox createBoundingBox(Vector vector) {
      return new BoundingBox(vector.getX() - this.width * 0.5D, vector.getY(), vector.getZ() - this.depth * 0.5D, vector.getX() + this.width * 0.5D, vector.getY() + this.height, vector.getZ() + this.depth * 0.5D);
   }

   public String toSimpleString() {
      String var10000 = MiscUtils.FORMATTER.format(this.width);
      return var10000 + " x " + MiscUtils.FORMATTER.format(this.height) + " x " + MiscUtils.FORMATTER.format(this.width);
   }

   public String toEyeHeightString() {
      return MiscUtils.FORMATTER.format(this.eyeHeight);
   }

   public Hitbox(double width, double height, double depth, double eyeHeight) {
      this.width = width;
      this.height = height;
      this.depth = depth;
      this.eyeHeight = eyeHeight;
   }

   public double getWidth() {
      return this.width;
   }

   public double getHeight() {
      return this.height;
   }

   public double getDepth() {
      return this.depth;
   }

   public double getEyeHeight() {
      return this.eyeHeight;
   }

   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof Hitbox)) {
         return false;
      } else {
         Hitbox other = (Hitbox)o;
         if (!other.canEqual(this)) {
            return false;
         } else if (Double.compare(this.getWidth(), other.getWidth()) != 0) {
            return false;
         } else if (Double.compare(this.getHeight(), other.getHeight()) != 0) {
            return false;
         } else if (Double.compare(this.getDepth(), other.getDepth()) != 0) {
            return false;
         } else {
            return Double.compare(this.getEyeHeight(), other.getEyeHeight()) == 0;
         }
      }
   }

   protected boolean canEqual(Object other) {
      return other instanceof Hitbox;
   }

   public int hashCode() {
      int PRIME = true;
      int result = 1;
      long $width = Double.doubleToLongBits(this.getWidth());
      int result = result * 59 + (int)($width >>> 32 ^ $width);
      long $height = Double.doubleToLongBits(this.getHeight());
      result = result * 59 + (int)($height >>> 32 ^ $height);
      long $depth = Double.doubleToLongBits(this.getDepth());
      result = result * 59 + (int)($depth >>> 32 ^ $depth);
      long $eyeHeight = Double.doubleToLongBits(this.getEyeHeight());
      result = result * 59 + (int)($eyeHeight >>> 32 ^ $eyeHeight);
      return result;
   }

   public String toString() {
      double var10000 = this.getWidth();
      return "Hitbox(width=" + var10000 + ", height=" + this.getHeight() + ", depth=" + this.getDepth() + ", eyeHeight=" + this.getEyeHeight() + ")";
   }
}
