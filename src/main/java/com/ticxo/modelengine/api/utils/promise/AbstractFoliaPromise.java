package com.ticxo.modelengine.api.utils.promise;

import com.ticxo.modelengine.api.ModelEngineAPI;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;

public abstract class AbstractFoliaPromise<T> extends AbstractPromise<T> {
   protected AbstractFoliaPromise() {
   }

   protected AbstractFoliaPromise(T val) {
      super(val);
   }

   protected final void executeAsync(Runnable runnable) {
      Bukkit.getAsyncScheduler().runNow(ModelEngineAPI.getAPI(), (task) -> {
         runnable.run();
      });
   }

   protected final void executeAsync(Runnable runnable, int delay) {
      Bukkit.getAsyncScheduler().runDelayed(ModelEngineAPI.getAPI(), (task) -> {
         runnable.run();
      }, (long)(delay * 50), TimeUnit.MILLISECONDS);
   }
}
