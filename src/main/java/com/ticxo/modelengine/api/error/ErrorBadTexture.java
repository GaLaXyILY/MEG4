package com.ticxo.modelengine.api.error;

public class ErrorBadTexture extends IError.Error {
   private final String name;

   public String getErrorMessage() {
      return "Error: Texture name contains non [a-z0-9/._-] characters. [" + this.name + "]";
   }

   public ErrorBadTexture(String name) {
      this.name = name;
   }
}
