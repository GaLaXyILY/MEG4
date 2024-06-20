package com.ticxo.modelengine.api.utils;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.ticxo.modelengine.api.utils.logger.TLogger;
import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class CompatibilityManager implements Listener {
   private final PluginManager pluginManager = Bukkit.getPluginManager();
   private final Set<String> allPlugins = Sets.newConcurrentHashSet();
   private final Map<String, CompatibilityManager.CompatibilityConfiguration> compatibilities = Maps.newConcurrentMap();

   public CompatibilityManager(JavaPlugin plugin) {
      Plugin[] var2 = this.pluginManager.getPlugins();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Plugin pluginManagerPlugin = var2[var4];
         this.allPlugins.add(pluginManagerPlugin.getName());
      }

      this.pluginManager.registerEvents(this, plugin);
   }

   public void registerSupport(String pluginName, CompatibilityManager.CompatibilityConfiguration configuration) {
      if (this.allPlugins.contains(pluginName)) {
         Plugin plugin = this.pluginManager.getPlugin(pluginName);
         if (plugin != null && plugin.isEnabled() && configuration.tryApply(plugin)) {
            TLogger.log("Compatibility applied: " + plugin.getName());
         } else {
            this.compatibilities.put(pluginName, configuration);
         }

      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPluginLoad(PluginEnableEvent event) {
      Plugin plugin = event.getPlugin();
      if (plugin.isEnabled()) {
         CompatibilityManager.CompatibilityConfiguration config = (CompatibilityManager.CompatibilityConfiguration)this.compatibilities.get(plugin.getName());
         if (config != null) {
            if (!config.tryApply(plugin)) {
               TLogger.error("Failed to apply compatibility support for " + plugin.getName() + ".");
            } else {
               TLogger.log("Compatibility applied: " + plugin.getName());
            }
         }

      }
   }

   @FunctionalInterface
   public interface CompatibilityConfiguration {
      boolean tryApply(Plugin var1);
   }
}
