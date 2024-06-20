package com.ticxo.modelengine.api.utils.data.tracker;

import java.util.function.BiConsumer;

public class UpdateDataTracker<T> extends DataTracker<T> {
   private final BiConsumer<T, T> setter;

   public UpdateDataTracker(BiConsumer<T, T> setter) {
      this.setter = setter;
   }

   public UpdateDataTracker(T value, BiConsumer<T, T> setter) {
      super(value);
      this.setter = setter;
   }

   public void set(T value) {
      this.set(value, (Runnable)null);
   }

   public void set(T value, Runnable ifDirty) {
      if (this.value == null || !this.value.equals(value)) {
         this.setter.accept(this.value, value);
         this.isDirty = true;
         if (ifDirty != null) {
            ifDirty.run();
         }

      }
   }
}
