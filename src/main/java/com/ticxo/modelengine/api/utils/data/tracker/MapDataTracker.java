package com.ticxo.modelengine.api.utils.data.tracker;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MapDataTracker<T, U> extends DataTracker<Map<T, U>> implements Map<T, U> {
   public MapDataTracker(Map<T, U> value) {
      super((Object)value);
   }

   public int size() {
      return ((Map)this.value).size();
   }

   public boolean isEmpty() {
      return ((Map)this.value).isEmpty();
   }

   public boolean containsKey(Object key) {
      return ((Map)this.value).containsKey(key);
   }

   public boolean containsValue(Object value) {
      return ((Map)this.value).containsValue(value);
   }

   public U get(Object key) {
      return ((Map)this.value).get(key);
   }

   @Nullable
   public U put(T key, U value) {
      U prev = ((Map)this.value).put(key, value);
      this.isDirty |= prev != value;
      return prev;
   }

   public U remove(Object key) {
      if (!this.isDirty) {
         this.isDirty = ((Map)this.value).containsKey(key);
      }

      return ((Map)this.value).remove(key);
   }

   public void putAll(@NotNull Map<? extends T, ? extends U> m) {
      if (!this.isDirty) {
         label24: {
            Iterator var2 = m.entrySet().iterator();

            Entry entry;
            Object t;
            do {
               if (!var2.hasNext()) {
                  break label24;
               }

               entry = (Entry)var2.next();
               t = ((Map)this.value).get(entry.getKey());
            } while(t != null && t == entry.getValue());

            this.isDirty = true;
         }
      }

      ((Map)this.value).putAll(m);
   }

   public void clear() {
      if (!((Map)this.value).isEmpty()) {
         ((Map)this.value).clear();
         this.isDirty = true;
      }
   }

   @NotNull
   public Set<T> keySet() {
      return ((Map)this.value).keySet();
   }

   @NotNull
   public Collection<U> values() {
      return ((Map)this.value).values();
   }

   @NotNull
   public Set<Entry<T, U>> entrySet() {
      return ((Map)this.value).entrySet();
   }
}
