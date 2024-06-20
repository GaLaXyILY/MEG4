package com.ticxo.modelengine.api.error;

public class WarnBoxUV extends IError.Warn {
   public String getErrorMessage() {
      return "Warning: Box UV detected. Cube UVs might not generate correctly if Box UV is used.";
   }
}
