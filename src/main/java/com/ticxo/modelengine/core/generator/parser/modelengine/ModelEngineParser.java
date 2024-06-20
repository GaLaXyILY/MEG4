package com.ticxo.modelengine.core.generator.parser.modelengine;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ticxo.modelengine.api.generator.assets.ModelAssets;
import com.ticxo.modelengine.api.generator.blueprint.ModelBlueprint;
import com.ticxo.modelengine.api.generator.parser.ModelParser;
import com.ticxo.modelengine.api.utils.TFile;
import com.ticxo.modelengine.core.generator.parser.blockbench.json.BlockbenchDeserializer;
import com.ticxo.modelengine.core.generator.parser.modelengine.json.ExtendedBlockbenchModel;
import com.ticxo.modelengine.core.generator.parser.modelengine.json.ModelEngineDeserializer;
import com.ticxo.modelengine.core.generator.parser.modelengine.json.ModelEngineExtraData;
import it.unimi.dsi.fastutil.Pair;
import java.io.File;
import java.io.FileReader;
import java.util.Locale;

public class ModelEngineParser implements ModelParser {
   private final Gson gson = (new GsonBuilder()).registerTypeAdapter(ExtendedBlockbenchModel.class, new BlockbenchDeserializer(ExtendedBlockbenchModel::new, false)).registerTypeAdapter(ModelEngineExtraData.class, new ModelEngineDeserializer()).create();

   public boolean validateFile(File file) {
      return TFile.isExtension(file.getName(), "megmodel");
   }

   public Pair<ModelBlueprint, ModelAssets> generate(File file) throws Exception {
      String modelName = TFile.removeExtension(file.getName()).toLowerCase(Locale.ENGLISH);
      FileReader reader = new FileReader(file);
      FileReader reader2 = new FileReader(file);
      ExtendedBlockbenchModel blockbenchModel = (ExtendedBlockbenchModel)this.gson.fromJson(reader, ExtendedBlockbenchModel.class);
      ModelEngineExtraData modelEngineData = (ModelEngineExtraData)this.gson.fromJson(reader2, ModelEngineExtraData.class);
      blockbenchModel.setExtraData(modelEngineData);
      ModelBlueprint blueprint = new ModelBlueprint();
      blueprint.setName(modelName);
      blockbenchModel.populateBlueprint(blueprint);
      blueprint.constructFlatBoneMap();
      blueprint.cacheBoneBehaviors();
      ModelAssets assets = new ModelAssets();
      assets.setName(modelName);
      blockbenchModel.populateAssets(blueprint, assets);
      reader.close();
      reader2.close();
      return Pair.of(blueprint, assets);
   }
}
