package com.ticxo.modelengine.api.utils.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GSONUtils {
   public static void ifPresent(JsonElement element, String key, Consumer<JsonElement> consumer) {
      if (element instanceof JsonObject) {
         JsonObject object = (JsonObject)element;
         if (object.has(key)) {
            consumer.accept(object.get(key));
         }
      }
   }

   public static void ifArray(JsonElement element, String key, Consumer<JsonElement> forEach) {
      if (element instanceof JsonObject) {
         JsonObject object = (JsonObject)element;
         if (object.has(key)) {
            JsonElement value = object.get(key);
            if (value instanceof JsonArray) {
               JsonArray array = (JsonArray)value;
               array.forEach(forEach);
            }

         }
      }
   }

   public static void ifArray(JsonElement element, String key, BiConsumer<Integer, JsonElement> forEach) {
      if (element instanceof JsonObject) {
         JsonObject object = (JsonObject)element;
         if (object.has(key)) {
            JsonElement value = object.get(key);
            if (value instanceof JsonArray) {
               JsonArray array = (JsonArray)value;

               for(int i = 0; i < array.size(); ++i) {
                  forEach.accept(i, array.get(i));
               }
            }

         }
      }
   }

   @Nullable
   public static <T> T get(JsonElement element, String key, Function<JsonElement, T> func) {
      if (element instanceof JsonObject) {
         JsonObject object = (JsonObject)element;
         if (!object.has(key)) {
            return null;
         } else {
            JsonElement value = object.get(key);
            return value.isJsonNull() ? null : func.apply(value);
         }
      } else {
         return null;
      }
   }

   @NotNull
   public static <T> T get(JsonElement element, String key, Function<JsonElement, T> func, @NotNull T def) {
      T val = get(element, key, func);
      return val == null ? def : val;
   }
}
