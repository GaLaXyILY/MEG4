package com.ticxo.modelengine.api.utils.logger;

import com.ticxo.modelengine.api.utils.config.ConfigProperty;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;

public class TLogger {
   public static Logger logger;

   public static void log() {
      log("");
   }

   public static void log(String string) {
      log(1, string);
   }

   public static void log(int level, String string) {
      if (ConfigProperty.DEBUG_LEVEL.getInt() >= level) {
         Logger var10000 = logger;
         Level var10001 = Level.INFO;
         String var10002 = getThread();
         var10000.log(var10001, var10002 + string + LogColor.RESET);
      }

   }

   public static void log(Object object) {
      log(object == null ? "null" : object.toString());
   }

   public static <T> void log(Iterable<T> iterable) {
      log(iterable, Object::toString);
   }

   public static <T> void log(Iterable<T> iterable, Function<T, String> toString) {
      String className = iterable.getClass().getSimpleName();
      StringBuilder builder = new StringBuilder();
      builder.append(className).append(":[");
      boolean isFirst = true;

      Object value;
      for(Iterator var5 = iterable.iterator(); var5.hasNext(); builder.append((String)toString.apply(value))) {
         value = var5.next();
         if (!isFirst) {
            builder.append(", ");
         } else {
            isFirst = false;
         }
      }

      builder.append("]");
      log(builder.toString());
   }

   public static <T> void log(T[] array) {
      log(array, Objects::toString);
   }

   public static <T> void log(T[] array, Function<T, String> toString) {
      String className = array.getClass().getSimpleName();
      StringBuilder builder = new StringBuilder();
      builder.append(className).append(":[");
      boolean isFirst = true;
      Object[] var5 = array;
      int var6 = array.length;

      for(int var7 = 0; var7 < var6; ++var7) {
         T value = var5[var7];
         if (!isFirst) {
            builder.append(", ");
         } else {
            isFirst = false;
         }

         builder.append((String)toString.apply(value));
      }

      builder.append("]");
      log(builder.toString());
   }

   public static void stacktrace() {
      if (isDebugEnabled()) {
         log((Object[])Thread.currentThread().getStackTrace(), (stackTraceElement) -> {
            return "\n" + stackTraceElement.toString();
         });
      }
   }

   public static void debug() {
      if (isDebugEnabled()) {
         log();
      }
   }

   public static void debug(String string) {
      if (isDebugEnabled()) {
         log(string);
      }
   }

   public static void debug(int level, String string) {
      if (isDebugEnabled()) {
         log(level, string);
      }
   }

   public static void debug(Object object) {
      if (isDebugEnabled()) {
         log(object);
      }
   }

   public static <T> void debug(Iterable<T> iterable) {
      debug(iterable, Objects::toString);
   }

   public static <T> void debug(Iterable<T> iterable, Function<T, String> toString) {
      if (isDebugEnabled()) {
         log(iterable, toString);
      }
   }

   public static <T> void debug(T[] array) {
      debug(array, Objects::toString);
   }

   public static <T> void debug(T[] array, Function<T, String> toString) {
      if (isDebugEnabled()) {
         log(array, toString);
      }
   }

   private static boolean isDebugEnabled() {
      return ConfigProperty.DEBUG_LEVEL.getInt() == 157;
   }

   public static void warn(String string) {
      warn(1, string);
   }

   public static void warn(int level, String string) {
      if (ConfigProperty.DEBUG_LEVEL.getInt() >= level) {
         Logger var10000 = logger;
         Level var10001 = Level.WARNING;
         String var10002 = getThread();
         var10000.log(var10001, var10002 + LogColor.YELLOW + string + LogColor.RESET);
      }

   }

   public static void error(String string) {
      error(1, string);
   }

   public static void error(int level, String string) {
      if (ConfigProperty.DEBUG_LEVEL.getInt() >= level) {
         Logger var10000 = logger;
         Level var10001 = Level.WARNING;
         String var10002 = getThread();
         var10000.log(var10001, var10002 + LogColor.RED + string + LogColor.RESET);
      }

   }

   private static String getThread() {
      return Bukkit.getServer().isPrimaryThread() ? "[S] " : "[A] ";
   }
}
