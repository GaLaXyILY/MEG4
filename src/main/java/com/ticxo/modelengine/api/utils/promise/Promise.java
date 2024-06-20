package com.ticxo.modelengine.api.utils.promise;

import com.ticxo.modelengine.api.ServerInfo;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

public interface Promise<T> extends Future<T> {
   static <U> Promise<U> empty() {
      return (Promise)(ServerInfo.IS_FOLIA ? GlobalPromise.empty() : LegacyPromise.empty());
   }

   static <U> Promise<U> empty(Entity entity) {
      return (Promise)(ServerInfo.IS_FOLIA ? EntityPromise.empty(entity) : empty());
   }

   static <U> Promise<U> empty(Location location) {
      return (Promise)(ServerInfo.IS_FOLIA ? RegionPromise.empty(location) : empty());
   }

   static Promise<Void> start() {
      return (Promise)(ServerInfo.IS_FOLIA ? GlobalPromise.completed((Object)null) : LegacyPromise.completed((Object)null));
   }

   static Promise<Void> start(Entity entity) {
      return (Promise)(ServerInfo.IS_FOLIA ? EntityPromise.completed(entity, (Object)null) : start());
   }

   static Promise<Void> start(Location location) {
      return (Promise)(ServerInfo.IS_FOLIA ? RegionPromise.completed(location, (Object)null) : start());
   }

   static <U> Promise<U> completed(@Nullable U value) {
      return (Promise)(ServerInfo.IS_FOLIA ? GlobalPromise.completed(value) : LegacyPromise.completed(value));
   }

   static <U> Promise<U> completed(Entity entity, @Nullable U value) {
      return (Promise)(ServerInfo.IS_FOLIA ? EntityPromise.completed(entity, value) : completed(value));
   }

   static <U> Promise<U> completed(Location location, @Nullable U value) {
      return (Promise)(ServerInfo.IS_FOLIA ? RegionPromise.completed(location, value) : completed(value));
   }

   static <U> Promise<U> supplyingSync(Supplier<U> supplier) {
      Promise<U> promise = empty();
      return promise.supplySync(supplier);
   }

   static <U> Promise<U> supplyingSync(Entity entity, Supplier<U> supplier) {
      Promise<U> promise = empty(entity);
      return promise.supplySync(supplier);
   }

   static <U> Promise<U> supplyingSync(Location location, Supplier<U> supplier) {
      Promise<U> promise = empty(location);
      return promise.supplySync(supplier);
   }

   static <U> Promise<U> supplyingSyncDelay(Supplier<U> supplier, int delay) {
      Promise<U> promise = empty();
      return promise.supplySyncDelay(supplier, delay);
   }

   static <U> Promise<U> supplyingSyncDelay(Entity entity, Supplier<U> supplier, int delay) {
      Promise<U> promise = empty(entity);
      return promise.supplySyncDelay(supplier, delay);
   }

   static <U> Promise<U> supplyingSyncDelay(Location location, Supplier<U> supplier, int delay) {
      Promise<U> promise = empty(location);
      return promise.supplySyncDelay(supplier, delay);
   }

   static <U> Promise<U> supplyingAsync(Supplier<U> supplier) {
      Promise<U> promise = empty();
      return promise.supplyAsync(supplier);
   }

   static <U> Promise<U> supplyingAsyncDelay(Supplier<U> supplier, int delay) {
      Promise<U> promise = empty();
      return promise.supplyAsyncDelay(supplier, delay);
   }

   Promise<T> runSync(Runnable var1);

   Promise<T> runSyncDelay(Runnable var1, int var2);

   Promise<T> runAsync(Runnable var1);

   Promise<T> runAsyncDelay(Runnable var1, int var2);

   Promise<T> supplySync(Supplier<T> var1);

   Promise<T> supplySyncDelay(Supplier<T> var1, int var2);

   Promise<T> supplyAsync(Supplier<T> var1);

   Promise<T> supplyAsyncDelay(Supplier<T> var1, int var2);

   default Promise<Void> thenRunSync(Runnable runnable) {
      return this.thenApplySync((t) -> {
         runnable.run();
         return null;
      });
   }

   default Promise<Void> thenRunSyncDelay(Runnable runnable, int delay) {
      return this.thenApplySyncDelay((t) -> {
         runnable.run();
         return null;
      }, delay);
   }

   default Promise<Void> thenRunAsync(Runnable runnable) {
      return this.thenApplyAsync((t) -> {
         runnable.run();
         return null;
      });
   }

   default Promise<Void> thenRunAsyncDelay(Runnable runnable, int delay) {
      return this.thenApplyAsyncDelay((t) -> {
         runnable.run();
         return null;
      }, delay);
   }

   <U> Promise<U> thenApplySync(Function<? super T, ? extends U> var1);

   <U> Promise<U> thenApplySyncDelay(Function<? super T, ? extends U> var1, int var2);

   <U> Promise<U> thenApplyAsync(Function<? super T, ? extends U> var1);

   <U> Promise<U> thenApplyAsyncDelay(Function<? super T, ? extends U> var1, int var2);

   default Promise<Void> thenAcceptSync(Consumer<? super T> consumer) {
      return this.thenApplySync((t) -> {
         consumer.accept(t);
         return null;
      });
   }

   default Promise<Void> thenAcceptSyncDelay(Consumer<? super T> consumer, int delay) {
      return this.thenApplySyncDelay((t) -> {
         consumer.accept(t);
         return null;
      }, delay);
   }

   default Promise<Void> thenAcceptAsync(Consumer<? super T> consumer) {
      return this.thenApplyAsync((t) -> {
         consumer.accept(t);
         return null;
      });
   }

   default Promise<Void> thenAcceptAsyncDelay(Consumer<? super T> consumer, int delay) {
      return this.thenApplyAsyncDelay((t) -> {
         consumer.accept(t);
         return null;
      }, delay);
   }
}
