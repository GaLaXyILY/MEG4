package com.ticxo.modelengine.api.generator.assets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModelAssets {
   private final List<BlueprintTexture> textures = new ArrayList();
   private final Map<String, JavaItemModel> models = new HashMap();
   private String name;

   public List<BlueprintTexture> getTextures() {
      return this.textures;
   }

   public Map<String, JavaItemModel> getModels() {
      return this.models;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }
}
