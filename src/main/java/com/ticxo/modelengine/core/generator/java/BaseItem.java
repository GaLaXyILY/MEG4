package com.ticxo.modelengine.core.generator.java;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class BaseItem {
   private final List<BaseItem.JavaOverride> overrides = new ArrayList();
   private transient String name;
   private String parent;
   private Map<String, String> textures;

   public void addModel(String model, int id) {
      this.overrides.add(new BaseItem.JavaOverride(model, id));
   }

   public void clearOverrides() {
      this.overrides.clear();
   }

   public void sortOverrides() {
      this.overrides.sort(Comparator.comparingInt((o) -> {
         return o.getPredicate().custom_model_data;
      }));
   }

   public List<BaseItem.JavaOverride> getOverrides() {
      return this.overrides;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   static class JavaOverride {
      private final BaseItem.JavaPredicate predicate;
      private final String model;

      public JavaOverride(String model, int id) {
         this.model = model;
         this.predicate = new BaseItem.JavaPredicate(id);
      }

      public BaseItem.JavaPredicate getPredicate() {
         return this.predicate;
      }

      public String getModel() {
         return this.model;
      }
   }

   static class JavaPredicate {
      private final int custom_model_data;

      public JavaPredicate(int custom_model_data) {
         this.custom_model_data = custom_model_data;
      }
   }
}
