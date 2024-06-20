package com.ticxo.modelengine.api.utils;

public enum OffsetMode {
   LOCAL,
   MODEL,
   GLOBAL;

   public static OffsetMode get(String name) {
      try {
         return valueOf(name);
      } catch (IllegalArgumentException var2) {
         return LOCAL;
      }
   }

   // $FF: synthetic method
   private static OffsetMode[] $values() {
      return new OffsetMode[]{LOCAL, MODEL, GLOBAL};
   }
}
