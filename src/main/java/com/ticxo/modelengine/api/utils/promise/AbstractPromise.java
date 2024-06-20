package com.ticxo.modelengine.api.utils.promise;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractPromise<T> implements Promise<T> {
   @NotNull
   private final CompletableFuture<T> future;

   protected AbstractPromise() {
      this.future = new CompletableFuture();
   }

   protected AbstractPromise(T val) {
      this.future = CompletableFuture.completedFuture(val);
   }

   public Promise<T> runSync(Runnable runnable) {
      this.executeSync(runnable);
      return this;
   }

   public Promise<T> runSyncDelay(Runnable runnable, int delay) {
      this.executeSync(runnable, delay);
      return this;
   }

   public Promise<T> runAsync(Runnable runnable) {
      this.executeAsync(runnable);
      return this;
   }

   public Promise<T> runAsyncDelay(Runnable runnable, int delay) {
      this.executeAsync(runnable, delay);
      return this;
   }

   public Promise<T> supplySync(Supplier<T> supplier) {
      this.executeSync(() -> {
         this.future.complete(supplier.get());
      });
      return this;
   }

   public Promise<T> supplySyncDelay(Supplier<T> supplier, int delay) {
      this.executeSync(() -> {
         this.future.complete(supplier.get());
      }, delay);
      return this;
   }

   public Promise<T> supplyAsync(Supplier<T> supplier) {
      this.executeAsync(() -> {
         this.future.complete(supplier.get());
      });
      return this;
   }

   public Promise<T> supplyAsyncDelay(Supplier<T> supplier, int delay) {
      this.executeAsync(() -> {
         this.future.complete(supplier.get());
      }, delay);
      return this;
   }

   public <U> Promise<U> thenApplySync(Function<? super T, ? extends U> function) {
      AbstractPromise<U> promise = this.createEmpty();
      this.future.whenComplete((t, throwable) -> {
         promise.executeSync(() -> {
            promise.future.complete(function.apply(t));
         });
      });
      return promise;
   }

   public <U> Promise<U> thenApplySyncDelay(Function<? super T, ? extends U> function, int delay) {
      AbstractPromise<U> promise = this.createEmpty();
      this.future.whenComplete((t, throwable) -> {
         promise.executeSync(() -> {
            promise.future.complete(function.apply(t));
         }, delay);
      });
      return promise;
   }

   public <U> Promise<U> thenApplyAsync(Function<? super T, ? extends U> function) {
      AbstractPromise<U> promise = this.createEmpty();
      this.future.whenComplete((t, throwable) -> {
         promise.executeAsync(() -> {
            promise.future.complete(function.apply(t));
         });
      });
      return promise;
   }

   public <U> Promise<U> thenApplyAsyncDelay(Function<? super T, ? extends U> function, int delay) {
      AbstractPromise<U> promise = this.createEmpty();
      this.future.whenComplete((t, throwable) -> {
         promise.executeAsync(() -> {
            promise.future.complete(function.apply(t));
         }, delay);
      });
      return promise;
   }

   public boolean cancel(boolean mayInterruptIfRunning) {
      return this.future.cancel(mayInterruptIfRunning);
   }

   public boolean isCancelled() {
      return this.future.isCancelled();
   }

   public boolean isDone() {
      return this.future.isDone();
   }

   public T get() throws InterruptedException, ExecutionException {
      return this.future.get();
   }

   public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
      return this.future.get(timeout, unit);
   }

   protected abstract <U> AbstractPromise<U> createEmpty();

   protected abstract void executeSync(Runnable var1);

   protected abstract void executeSync(Runnable var1, int var2);

   protected abstract void executeAsync(Runnable var1);

   protected abstract void executeAsync(Runnable var1, int var2);
}
