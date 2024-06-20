package com.ticxo.modelengine.api.error;

import com.ticxo.modelengine.api.utils.logger.LogColor;

public class WarnBoneTooLarge extends IError.Warn {
   private final String bone;
   private double x;
   private double y;
   private double z;

   public String getErrorMessage() {
      return String.format("Warning: The bone %s exceeds the maximum size of 120x120x120. [ %s, %s, %s ]", LogColor.BLUE + this.bone + LogColor.YELLOW, this.x, this.y, this.z);
   }

   public WarnBoneTooLarge(String bone) {
      this.bone = bone;
   }

   public void setX(double x) {
      this.x = x;
   }

   public void setY(double y) {
      this.y = y;
   }

   public void setZ(double z) {
      this.z = z;
   }
}
