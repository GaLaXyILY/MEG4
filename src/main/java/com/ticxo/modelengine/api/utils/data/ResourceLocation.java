package com.ticxo.modelengine.api.utils.data;

import java.util.Locale;

public class ResourceLocation {
   private final String namespace;
   private final String path;

   public ResourceLocation(String full) {
      full = full.toLowerCase(Locale.ENGLISH);
      String[] split = full.split(":", 2);
      if (split.length <= 1) {
         this.namespace = "minecraft";
         this.path = full;
      } else {
         this.namespace = split[0];
         this.path = split[1];
      }

   }

   public String toString() {
      return "minecraft".equals(this.namespace) ? this.path : this.namespace + ":" + this.path;
   }

   public String getNamespace() {
      return this.namespace;
   }

   public String getPath() {
      return this.path;
   }

   public ResourceLocation(String namespace, String path) {
      this.namespace = namespace;
      this.path = path;
   }
}
