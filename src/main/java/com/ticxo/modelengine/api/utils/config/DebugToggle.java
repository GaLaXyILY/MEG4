package com.ticxo.modelengine.api.utils.config;

import java.util.HashSet;
import java.util.Set;
import org.jetbrains.annotations.Nullable;

public enum DebugToggle {
   SHOW_OBB,
   SHOW_CULL_POINTS;

   private static final Set<DebugToggle> toggles = new HashSet();

   public static void setDebug(DebugToggle debug, boolean flag) {
      if (flag) {
         toggles.add(debug);
      } else {
         toggles.remove(debug);
      }

   }

   public static boolean isDebugging(DebugToggle debug) {
      return toggles.contains(debug);
   }

   @Nullable
   public static DebugToggle get(String value) {
      try {
         return valueOf(value);
      } catch (Throwable var2) {
         return null;
      }
   }

   // $FF: synthetic method
   private static DebugToggle[] $values() {
      return new DebugToggle[]{SHOW_OBB, SHOW_CULL_POINTS};
   }
}
