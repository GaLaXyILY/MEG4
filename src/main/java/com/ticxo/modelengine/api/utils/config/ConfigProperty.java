package com.ticxo.modelengine.api.utils.config;

import org.jetbrains.annotations.Nullable;

public enum ConfigProperty implements Property {
   ENGINE("Model-Engine"),
   GENERATOR("Model-Generator"),
   OPTIMIZATION("Network-Optimization"),
   DEFAULT_NAMES(ENGINE, "Default-Animations"),
   SCRIPT_WARNING(ENGINE, "Print-Script-Warnings", false),
   USE_STATE_MACHINE(ENGINE, "Use-State-Machine", false),
   ENGINE_THREADS(ENGINE, "Engine-Threads", 4),
   MAX_ENGINE_THREADS(ENGINE, "Max-Engine-Threads", 10),
   LATE_REGISTER(GENERATOR, "Register-Post-Server", true),
   LATE_ASSETS(GENERATOR, "Assets-Post-Server", true),
   LATE_ZIPPING(GENERATOR, "Compile-Post-Server", true),
   ERROR(GENERATOR, "Enable-Error", true),
   DEBUG_LEVEL(GENERATOR, "Debug-Level", 1),
   NAMESPACE(GENERATOR, "Namespace", "modelengine"),
   ZIP(GENERATOR, "Create-Zip", true),
   ATLAS(GENERATOR, "Create-Atlas", true),
   SHADER(GENERATOR, "Create-Shader", true),
   MCMETA(GENERATOR, "Create-MC-META", true),
   ITEM_MODEL(GENERATOR, "Item-Model", "LEATHER_HORSE_ARMOR"),
   BUNDLE_EVERYTHING(OPTIMIZATION, "Bundle-Everything", false),
   BUNDLE_SIZE(OPTIMIZATION, "Bundle-Size", 512),
   CULL_INTERVAL(OPTIMIZATION, "Cull-Interval", 4),
   CULLING_THREADS(OPTIMIZATION, "Culling-Threads", 4),
   MAX_CULLING_THREADS(OPTIMIZATION, "Max-Culling-Threads", 10),
   VERTICAL_CULL(OPTIMIZATION, "Vertical-Render-Distance"),
   VERTICAL_CULL_ENABLE(VERTICAL_CULL, "Enabled", true),
   VERTICAL_CULL_DISTANCE(VERTICAL_CULL, "Vertical-Render-Distance", 32),
   VERTICAL_CULL_TYPE(VERTICAL_CULL, "Cull-Type", "CULLED"),
   BACKWARDS_CULL(OPTIMIZATION, "Skip-Models-Behind-Viewer"),
   BACKWARDS_CULL_ENABLED(BACKWARDS_CULL, "Enabled", true),
   BACKWARDS_CULL_ANGLE(BACKWARDS_CULL, "View-Angle", 180),
   BACKWARDS_CULL_IGNORE_RADIUS(BACKWARDS_CULL, "Force-Render-Radius", 5),
   BACKWARDS_CULL_TYPE(BACKWARDS_CULL, "Cull-Type", "MOVEMENT_ONLY"),
   BLOCK_CULL(OPTIMIZATION, "Skip-Blocked-Models"),
   BLOCK_CULL_ENABLE(BLOCK_CULL, "Enabled", true),
   BLOCK_CULL_IGNORE_RADIUS(BLOCK_CULL, "Force-Render-Radius", 5),
   BLOCK_CULL_TYPE(BLOCK_CULL, "Cull-Type", "CULLED"),
   BLOCK_CULL_IGNORE_SIZE(BLOCK_CULL, "Force-Render-Size"),
   BLOCK_CULL_USE_PAPER_CLIP(BLOCK_CULL, "Use-Paper-Clip-Method", false),
   BLOCK_CULL_IGNORE_SIZE_WIDTH(BLOCK_CULL_IGNORE_SIZE, "Width", 32),
   BLOCK_CULL_IGNORE_SIZE_HEIGHT(BLOCK_CULL_IGNORE_SIZE, "Height", 32);

   private final String path;
   private final Object def;

   private ConfigProperty(String path) {
      this((String)path, (Object)null);
   }

   private ConfigProperty(ConfigProperty scope, String path) {
      this((String)(scope.getPath() + "." + path), (Object)null);
   }

   private ConfigProperty(String path, @Nullable Object def) {
      this.path = path;
      this.def = def;
   }

   private ConfigProperty(ConfigProperty scope, String path, @Nullable Object def) {
      this(scope.getPath() + "." + path, def);
   }

   public String getPath() {
      return this.path;
   }

   public Object getDef() {
      return this.def;
   }

   // $FF: synthetic method
   private static ConfigProperty[] $values() {
      return new ConfigProperty[]{ENGINE, GENERATOR, OPTIMIZATION, DEFAULT_NAMES, SCRIPT_WARNING, USE_STATE_MACHINE, ENGINE_THREADS, MAX_ENGINE_THREADS, LATE_REGISTER, LATE_ASSETS, LATE_ZIPPING, ERROR, DEBUG_LEVEL, NAMESPACE, ZIP, ATLAS, SHADER, MCMETA, ITEM_MODEL, BUNDLE_EVERYTHING, BUNDLE_SIZE, CULL_INTERVAL, CULLING_THREADS, MAX_CULLING_THREADS, VERTICAL_CULL, VERTICAL_CULL_ENABLE, VERTICAL_CULL_DISTANCE, VERTICAL_CULL_TYPE, BACKWARDS_CULL, BACKWARDS_CULL_ENABLED, BACKWARDS_CULL_ANGLE, BACKWARDS_CULL_IGNORE_RADIUS, BACKWARDS_CULL_TYPE, BLOCK_CULL, BLOCK_CULL_ENABLE, BLOCK_CULL_IGNORE_RADIUS, BLOCK_CULL_TYPE, BLOCK_CULL_IGNORE_SIZE, BLOCK_CULL_USE_PAPER_CLIP, BLOCK_CULL_IGNORE_SIZE_WIDTH, BLOCK_CULL_IGNORE_SIZE_HEIGHT};
   }
}
