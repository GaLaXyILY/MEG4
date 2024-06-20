package com.ticxo.modelengine.api.error;

public class WarnBadEyeHeight extends IError.Warn {
   public String getErrorMessage() {
      return "Warning: Eye height is below 0. Entity might suffocate.";
   }
}
