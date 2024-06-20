package com.ticxo.modelengine.api.error;

import com.ticxo.modelengine.api.model.bone.behavior.BoneBehaviorType;
import com.ticxo.modelengine.api.utils.logger.LogColor;

public class ErrorMissingBoneBehaviorData extends IError.Error {
   private final String bone;
   private final BoneBehaviorType<?> boneBehaviorType;
   private final String key;

   public String getErrorMessage() {
      Object[] var10001 = new Object[3];
      LogColor var10004 = LogColor.BLUE;
      var10001[0] = var10004 + this.boneBehaviorType.getId() + LogColor.RED;
      var10001[1] = LogColor.BLUE + this.bone + LogColor.RED;
      var10001[2] = LogColor.BLUE + this.key + LogColor.RED;
      return String.format("Error: The bone behavior %s of %s is missing required data %s. Removing behavior.", var10001);
   }

   public ErrorMissingBoneBehaviorData(String bone, BoneBehaviorType<?> boneBehaviorType, String key) {
      this.bone = bone;
      this.boneBehaviorType = boneBehaviorType;
      this.key = key;
   }
}
