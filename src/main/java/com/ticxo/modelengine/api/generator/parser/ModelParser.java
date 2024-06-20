package com.ticxo.modelengine.api.generator.parser;

import com.ticxo.modelengine.api.generator.assets.ModelAssets;
import com.ticxo.modelengine.api.generator.blueprint.ModelBlueprint;
import it.unimi.dsi.fastutil.Pair;
import java.io.File;

public interface ModelParser {
   boolean validateFile(File var1);

   Pair<ModelBlueprint, ModelAssets> generate(File var1) throws Exception;
}
