package com.ticxo.modelengine.api.error;

import com.ticxo.modelengine.api.model.bone.behavior.BoneBehaviorType;
import com.ticxo.modelengine.api.utils.logger.LogColor;

public class ErrorWrongBoneBehaviorDataType extends IError.Error {
   private final String bone;
   private final BoneBehaviorType<?> boneBehaviorType;
   private final String key;
   private final Class<?> expects;
   private final Class<?> provided;

   public String getErrorMessage() {
      Object[] var10001 = new Object[5];
      LogColor var10004 = LogColor.BLUE;
      var10001[0] = var10004 + this.boneBehaviorType.getId() + LogColor.RED;
      var10001[1] = LogColor.BLUE + this.bone + LogColor.RED;
      var10001[2] = LogColor.BLUE + this.key + LogColor.RED;
      var10004 = LogColor.BLUE;
      var10001[3] = var10004 + this.expects.getSimpleName() + LogColor.RED;
      var10004 = LogColor.BLUE;
      var10001[4] = var10004 + this.provided.getSimpleName() + LogColor.RED;
      return String.format("Error: The bone behavior %s of %s was given the wrong data type for %s. Expected %s, provided %s. Removing behavior.", var10001);
   }

   public ErrorWrongBoneBehaviorDataType(String bone, BoneBehaviorType<?> boneBehaviorType, String key, Class<?> expects, Class<?> provided) {
      this.bone = bone;
      this.boneBehaviorType = boneBehaviorType;
      this.key = key;
      this.expects = expects;
      this.provided = provided;
   }
}
