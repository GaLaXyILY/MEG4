package com.ticxo.modelengine.core.generator;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.gson.Gson;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.error.IError;
import com.ticxo.modelengine.api.events.ModelRegistrationEvent;
import com.ticxo.modelengine.api.generator.BaseItemEnum;
import com.ticxo.modelengine.api.generator.ModelGenerator;
import com.ticxo.modelengine.api.generator.assets.BlueprintTexture;
import com.ticxo.modelengine.api.generator.assets.JavaItemModel;
import com.ticxo.modelengine.api.generator.assets.ModelAssets;
import com.ticxo.modelengine.api.generator.blueprint.BlueprintBone;
import com.ticxo.modelengine.api.generator.blueprint.ModelBlueprint;
import com.ticxo.modelengine.api.generator.parser.ModelParser;
import com.ticxo.modelengine.api.model.ModelRegistry;
import com.ticxo.modelengine.api.utils.TFile;
import com.ticxo.modelengine.api.utils.config.ConfigProperty;
import com.ticxo.modelengine.api.utils.logger.LogColor;
import com.ticxo.modelengine.api.utils.logger.TLogger;
import com.ticxo.modelengine.api.utils.math.TMath;
import com.ticxo.modelengine.api.utils.ticker.DualTicker;
import com.ticxo.modelengine.core.generator.atlas.AtlasManager;
import com.ticxo.modelengine.core.generator.java.BaseItem;
import com.ticxo.modelengine.core.generator.parser.blockbench.BlockbenchParser;
import com.ticxo.modelengine.core.generator.parser.modelengine.ModelEngineParser;
import com.ticxo.modelengine.core.mythic.MythicUtils;
import it.unimi.dsi.fastutil.Pair;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;
import org.bukkit.Bukkit;

public class ModelGeneratorImpl implements ModelGenerator {
   private final ModelEngineAPI plugin;
   private final Gson gson;
   private final List<ModelParser> parsers = new ArrayList();
   private final List<ModelAssets> assets = new ArrayList();
   private final AtlasManager atlasManager;
   private final File blueprintFolder;
   private final File packFolder;
   private final File assetsFolder;
   private final File baseItemFolder;
   private final File zippedResourcePack;
   private final File cachedIDJson;
   private final Map<ModelGenerator.Phase, Set<Runnable>> tasks = Maps.newConcurrentMap();
   private final Set<ModelGenerator.Phase> executed = new HashSet();
   private ModelIdCache cache;
   private String namespace;
   private File modelFolder;
   private BaseItem baseItem;
   private BaseItemEnum baseItemType;
   private boolean generateMeta;
   private boolean initialized;

   public ModelGeneratorImpl(ModelEngineAPI plugin) {
      this.plugin = plugin;
      this.gson = plugin.getGson();
      this.blueprintFolder = TFile.createDirectory(plugin.getDataFolder(), "blueprints");
      this.packFolder = TFile.createDirectory(plugin.getDataFolder(), "resource pack");
      this.assetsFolder = TFile.createDirectory(this.packFolder, "assets");
      this.baseItemFolder = TFile.createDirectory(this.assetsFolder, "minecraft", "models", "item");
      this.zippedResourcePack = TFile.createFile(plugin.getDataFolder(), "resource pack.zip");
      this.cachedIDJson = TFile.createFileOrEmpty(plugin.getDataFolder(), ".data", "cache.json");
      this.atlasManager = new AtlasManager(this);
      this.refreshCache();
      plugin.getConfigManager().registerReferenceUpdate(this::updateConfig);
      this.parsers.add(new ModelEngineParser());
      this.parsers.add(new BlockbenchParser());
   }

   public void importModels(boolean isStartup) {
      this.executed.clear();
      if (isStartup && !ConfigProperty.LATE_REGISTER.getBoolean()) {
         this.importModelsInternal(true);
      } else {
         DualTicker.queueIOTask(() -> {
            this.importModelsInternal(isStartup);
         });
      }

   }

   public void generateAssets(boolean isStartup) {
      if (!ConfigProperty.LATE_REGISTER.getBoolean() && ConfigProperty.LATE_ASSETS.getBoolean()) {
         DualTicker.queueIOTask(() -> {
            this.generateAssetsInternal(isStartup);
         });
      } else {
         this.generateAssetsInternal(true);
      }

   }

   public void zipResourcePack(boolean isStartup) {
      if (!ConfigProperty.ZIP.getBoolean()) {
         this.executeQueuedTask(ModelGenerator.Phase.FINISHED);
      } else {
         if (!ConfigProperty.LATE_ASSETS.getBoolean() && ConfigProperty.LATE_ZIPPING.getBoolean()) {
            DualTicker.queueIOTask(this::zipResourcePackInternal);
         } else {
            this.zipResourcePackInternal();
         }

      }
   }

   public void updateConfig() {
      this.namespace = ConfigProperty.NAMESPACE.getString();
      this.modelFolder = TFile.createDirectory(this.assetsFolder, this.namespace, "models");
      this.changeBaseItem(ConfigProperty.ITEM_MODEL.getBaseItem());
      this.generateMeta = ConfigProperty.MCMETA.getBoolean();
   }

   public void queueTask(ModelGenerator.Phase post, Runnable task) {
      if (!this.executed.contains(ModelGenerator.Phase.FINISHED) && !this.executed.contains(post)) {
         ((Set)this.tasks.computeIfAbsent(post, (phase) -> {
            return new LinkedHashSet();
         })).add(task);
      } else {
         task.run();
      }

   }

   private void executeQueuedTask(ModelGenerator.Phase phase) {
      this.executed.add(phase);
      if (phase == ModelGenerator.Phase.FINISHED) {
         this.tasks.values().forEach((runnables) -> {
            runnables.forEach(Runnable::run);
         });
         this.tasks.clear();
      } else {
         this.tasks.computeIfPresent(phase, (p, runnables) -> {
            runnables.forEach(Runnable::run);
            runnables.clear();
            return runnables;
         });
      }

      ModelEngineAPI.callEvent(new ModelRegistrationEvent(phase));
   }

   public void changeBaseItem(BaseItemEnum base) {
      this.baseItemType = base;
      String name = base.name().toLowerCase(Locale.ENGLISH);
      InputStream inputStream = this.plugin.getResource("pack/colorable/" + name + ".json");
      if (inputStream == null) {
         TLogger.warn("Unknown colorable item: " + name + ". Reverting to use leather_horse_armor.");
         inputStream = this.plugin.getResource("pack/colorable/leather_horse_armor.json");
      }

      if (inputStream == null) {
         TLogger.error("Unable to locate base files.");
      } else {
         Reader itemTemplateReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
         this.baseItem = (BaseItem)this.gson.fromJson(itemTemplateReader, BaseItem.class);
         this.baseItem.setName(name);
      }
   }

   private void importModelsInternal(boolean isStartup) {
      TLogger.log();
      LogColor var10000 = LogColor.BOLD;
      TLogger.log(var10000 + LogColor.CYAN.toString() + "[Importing models]");
      this.initialized = false;
      this.executeQueuedTask(ModelGenerator.Phase.PRE_IMPORT);
      this.refreshCache();
      ModelRegistry registry = this.plugin.getModelRegistry();
      registry.clearRegistry();
      if (!this.blueprintFolder.isDirectory()) {
         this.initialized = true;
         this.executeQueuedTask(ModelGenerator.Phase.FINISHED);
      } else {
         List<File> files = Lists.newArrayList();
         File[] fa = this.blueprintFolder.listFiles();
         if (fa != null) {
            ((List)files).addAll(Arrays.asList(fa));
         }

         if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null) {
            try {
               ((List)files).addAll(MythicUtils.getPackModelFiles());
            } catch (Throwable var20) {
               var20.printStackTrace();
            }
         }

         if (((List)files).size() == 0) {
            this.initialized = true;
            this.executeQueuedTask(ModelGenerator.Phase.FINISHED);
         } else {
            Queue<File> compartment = new LinkedList();
            ((List)files).sort(Ordering.natural());

            while(true) {
               Iterator var6 = ((List)files).iterator();

               while(true) {
                  while(var6.hasNext()) {
                     File file = (File)var6.next();
                     if (!file.isFile()) {
                        if (file.isDirectory()) {
                           compartment.add(file);
                        }
                     } else {
                        boolean fileParsed = false;
                        TLogger.log();
                        var10000 = LogColor.CYAN;
                        TLogger.log(var10000 + "Importing " + file.getName() + ".");
                        Iterator var9 = this.parsers.iterator();

                        while(var9.hasNext()) {
                           ModelParser parser = (ModelParser)var9.next();
                           if (parser.validateFile(file)) {
                              try {
                                 Pair<ModelBlueprint, ModelAssets> modelData = parser.generate(file);
                                 if (modelData != null) {
                                    ModelBlueprint blueprint = (ModelBlueprint)modelData.left();
                                    ModelAssets asset = (ModelAssets)modelData.right();
                                    blueprint.finalizeModel();
                                    Iterator var14 = blueprint.getFlatMap().entrySet().iterator();

                                    while(var14.hasNext()) {
                                       Entry<String, BlueprintBone> boneEntry = (Entry)var14.next();
                                       BlueprintBone bone = (BlueprintBone)boneEntry.getValue();
                                       if (bone.isRenderer()) {
                                          this.cache.requestId(blueprint.getName(), bone);
                                       }
                                    }

                                    registry.registerBlueprint(blueprint);
                                    this.assets.add(asset);
                                    fileParsed = true;
                                    break;
                                 }
                              } catch (Exception var21) {
                                 var21.printStackTrace();
                                 break;
                              }
                           }
                        }

                        if (!fileParsed) {
                           IError.UNKNOWN_FORMAT.log();
                        }
                     }
                  }

                  File[] ca;
                  do {
                     if (compartment.isEmpty()) {
                        registry.sortIds();
                        this.initialized = true;

                        try {
                           FileWriter writer = new FileWriter(this.cachedIDJson);

                           try {
                              this.cache.endSession();
                              writer.write(this.gson.toJson(this.cache));
                           } catch (Throwable var18) {
                              try {
                                 writer.close();
                              } catch (Throwable var17) {
                                 var18.addSuppressed(var17);
                              }

                              throw var18;
                           }

                           writer.close();
                        } catch (IOException var19) {
                           var19.printStackTrace();
                        }

                        this.executeQueuedTask(ModelGenerator.Phase.POST_IMPORT);
                        this.generateAssets(isStartup);
                        return;
                     }

                     ca = ((File)compartment.poll()).listFiles();
                  } while(ca == null || ca.length == 0);

                  files = Arrays.asList(ca);
                  ((List)files).sort(Ordering.natural());
                  break;
               }
            }
         }
      }
   }

   private void generateAssetsInternal(boolean isStartup) {
      this.executeQueuedTask(ModelGenerator.Phase.PRE_ASSETS);
      this.baseItem.clearOverrides();
      this.atlasManager.reset();
      HashMap<String, BlueprintTexture.MCMeta> lazyMCMeta = new HashMap();
      Iterator var3 = this.assets.iterator();

      while(var3.hasNext()) {
         ModelAssets asset = (ModelAssets)var3.next();
         Iterator var5 = asset.getTextures().iterator();

         File modelJson;
         while(var5.hasNext()) {
            BlueprintTexture texture = (BlueprintTexture)var5.next();
            if (!"minecraft".equals(texture.getPath().getNamespace())) {
               this.atlasManager.addSingle(texture.getPath());
               modelJson = TFile.createFile(this.assetsFolder, "textures", texture.getPath(), "png");
               BufferedImage image = TFile.toImage(texture.getSource());

               try {
                  ImageIO.write(image, "png", modelJson);
               } catch (IOException var16) {
                  var16.printStackTrace();
               }

               if (texture.getMcMeta() != null && this.shouldGenerateMCMeta(image, texture)) {
                  File mcmeta = TFile.createFile(this.assetsFolder, "textures", texture.getPath(), "png.mcmeta");

                  try {
                     FileWriter writer = new FileWriter(mcmeta);

                     try {
                        lazyMCMeta.put("animation", texture.getMcMeta());
                        writer.write(this.gson.toJson(lazyMCMeta));
                     } catch (Throwable var18) {
                        try {
                           writer.close();
                        } catch (Throwable var17) {
                           var18.addSuppressed(var17);
                        }

                        throw var18;
                     }

                     writer.close();
                  } catch (IOException var19) {
                     var19.printStackTrace();
                  }
               }
            }
         }

         var5 = asset.getModels().values().iterator();

         while(var5.hasNext()) {
            JavaItemModel model = (JavaItemModel)var5.next();
            model.finalizeModel();
            modelJson = TFile.createFile(this.modelFolder, asset.getName(), model.getName() + ".json");

            try {
               FileWriter writer = new FileWriter(modelJson);

               try {
                  writer.write(this.gson.toJson(model));
               } catch (Throwable var21) {
                  try {
                     writer.close();
                  } catch (Throwable var20) {
                     var21.addSuppressed(var20);
                  }

                  throw var21;
               }

               writer.close();
            } catch (IOException var22) {
               var22.printStackTrace();
            }

            Map var10000 = this.cache.cachedId;
            String var10001 = asset.getName();
            Integer id = (Integer)var10000.get(var10001 + ":" + model.getName());
            if (id != null) {
               var10001 = this.namespace;
               this.baseItem.addModel(var10001 + ":" + asset.getName() + "/" + model.getName(), id);
            }
         }
      }

      this.assets.clear();
      this.baseItem.sortOverrides();
      File baseItemFile = TFile.createFile(this.baseItemFolder, this.baseItem.getName() + ".json");

      try {
         FileWriter writer = new FileWriter(baseItemFile);

         try {
            writer.write(this.gson.toJson(this.baseItem));
         } catch (Throwable var14) {
            try {
               writer.close();
            } catch (Throwable var13) {
               var14.addSuppressed(var13);
            }

            throw var14;
         }

         writer.close();
      } catch (IOException var15) {
         var15.printStackTrace();
      }

      if (ConfigProperty.ATLAS.getBoolean()) {
         this.atlasManager.generateFile();
      }

      TFile.copyResource(this.plugin, this.packFolder, "pack", "pack.png");
      TFile.copyResource(this.plugin, this.packFolder, "pack", "pack.mcmeta");
      TFile.copyResource(this.plugin, this.packFolder, "pack", "assets/minecraft/models/custom/entities/player/head.json");
      TFile.copyResource(this.plugin, this.packFolder, "pack", "assets/minecraft/models/custom/entities/player/left_arm.json");
      TFile.copyResource(this.plugin, this.packFolder, "pack", "assets/minecraft/models/custom/entities/player/left_leg.json");
      TFile.copyResource(this.plugin, this.packFolder, "pack", "assets/minecraft/models/custom/entities/player/right_arm.json");
      TFile.copyResource(this.plugin, this.packFolder, "pack", "assets/minecraft/models/custom/entities/player/right_leg.json");
      TFile.copyResource(this.plugin, this.packFolder, "pack", "assets/minecraft/models/custom/entities/player/slim_left.json");
      TFile.copyResource(this.plugin, this.packFolder, "pack", "assets/minecraft/models/custom/entities/player/slim_right.json");
      TFile.copyResource(this.plugin, this.packFolder, "pack", "assets/minecraft/models/custom/entities/player/torso.json");
      TFile.copyResource(this.plugin, this.packFolder, "pack", "assets/minecraft/models/item/player_head.json");
      if (ConfigProperty.SHADER.getBoolean()) {
         TFile.copyResource(this.plugin, this.packFolder, "pack", "assets/minecraft/shaders/core/rendertype_entity_translucent.fsh");
         TFile.copyResource(this.plugin, this.packFolder, "pack", "assets/minecraft/shaders/core/rendertype_entity_translucent.json");
         TFile.copyResource(this.plugin, this.packFolder, "pack", "assets/minecraft/shaders/core/rendertype_entity_translucent.vsh");
      }

      this.executeQueuedTask(ModelGenerator.Phase.POST_ASSETS);
      this.zipResourcePack(isStartup);
   }

   private void zipResourcePackInternal() {
      this.executeQueuedTask(ModelGenerator.Phase.PRE_ZIPPING);

      try {
         FileOutputStream zippedFOS = new FileOutputStream(this.zippedResourcePack);
         ZipOutputStream zipOut = new ZipOutputStream(zippedFOS);
         File[] files = this.packFolder.listFiles();
         if (files == null) {
            this.executeQueuedTask(ModelGenerator.Phase.FINISHED);
            return;
         }

         File[] var4 = files;
         int var5 = files.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            File file = var4[var6];
            this.zipFile(file, file.getName(), zipOut);
         }

         zipOut.close();
         zippedFOS.close();
         TLogger.log();
         TLogger.log(LogColor.BRIGHT_GREEN + "Resource pack zipped.");
      } catch (IOException var8) {
         var8.printStackTrace();
      }

      this.executeQueuedTask(ModelGenerator.Phase.POST_ZIPPING);
      this.executeQueuedTask(ModelGenerator.Phase.FINISHED);
   }

   private void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
      if (!fileToZip.isHidden()) {
         int length;
         if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
               zipOut.putNextEntry(new ZipEntry(fileName));
            } else {
               zipOut.putNextEntry(new ZipEntry(fileName + "/"));
            }

            zipOut.closeEntry();
            File[] children = fileToZip.listFiles();
            if (children != null) {
               File[] var10 = children;
               int var11 = children.length;

               for(length = 0; length < var11; ++length) {
                  File childFile = var10[length];
                  this.zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
               }
            }

         } else {
            FileInputStream fis = new FileInputStream(fileToZip);
            ZipEntry zipEntry = new ZipEntry(fileName);
            zipOut.putNextEntry(zipEntry);
            byte[] bytes = new byte[1024];

            while((length = fis.read(bytes)) >= 0) {
               zipOut.write(bytes, 0, length);
            }

            fis.close();
         }
      }
   }

   private boolean shouldGenerateMCMeta(BufferedImage image, BlueprintTexture texture) {
      if (texture.getMcMeta().isMustGenerate()) {
         return true;
      } else if (!this.generateMeta) {
         return false;
      } else {
         float textureRatio = (float)image.getHeight() / (float)image.getWidth();
         float uvRatio = (float)texture.getFrameHeight() / (float)texture.getFrameWidth();
         if (TMath.isSimilar(textureRatio, uvRatio)) {
            return false;
         } else {
            return textureRatio / uvRatio > 1.0F;
         }
      }
   }

   private void refreshCache() {
      TFile.recreateFile(this.cachedIDJson);

      try {
         FileReader reader = new FileReader(this.cachedIDJson);

         try {
            this.cache = (ModelIdCache)this.gson.fromJson(reader, ModelIdCache.class);
            if (this.cache == null) {
               this.cache = new ModelIdCache();
            }
         } catch (Throwable var5) {
            try {
               reader.close();
            } catch (Throwable var4) {
               var5.addSuppressed(var4);
            }

            throw var5;
         }

         reader.close();
      } catch (IOException var6) {
         this.cache = new ModelIdCache();
         TLogger.error("Unable to read the model ID cache file. Is it corrupted?");
         var6.printStackTrace();
      }

   }

   public ModelEngineAPI getPlugin() {
      return this.plugin;
   }

   public Gson getGson() {
      return this.gson;
   }

   public List<ModelParser> getParsers() {
      return this.parsers;
   }

   public List<ModelAssets> getAssets() {
      return this.assets;
   }

   public AtlasManager getAtlasManager() {
      return this.atlasManager;
   }

   public File getBlueprintFolder() {
      return this.blueprintFolder;
   }

   public File getPackFolder() {
      return this.packFolder;
   }

   public File getAssetsFolder() {
      return this.assetsFolder;
   }

   public File getBaseItemFolder() {
      return this.baseItemFolder;
   }

   public File getZippedResourcePack() {
      return this.zippedResourcePack;
   }

   public File getCachedIDJson() {
      return this.cachedIDJson;
   }

   public Map<ModelGenerator.Phase, Set<Runnable>> getTasks() {
      return this.tasks;
   }

   public Set<ModelGenerator.Phase> getExecuted() {
      return this.executed;
   }

   public ModelIdCache getCache() {
      return this.cache;
   }

   public String getNamespace() {
      return this.namespace;
   }

   public File getModelFolder() {
      return this.modelFolder;
   }

   public BaseItem getBaseItem() {
      return this.baseItem;
   }

   public BaseItemEnum getBaseItemType() {
      return this.baseItemType;
   }

   public boolean isGenerateMeta() {
      return this.generateMeta;
   }

   public boolean isInitialized() {
      return this.initialized;
   }
}
