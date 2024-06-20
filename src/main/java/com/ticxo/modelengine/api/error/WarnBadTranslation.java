package com.ticxo.modelengine.api.error;

import com.ticxo.modelengine.api.utils.logger.LogColor;

public class WarnBadTranslation extends IError.Warn {
   private final String bone;
   private double x;
   private double y;
   private double z;

   public String getErrorMessage() {
      return String.format("Warning: The bone %s is translated too far away from the pivot. Maximum translation is 80x80x80. [ %s, %s, %s ]", LogColor.BLUE + this.bone + LogColor.YELLOW, this.x, this.y, this.z);
   }

   public WarnBadTranslation(String bone) {
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
