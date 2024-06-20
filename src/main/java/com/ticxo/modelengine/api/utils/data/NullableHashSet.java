package com.ticxo.modelengine.api.utils.data;

import java.util.LinkedHashSet;

public class NullableHashSet<T> extends LinkedHashSet<T> {
   public boolean add(T t) {
      return t == null ? false : super.add(t);
   }
}
