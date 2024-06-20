package com.ticxo.modelengine.api.error;

public class WarnNoHitbox extends IError.Warn {
   public String getErrorMessage() {
      return "Warning: Missing hitbox.";
   }
}
