package com.ticxo.modelengine.api.utils.data.tracker;

import java.util.Collection;
import java.util.Iterator;
import org.jetbrains.annotations.NotNull;

public class CollectionDataTracker<T> extends DataTracker<Collection<T>> implements Collection<T> {
   public CollectionDataTracker(Collection<T> value) {
      super((Object)value);
   }

   public int size() {
      return ((Collection)this.value).size();
   }

   public boolean isEmpty() {
      return ((Collection)this.value).isEmpty();
   }

   public boolean contains(Object o) {
      return ((Collection)this.value).contains(o);
   }

   @NotNull
   public Iterator<T> iterator() {
      return ((Collection)this.value).iterator();
   }

   @NotNull
   public Object[] toArray() {
      return ((Collection)this.value).toArray();
   }

   @NotNull
   public <T1> T1[] toArray(@NotNull T1[] a) {
      return ((Collection)this.value).toArray(a);
   }

   public boolean add(T t) {
      boolean flag = ((Collection)this.value).add(t);
      this.isDirty |= flag;
      return flag;
   }

   public boolean remove(Object o) {
      boolean flag = ((Collection)this.value).remove(o);
      this.isDirty |= flag;
      return flag;
   }

   public boolean containsAll(@NotNull Collection<?> c) {
      return ((Collection)this.value).containsAll(c);
   }

   public boolean addAll(@NotNull Collection<? extends T> c) {
      boolean flag = ((Collection)this.value).addAll(c);
      this.isDirty |= flag;
      return flag;
   }

   public boolean removeAll(@NotNull Collection<?> c) {
      boolean flag = ((Collection)this.value).removeAll(c);
      this.isDirty |= flag;
      return flag;
   }

   public boolean retainAll(@NotNull Collection<?> c) {
      boolean flag = ((Collection)this.value).retainAll(c);
      this.isDirty |= flag;
      return flag;
   }

   public void clear() {
      if (!((Collection)this.value).isEmpty()) {
         ((Collection)this.value).clear();
         this.isDirty = true;
      }
   }
}
