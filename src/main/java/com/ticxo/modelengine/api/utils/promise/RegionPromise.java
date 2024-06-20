package com.ticxo.modelengine.api.utils.promise;

import com.ticxo.modelengine.api.ModelEngineAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class RegionPromise<T> extends AbstractFoliaPromise<T> {
   private final Location location;

   private RegionPromise(Location location) {
      this.location = location;
   }

   private RegionPromise(Location location, T val) {
      super(val);
      this.location = location;
   }

   protected <U> AbstractPromise<U> createEmpty() {
      return empty(this.location);
   }

   protected void executeSync(Runnable runnable) {
      Bukkit.getRegionScheduler().execute(ModelEngineAPI.getAPI(), this.location, runnable);
   }

   protected void executeSync(Runnable runnable, int delay) {
      Bukkit.getRegionScheduler().runDelayed(ModelEngineAPI.getAPI(), this.location, (task) -> {
         runnable.run();
      }, (long)delay);
   }

   static <T> RegionPromise<T> empty(Location location) {
      return new RegionPromise(location);
   }

   static <T> RegionPromise<T> completed(Location location, T val) {
      return new RegionPromise(location, val);
   }
}
