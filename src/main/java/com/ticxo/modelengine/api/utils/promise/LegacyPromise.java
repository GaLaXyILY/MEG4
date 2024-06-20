package com.ticxo.modelengine.api.utils.promise;

import com.ticxo.modelengine.api.ModelEngineAPI;
import org.bukkit.Bukkit;

public class LegacyPromise<T> extends AbstractPromise<T> {
   private LegacyPromise() {
   }

   private LegacyPromise(T val) {
      super(val);
   }

   protected <U> AbstractPromise<U> createEmpty() {
      return empty();
   }

   protected void executeSync(Runnable runnable) {
      if (Bukkit.isPrimaryThread()) {
         runnable.run();
      } else {
         Bukkit.getScheduler().runTask(ModelEngineAPI.getAPI(), runnable);
      }

   }

   protected void executeSync(Runnable runnable, int delay) {
      Bukkit.getScheduler().runTaskLater(ModelEngineAPI.getAPI(), runnable, (long)delay);
   }

   protected void executeAsync(Runnable runnable) {
      if (Bukkit.isPrimaryThread()) {
         Bukkit.getScheduler().runTaskAsynchronously(ModelEngineAPI.getAPI(), runnable);
      } else {
         runnable.run();
      }

   }

   protected void executeAsync(Runnable runnable, int delay) {
      Bukkit.getScheduler().runTaskLaterAsynchronously(ModelEngineAPI.getAPI(), runnable, (long)delay);
   }

   static <T> LegacyPromise<T> empty() {
      return new LegacyPromise();
   }

   static <T> LegacyPromise<T> completed(T val) {
      return new LegacyPromise(val);
   }
}
