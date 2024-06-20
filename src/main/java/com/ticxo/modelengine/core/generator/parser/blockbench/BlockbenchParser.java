package com.ticxo.modelengine.core.generator.parser.blockbench;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ticxo.modelengine.api.generator.assets.ModelAssets;
import com.ticxo.modelengine.api.generator.blueprint.ModelBlueprint;
import com.ticxo.modelengine.api.generator.parser.ModelParser;
import com.ticxo.modelengine.api.utils.TFile;
import com.ticxo.modelengine.core.generator.parser.blockbench.json.BlockbenchDeserializer;
import com.ticxo.modelengine.core.generator.parser.blockbench.json.BlockbenchModel;
import it.unimi.dsi.fastutil.Pair;
import java.io.File;
import java.io.FileReader;
import java.util.Locale;

public class BlockbenchParser implements ModelParser {
   private final Gson gson = (new GsonBuilder()).registerTypeAdapter(BlockbenchModel.class, new BlockbenchDeserializer(BlockbenchModel::new, true)).create();

   public boolean validateFile(File file) {
      return TFile.isExtension(file.getName(), "bbmodel");
   }

   public Pair<ModelBlueprint, ModelAssets> generate(File file) throws Exception {
      String modelName = TFile.removeExtension(file.getName()).toLowerCase(Locale.ENGLISH);
      FileReader reader = new FileReader(file);
      BlockbenchModel blockbenchModel = (BlockbenchModel)this.gson.fromJson(reader, BlockbenchModel.class);
      ModelBlueprint blueprint = new ModelBlueprint();
      blueprint.setName(modelName);
      blockbenchModel.populateBlueprint(blueprint);
      blueprint.constructFlatBoneMap();
      blueprint.cacheBoneBehaviors();
      ModelAssets assets = new ModelAssets();
      assets.setName(modelName);
      blockbenchModel.populateAssets(blueprint, assets);
      reader.close();
      return Pair.of(blueprint, assets);
   }
}
