package com.ticxo.modelengine.api.error;

import com.ticxo.modelengine.api.utils.logger.LogColor;

public class WarnMultipleAngle extends IError.Warn {
   private final String bone;
   private final String cube;

   public String getErrorMessage() {
      return String.format("Warning: The cube %s in bone %s is rotated in multiple axis. Choosing one axis.", LogColor.BLUE + this.cube + LogColor.YELLOW, LogColor.BLUE + this.bone + LogColor.YELLOW);
   }

   public WarnMultipleAngle(String bone, String cube) {
      this.bone = bone;
      this.cube = cube;
   }
}
