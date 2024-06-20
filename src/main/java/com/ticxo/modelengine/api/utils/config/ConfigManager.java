package com.ticxo.modelengine.api.utils.config;

import com.google.common.collect.Sets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class ConfigManager {
   private final JavaPlugin plugin;
   private final Map<String, Object> config = new LinkedHashMap();
   private final Set<Runnable> updater = Sets.newConcurrentHashSet();
   private FileConfiguration file;

   public ConfigManager(JavaPlugin plugin) {
      this.plugin = plugin;
      this.file = plugin.getConfig();
      plugin.saveDefaultConfig();
   }

   public void register(Property property) {
      if (property.getDef() != null) {
         this.register(property.getPath(), property.getDef());
      }
   }

   public void register(String path, @NotNull Object def) {
      this.config.put(path, this.file.get(path, def));
   }

   public void registerReferenceUpdate(Runnable runnable) {
      this.updater.add(runnable);
      runnable.run();
   }

   public void reload() {
      this.plugin.reloadConfig();
      this.file = this.plugin.getConfig();
      this.config.replaceAll((p, v) -> {
         return this.file.get(p, this.config.get(p));
      });
   }

   public void updateReferences() {
      this.updater.forEach(Runnable::run);
   }

   public <T> T get(String path) {
      try {
         return this.config.get(path);
      } catch (ClassCastException var3) {
         return null;
      }
   }

   public <T> T get(Property property) {
      return this.get(property.getPath());
   }

   public int getInt(Property property) {
      return (Integer)this.get(property);
   }

   public double getDouble(Property property) {
      Object o = this.get(property);
      if (o instanceof Number) {
         Number number = (Number)o;
         return number.doubleValue();
      } else {
         return 0.0D;
      }
   }

   public String getString(Property property) {
      return (String)this.get(property);
   }

   public boolean getBoolean(Property property) {
      return (Boolean)this.get(property);
   }

   public void save() {
      Map var10000 = this.config;
      FileConfiguration var10001 = this.file;
      Objects.requireNonNull(var10001);
      var10000.forEach(var10001::set);
      this.plugin.saveConfig();
   }
}
