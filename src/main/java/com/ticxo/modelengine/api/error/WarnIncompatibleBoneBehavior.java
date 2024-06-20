package com.ticxo.modelengine.api.error;

import com.ticxo.modelengine.api.utils.logger.LogColor;

public class WarnIncompatibleBoneBehavior extends IError.Warn {
   private final String boneName;
   private final String id;

   public String getErrorMessage() {
      return String.format("Warning: Bone behavior %s on bone %s detected incompatibility with other bone behaviors. Beware of bugs and glitches.", LogColor.BLUE + this.id + LogColor.YELLOW, LogColor.BLUE + this.boneName + LogColor.YELLOW);
   }

   public WarnIncompatibleBoneBehavior(String boneName, String id) {
      this.boneName = boneName;
      this.id = id;
   }
}
