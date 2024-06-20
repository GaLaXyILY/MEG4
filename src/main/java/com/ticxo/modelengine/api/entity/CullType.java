package com.ticxo.modelengine.api.entity;

import java.util.Locale;

public enum CullType {
   NO_CULL,
   MOVEMENT_ONLY,
   CULLED;

   public static CullType get(String value) {
      try {
         return valueOf(value.toUpperCase(Locale.ENGLISH));
      } catch (IllegalArgumentException var2) {
         return NO_CULL;
      }
   }

   // $FF: synthetic method
   private static CullType[] $values() {
      return new CullType[]{NO_CULL, MOVEMENT_ONLY, CULLED};
   }
}
