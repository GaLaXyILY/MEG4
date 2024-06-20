package com.ticxo.modelengine.api.error;

import com.ticxo.modelengine.api.utils.logger.LogColor;

public class ErrorUnknownBoneBehavior extends IError.Error {
   private final String boneName;
   private final String id;

   public String getErrorMessage() {
      return String.format("Error: Unknown bone behavior %s on bone %s.", LogColor.BLUE + this.id + LogColor.RED, LogColor.BLUE + this.boneName + LogColor.RED);
   }

   public ErrorUnknownBoneBehavior(String boneName, String id) {
      this.boneName = boneName;
      this.id = id;
   }
}
