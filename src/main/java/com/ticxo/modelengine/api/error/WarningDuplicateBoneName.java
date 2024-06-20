package com.ticxo.modelengine.api.error;

import com.ticxo.modelengine.api.utils.logger.LogColor;

public class WarningDuplicateBoneName extends IError.Warn {
   private final String bone;

   public String getErrorMessage() {
      return String.format("Warning: Model contains duplicate bone names %s.", LogColor.BLUE + this.bone + LogColor.YELLOW);
   }

   public WarningDuplicateBoneName(String bone) {
      this.bone = bone;
   }
}
