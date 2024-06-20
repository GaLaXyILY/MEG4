package com.ticxo.modelengine.api.utils;

import com.ticxo.modelengine.api.utils.ticker.DualTicker;
import java.util.ConcurrentModificationException;

public class RaceConditionUtil {
   public static void wrapConmod(Runnable runnable) {
      wrapConmod(runnable, 1);
   }

   public static void wrapConmod(Runnable runnable, int delay) {
      wrapConmod(runnable, delay, 1);
   }

   public static void wrapConmod(Runnable runnable, int delay, int attempts) {
      try {
         runnable.run();
      } catch (ConcurrentModificationException var4) {
         if (attempts > 0) {
            DualTicker.queueDelayedSyncTask(() -> {
               wrapConmod(runnable, delay, attempts - 1);
            }, delay);
         }
      } catch (Throwable var5) {
         var5.printStackTrace();
      }

   }

   public static void wrapAll(Runnable runnable) {
      wrapConmod(runnable, 1);
   }

   public static void wrapAll(Runnable runnable, int delay) {
      wrapConmod(runnable, delay, 1);
   }

   public static void wrapAll(Runnable runnable, int delay, int attempts) {
      try {
         runnable.run();
      } catch (Throwable var4) {
         if (attempts > 0) {
            DualTicker.queueDelayedSyncTask(() -> {
               wrapConmod(runnable, delay, attempts - 1);
            }, delay);
         } else {
            var4.printStackTrace();
         }
      }

   }
}
