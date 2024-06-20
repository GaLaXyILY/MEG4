package com.ticxo.modelengine.core.generator.atlas;

import com.ticxo.modelengine.api.utils.TFile;
import com.ticxo.modelengine.api.utils.data.ResourceLocation;
import com.ticxo.modelengine.core.generator.ModelGeneratorImpl;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class AtlasManager {
   private final ModelGeneratorImpl generator;
   private final File atlases;
   private final Set<ResourceLocation> registeredPaths = new HashSet();
   private final Atlas atlas;

   public AtlasManager(ModelGeneratorImpl generator) {
      this.generator = generator;
      this.atlases = TFile.createDirectory(generator.getPackFolder(), "assets", "minecraft", "atlases");
      this.atlas = new Atlas();
      this.reset();
   }

   public void reset() {
      this.registeredPaths.clear();
      this.atlas.getSources().clear();
      this.atlas.getSources().add(new Atlas.Directory("entity"));
   }

   public void addSingle(ResourceLocation location) {
      String path = location.getPath();
      if (!path.startsWith("entity") && !path.startsWith("item") && !path.startsWith("block")) {
         if (this.registeredPaths.add(location)) {
            this.atlas.getSources().add(new Atlas.Single(location.toString()));
         }
      }
   }

   public void generateFile() {
      try {
         File file = TFile.createFile(this.atlases, "blocks.json");
         FileWriter writer = new FileWriter(file);
         writer.write(this.generator.getGson().toJson(this.atlas));
         writer.close();
      } catch (IOException var3) {
         var3.printStackTrace();
      }

   }

   public Atlas getAtlas() {
      return this.atlas;
   }
}
