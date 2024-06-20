package com.ticxo.modelengine.api.utils.data.io;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.utils.data.ItemUtils;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class SavedData extends HashMap<String, Object> {
   public static final NamespacedKey DATA_KEY = new NamespacedKey(ModelEngineAPI.getAPI(), "model_data");

   public static SavedData parse(String json) {
      return (SavedData)gson().fromJson(json, SavedData.class);
   }

   public void putBoolean(String key, Boolean value) {
      this.put(key, value);
   }

   public Boolean getBoolean(String key, Boolean def) {
      String val = this.getAsString(key);
      return val == null ? def : Boolean.parseBoolean(val);
   }

   public Boolean getBoolean(String key) {
      return this.getBoolean(key, (Boolean)null);
   }

   public void putByte(String key, Byte value) {
      this.put(key, value);
   }

   public Byte getByte(String key, Byte def) {
      try {
         String val = this.getAsString(key);
         return val == null ? def : Byte.parseByte(val);
      } catch (NumberFormatException var4) {
         this.wrapException(key, var4);
         return def;
      }
   }

   public Byte getByte(String key) {
      return this.getByte(key, (Byte)null);
   }

   public void putInt(String key, Integer value) {
      this.put(key, value);
   }

   public Integer getInt(String key, Integer def) {
      try {
         String val = this.getAsString(key);
         return val == null ? def : (int)Double.parseDouble(val);
      } catch (NumberFormatException var4) {
         this.wrapException(key, var4);
         return def;
      }
   }

   public Integer getInt(String key) {
      return this.getInt(key, (Integer)null);
   }

   public void putFloat(String key, Float value) {
      this.put(key, value);
   }

   public Float getFloat(String key, Float def) {
      try {
         String val = this.getAsString(key);
         return val == null ? def : Float.parseFloat(val);
      } catch (NumberFormatException var4) {
         this.wrapException(key, var4);
         return def;
      }
   }

   public Float getFloat(String key) {
      return this.getFloat(key, (Float)null);
   }

   public void putDouble(String key, Double value) {
      this.put(key, value);
   }

   public Double getDouble(String key, Double def) {
      try {
         String val = this.getAsString(key);
         return val == null ? def : Double.parseDouble(val);
      } catch (NumberFormatException var4) {
         this.wrapException(key, var4);
         return def;
      }
   }

   public Double getDouble(String key) {
      return this.getDouble(key, (Double)null);
   }

   public void putString(String key, String value) {
      this.put(key, value);
   }

   public String getString(String key, String def) {
      return this.getOrDefaultAsString(key, def);
   }

   public String getString(String key) {
      return this.getString(key, (String)null);
   }

   public void putUUID(String key, UUID value) {
      this.put(key, value);
   }

   public UUID getUUID(String key, UUID def) {
      String value = this.getAsString(key);
      return value == null ? def : UUID.fromString(value);
   }

   public UUID getUUID(String key) {
      return this.getUUID(key, (UUID)null);
   }

   public void putList(String key, Collection<?> collection) {
      this.put(key, collection);
   }

   public <T> List<T> getList(String key) {
      String value = this.getAsString(key);
      return value == null ? List.of() : (List)gson().fromJson(value, (new TypeToken<Object>() {
      }).getType());
   }

   public <T> List<T> getList(String key, Class<T> clazz) {
      String value = gson().toJson(this.get(key));
      return value == null ? List.of() : (List)gson().fromJson(value, TypeToken.getParameterized(List.class, new Type[]{clazz}));
   }

   public void putData(String key, SavedData value) {
      if (this == value) {
         throw new RuntimeException("Cannot add data: Attempting to add self to self.");
      } else {
         this.put(key, value);
      }
   }

   public Optional<SavedData> getData(String key) {
      Object value = this.get(key);
      if (value instanceof Map) {
         SavedData data = new SavedData();
         data.putAll((Map)value);
         return Optional.of(data);
      } else {
         return Optional.empty();
      }
   }

   public void putItemStack(String key, @Nullable ItemStack stack) {
      if (stack != null) {
         this.putString(key, ItemUtils.encodeItemStackToString(stack));
      }
   }

   public ItemStack getItemStack(String key) {
      return this.getItemStack(key, (ItemStack)null);
   }

   public ItemStack getItemStack(String key, ItemStack def) {
      String value = this.getAsString(key);
      if (value == null) {
         return def;
      } else {
         try {
            return ItemUtils.decodeItemStack(value);
         } catch (Throwable var5) {
            var5.printStackTrace();
            return def;
         }
      }
   }

   public <T> void saveIfExist(String key, Supplier<T> supplier, SavedData.DataSaver<T> saveConsumer) {
      T val = supplier.get();
      if (val != null) {
         saveConsumer.save(this, key, val);
      }

   }

   public <T> void loadIfExist(String key, SavedData.DataLoader<T> getter, Consumer<T> consumer) {
      T val = getter.load(this, key);
      if (val != null) {
         consumer.accept(val);
      }

   }

   public String toString() {
      return gson().toJson(this);
   }

   private void wrapException(String key, Exception e) {
      throw new RuntimeException("An error occurred while reading the value of " + key, e);
   }

   private String getAsString(String key) {
      return this.getOrDefaultAsString(key, (String)null);
   }

   private String getOrDefaultAsString(String key, String def) {
      Object obj = this.get(key);
      return obj == null ? def : obj.toString();
   }

   private static Gson gson() {
      return ModelEngineAPI.getAPI().getGson();
   }

   @FunctionalInterface
   public interface DataSaver<S> {
      void save(SavedData var1, String var2, S var3);
   }

   @FunctionalInterface
   public interface DataLoader<S> {
      S load(SavedData var1, String var2);
   }
}
