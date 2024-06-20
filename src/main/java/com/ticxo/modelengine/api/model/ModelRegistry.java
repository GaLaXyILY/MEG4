package com.ticxo.modelengine.api.model;

import com.google.common.collect.Ordering;
import com.ticxo.modelengine.api.generator.blueprint.ModelBlueprint;
import com.ticxo.modelengine.api.utils.registry.TUnaryRegistry;
import java.util.ArrayList;
import java.util.List;

public class ModelRegistry extends TUnaryRegistry<ModelBlueprint> {
   private final List<String> orderedId = new ArrayList();

   public void registerBlueprint(ModelBlueprint blueprint) {
      this.orderedId.add(blueprint.getName());
      this.register(blueprint.getName(), blueprint);
   }

   public void clearRegistry() {
      this.orderedId.clear();
      this.registry.clear();
   }

   public void sortIds() {
      this.orderedId.sort(Ordering.natural());
   }

   public List<String> getOrderedId() {
      return this.orderedId;
   }
}
