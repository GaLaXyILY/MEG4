package com.ticxo.modelengine.api.error;

import com.ticxo.modelengine.api.utils.logger.LogColor;

public class WarnBadAngle extends IError.Warn {
   private final String bone;
   private final String cube;
   private final double angle;

   public String getErrorMessage() {
      return String.format("Warning: The cube %s in bone %s has illegal rotations. Cube rotation can only be -45, -22.5, 0, 22.5 and 45. [ %s ]", LogColor.BLUE + this.cube + LogColor.YELLOW, LogColor.BLUE + this.bone + LogColor.YELLOW, this.angle);
   }

   public WarnBadAngle(String bone, String cube, double angle) {
      this.bone = bone;
      this.cube = cube;
      this.angle = angle;
   }
}
