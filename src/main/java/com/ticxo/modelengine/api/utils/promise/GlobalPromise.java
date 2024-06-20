package com.ticxo.modelengine.api.utils.promise;

import com.ticxo.modelengine.api.ModelEngineAPI;
import org.bukkit.Bukkit;

public class GlobalPromise<T> extends AbstractFoliaPromise<T> {
   private GlobalPromise() {
   }

   private GlobalPromise(T val) {
      super(val);
   }

   protected <U> AbstractPromise<U> createEmpty() {
      return empty();
   }

   protected void executeSync(Runnable runnable) {
      Bukkit.getGlobalRegionScheduler().execute(ModelEngineAPI.getAPI(), runnable);
   }

   protected void executeSync(Runnable runnable, int delay) {
      Bukkit.getGlobalRegionScheduler().runDelayed(ModelEngineAPI.getAPI(), (task) -> {
         runnable.run();
      }, (long)delay);
   }

   static <T> GlobalPromise<T> empty() {
      return new GlobalPromise();
   }

   static <T> GlobalPromise<T> completed(T val) {
      return new GlobalPromise(val);
   }
}
