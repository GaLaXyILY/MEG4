package com.ticxo.modelengine.core.mythic.compatibility;

import com.ticxo.modelengine.api.utils.CompatibilityManager;
import com.ticxo.modelengine.core.ModelEngine;
import com.ticxo.modelengine.core.mythic.animation.MythicScriptReader;
import com.ticxo.modelengine.core.mythic.utils.ModelEngineComponentRegistry;
import io.lumine.mythic.bukkit.MythicBukkit;
import java.util.ArrayList;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class MythicCompatibility implements CompatibilityManager.CompatibilityConfiguration {
   private final ModelEngine plugin;
   private ModelEngineSupportImpl mythicSupport;
   private ModelEngineComponentRegistry customComponentRegistry;

   public MythicCompatibility(ModelEngine plugin) {
      this.plugin = plugin;
   }

   public boolean tryApply(Plugin plugin) {
      this.customComponentRegistry = new ModelEngineComponentRegistry(this.plugin, new ArrayList<String>() {
         {
            this.add("com.ticxo.modelengine.core.mythic.conditions");
            this.add("com.ticxo.modelengine.core.mythic.mechanics");
            this.add("com.ticxo.modelengine.core.mythic.targeters");
         }
      });
      this.plugin.getScriptReaderRegistry().register("mm", new MythicScriptReader());
      this.mythicSupport = new ModelEngineSupportImpl();
      MythicBukkit.inst().getCompatibility().setModelEngine(Optional.of(this.mythicSupport));
      Bukkit.getPluginManager().registerEvents(this.customComponentRegistry, plugin);
      this.plugin.getModelGenerator().importModels(true);
      return true;
   }

   public ModelEngineSupportImpl getMythicSupport() {
      return this.mythicSupport;
   }

   public ModelEngineComponentRegistry getCustomComponentRegistry() {
      return this.customComponentRegistry;
   }
}
