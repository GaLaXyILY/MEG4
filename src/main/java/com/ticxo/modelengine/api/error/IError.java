package com.ticxo.modelengine.api.error;

import com.ticxo.modelengine.api.utils.config.ConfigProperty;
import com.ticxo.modelengine.api.utils.logger.TLogger;

public interface IError {
   ErrorUnknownFormat UNKNOWN_FORMAT = new ErrorUnknownFormat();
   WarnBoxUV BOX_UV = new WarnBoxUV();
   WarnBadEyeHeight BAD_EYE_HEIGHT = new WarnBadEyeHeight();
   WarnNoHitbox NO_HITBOX = new WarnNoHitbox();

   String getErrorMessage();

   IError.Severity getSeverity();

   default void log() {
      if (ConfigProperty.ERROR.getBoolean()) {
         switch(this.getSeverity()) {
         case WARN:
            TLogger.warn("--" + this.getErrorMessage());
            break;
         case ERROR:
            TLogger.error("--" + this.getErrorMessage());
         }

      }
   }

   public static enum Severity {
      WARN,
      ERROR;

      // $FF: synthetic method
      private static IError.Severity[] $values() {
         return new IError.Severity[]{WARN, ERROR};
      }
   }

   public abstract static class Error implements IError {
      public IError.Severity getSeverity() {
         return IError.Severity.ERROR;
      }
   }

   public abstract static class Warn implements IError {
      public IError.Severity getSeverity() {
         return IError.Severity.WARN;
      }
   }
}
