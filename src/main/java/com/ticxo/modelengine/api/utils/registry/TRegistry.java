package com.ticxo.modelengine.api.utils.registry;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Set;

public abstract class TRegistry<T, R> {
   protected final Map<String, T> registry = this.mapSupplier();
   protected T defaultItem;

   public void register(String id, T item) {
      this.registry.put(id, item);
   }

   public void registerAndDefault(String id, T item) {
      this.registry.put(id, item);
      this.defaultItem = item;
   }

   public R get(String id) {
      return !this.registry.containsKey(id) ? this.getDefault() : this.convert(this.registry.get(id));
   }

   public R getDefault() {
      return this.convert(this.defaultItem);
   }

   public Set<String> getKeys() {
      return this.registry.keySet();
   }

   protected abstract R convert(T var1);

   protected Map<String, T> mapSupplier() {
      return Maps.newConcurrentMap();
   }

   public void setDefaultItem(T defaultItem) {
      this.defaultItem = defaultItem;
   }
}
