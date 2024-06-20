package com.ticxo.modelengine.api.error;

import com.ticxo.modelengine.api.utils.logger.LogColor;

public class ErrorNoFaceCube extends IError.Error {
   private final String bone;
   private final String cube;

   public String getErrorMessage() {
      return String.format("Error: The cube %s in bone %s has no faces. This might be caused by all faces having UV size of 0. Excluding cube from bone.", LogColor.BLUE + this.cube + LogColor.RED, LogColor.BLUE + this.bone + LogColor.RED);
   }

   public ErrorNoFaceCube(String bone, String cube) {
      this.bone = bone;
      this.cube = cube;
   }
}
