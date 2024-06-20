package com.ticxo.modelengine.api.utils.data.io;

import java.util.Optional;

public interface DataIO {
   default Optional<SavedData> save() {
      SavedData data = new SavedData();
      this.save(data);
      return data.keySet().isEmpty() ? Optional.empty() : Optional.of(data);
   }

   void save(SavedData var1);

   void load(SavedData var1);
}
