package com.ticxo.modelengine.api.error;

import com.ticxo.modelengine.api.utils.logger.LogColor;

public class ErrorIncompatibleBone extends IError.Error {
   private final boolean rendering;
   private final String boneName;
   private final String id;

   public String getErrorMessage() {
      Object[] var10001 = new Object[]{this.rendering ? "ghost" : "renderer", null, null, null};
      LogColor var10004 = LogColor.BLUE;
      var10001[1] = var10004 + this.id + LogColor.RED;
      var10001[2] = this.rendering ? "renderer bone" : "ghost bone";
      var10001[3] = LogColor.BLUE + this.boneName + LogColor.RED;
      return String.format("Error: The %s bone behavior type %s is not compatible with %s %s.", var10001);
   }

   public ErrorIncompatibleBone(boolean rendering, String boneName, String id) {
      this.rendering = rendering;
      this.boneName = boneName;
      this.id = id;
   }
}
