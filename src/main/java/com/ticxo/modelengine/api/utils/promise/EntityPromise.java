package com.ticxo.modelengine.api.utils.promise;

import com.ticxo.modelengine.api.ModelEngineAPI;
import org.bukkit.entity.Entity;

public class EntityPromise<T> extends AbstractFoliaPromise<T> {
   private final Entity entity;

   private EntityPromise(Entity entity) {
      this.entity = entity;
   }

   private EntityPromise(Entity entity, T val) {
      super(val);
      this.entity = entity;
   }

   protected <U> AbstractPromise<U> createEmpty() {
      return empty(this.entity);
   }

   protected void executeSync(Runnable runnable) {
      this.entity.getScheduler().execute(ModelEngineAPI.getAPI(), runnable, (Runnable)null, 0L);
   }

   protected void executeSync(Runnable runnable, int delay) {
      this.entity.getScheduler().execute(ModelEngineAPI.getAPI(), runnable, (Runnable)null, (long)delay);
   }

   static <T> EntityPromise<T> empty(Entity entity) {
      return new EntityPromise(entity);
   }

   static <T> EntityPromise<T> completed(Entity entity, T val) {
      return new EntityPromise(entity, val);
   }
}
