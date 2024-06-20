package com.ticxo.modelengine.api.model.bone.behavior;

import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BoneBehaviorData {
   private final Map<String, Object> data;

   @Nullable
   public <T> T get(String key) {
      try {
         return this.data.get(key);
      } catch (ClassCastException var3) {
         var3.printStackTrace();
         return null;
      }
   }

   @NotNull
   public <T> T get(String key, @NotNull T def) {
      Object value = this.data.get(key);
      if (value == null) {
         return def;
      } else {
         Class<?> defClass = def.getClass();
         Class<?> valClass = value.getClass();
         if (defClass.isAssignableFrom(valClass)) {
            return value;
         } else {
            (new ClassCastException(String.format("Could not cast %s to %s. Returning default value.", valClass.getSimpleName(), defClass.getSimpleName()))).printStackTrace();
            return def;
         }
      }
   }

   public BoneBehaviorData(Map<String, Object> data) {
      this.data = data;
   }
}
