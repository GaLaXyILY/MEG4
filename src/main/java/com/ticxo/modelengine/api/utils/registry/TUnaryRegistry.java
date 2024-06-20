package com.ticxo.modelengine.api.utils.registry;

public abstract class TUnaryRegistry<T> extends TRegistry<T, T> {
   public T get(String id) {
      return id != null && this.registry.containsKey(id) ? this.registry.get(id) : this.getDefault();
   }

   public T getDefault() {
      return this.defaultItem;
   }

   protected T convert(T item) {
      throw new UnsupportedOperationException("The convert method should not be called in a singleton registry.");
   }
}
