package com.ticxo.modelengine.api.error;

public class WarnBadNamespace extends IError.Warn {
   private final String namespace;

   public String getErrorMessage() {
      return "Warning: Namespace contains characters other than a-z_0-9. Using default namespace.[" + this.namespace + "]";
   }

   public WarnBadNamespace(String namespace) {
      this.namespace = namespace;
   }
}
