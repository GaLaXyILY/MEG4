package com.ticxo.modelengine.api.model.bone.behavior;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.jetbrains.annotations.Nullable;

public class BoneBehaviorRegistry {
   private final Map<String, BoneBehaviorType<?>> idRegistry = new HashMap();
   private Gson gson;

   public void register(BoneBehaviorType<?> type) {
      this.idRegistry.put(type.getId(), type);
   }

   @Nullable
   public BoneBehaviorType<?> getById(String id) {
      return (BoneBehaviorType)this.idRegistry.get(id);
   }

   public Set<String> getIds() {
      return ImmutableSet.copyOf(this.idRegistry.keySet());
   }

   public Gson getGson() {
      if (this.gson == null) {
         GsonBuilder builder = new GsonBuilder();
         this.idRegistry.forEach((s, boneBehaviorType) -> {
            Map var10000 = boneBehaviorType.getDataDeserializer();
            Objects.requireNonNull(builder);
            var10000.forEach(builder::registerTypeAdapter);
         });
         this.gson = builder.create();
      }

      return this.gson;
   }
}
