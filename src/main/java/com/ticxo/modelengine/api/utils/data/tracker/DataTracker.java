package com.ticxo.modelengine.api.utils.data.tracker;

import java.util.function.BiPredicate;
import java.util.function.Consumer;

public class DataTracker<T> {
   protected final BiPredicate<T, T> equal;
   protected boolean isDirty;
   protected T value;

   public DataTracker() {
      this.equal = Object::equals;
   }

   public DataTracker(T value) {
      this.value = value;
      this.equal = Object::equals;
   }

   public DataTracker(BiPredicate<T, T> equal) {
      this.equal = equal;
   }

   public void markDirty() {
      this.isDirty = true;
   }

   public void clearDirty() {
      this.isDirty = false;
   }

   public void ifDirty(Consumer<T> consumer) {
      if (this.isDirty) {
         consumer.accept(this.value);
      }

   }

   public void ifDirty(Consumer<T> consumer, boolean force) {
      if (this.isDirty || force) {
         consumer.accept(this.value);
      }

   }

   public void set(T value) {
      this.set(value, (Runnable)null);
   }

   public void set(T value, Runnable ifDirty) {
      if (this.value == null || !this.equal.test(this.value, value)) {
         this.value = value;
         this.isDirty = true;
         if (ifDirty != null) {
            ifDirty.run();
         }

      }
   }

   public T get() {
      return this.value;
   }

   public boolean isDirty() {
      return this.isDirty;
   }
}
