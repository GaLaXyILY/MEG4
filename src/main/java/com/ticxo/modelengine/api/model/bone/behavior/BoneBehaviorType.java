package com.ticxo.modelengine.api.model.bone.behavior;

import com.google.gson.JsonDeserializer;
import com.ticxo.modelengine.api.error.ErrorMissingBoneBehaviorData;
import com.ticxo.modelengine.api.error.ErrorWrongBoneBehaviorDataType;
import com.ticxo.modelengine.api.generator.blueprint.BlueprintBone;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.bone.ModelBone;
import com.ticxo.modelengine.api.model.bone.manager.BehaviorManager;
import com.ticxo.modelengine.api.model.bone.render.DefaultRenderType;
import com.ticxo.modelengine.api.model.bone.render.IRenderType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Predicate;
import org.jetbrains.annotations.Nullable;

public class BoneBehaviorType<T extends BoneBehavior> {
   private final BoneBehaviorType.BehaviorProvider<T> behaviorProvider;
   private final BoneBehaviorType.BehaviorManagerProvider<T> behaviorManagerProvider;
   private final String id;
   private final Map<String, Class<?>> requiredArguments;
   private final Map<String, Class<?>> optionalArguments;
   private final Map<Class<?>, JsonDeserializer<?>> dataDeserializer;
   private final IRenderType renderType;
   private final Set<ProceduralType> proceduralTypes;
   private final Predicate<Set<BoneBehaviorType<?>>> predicate;
   private final BoneBehaviorType.BehaviorProvider<T> forcedBehaviorProvider;
   private final boolean ignoreCubes;

   public void assignCachedProvider(BlueprintBone bone, Map<String, Object> data) {
      Map<String, Object> verifiedData = new HashMap();
      Iterator var4 = this.requiredArguments.entrySet().iterator();

      Entry entry;
      String key;
      Class clazz;
      Object value;
      while(var4.hasNext()) {
         entry = (Entry)var4.next();
         key = (String)entry.getKey();
         clazz = (Class)entry.getValue();
         value = data.get(key);
         if (value == null) {
            new ErrorMissingBoneBehaviorData(bone.getName(), this, key);
            return;
         }

         if (!clazz.isAssignableFrom(value.getClass())) {
            new ErrorWrongBoneBehaviorDataType(bone.getName(), this, key, clazz, value.getClass());
            return;
         }

         verifiedData.put(key, value);
      }

      var4 = this.optionalArguments.entrySet().iterator();

      while(var4.hasNext()) {
         entry = (Entry)var4.next();
         key = (String)entry.getKey();
         clazz = (Class)entry.getValue();
         value = data.get(key);
         if (value != null) {
            if (!clazz.isAssignableFrom(value.getClass())) {
               new ErrorWrongBoneBehaviorDataType(bone.getName(), this, key, clazz, value.getClass());
               return;
            }

            verifiedData.put(key, value);
         }
      }

      bone.getCachedBehaviorProvider().put(this, new BoneBehaviorType.CachedProvider(this.behaviorProvider, this, new BoneBehaviorData(verifiedData)));
   }

   public void assignForcedCachedProvider(BlueprintBone bone) {
      if (this.forcedBehaviorProvider != null) {
         if (!bone.getCachedBehaviorProvider().containsKey(this)) {
            bone.getCachedBehaviorProvider().put(this, new BoneBehaviorType.CachedProvider(this.forcedBehaviorProvider, this, new BoneBehaviorData(new HashMap())));
         }

      }
   }

   public boolean test(Set<BoneBehaviorType<?>> types) {
      return this.predicate.test(types);
   }

   public static boolean noProcedural(Set<BoneBehaviorType<?>> types) {
      Iterator var1 = types.iterator();

      BoneBehaviorType type;
      do {
         if (!var1.hasNext()) {
            return true;
         }

         type = (BoneBehaviorType)var1.next();
      } while(type.getProceduralTypes().isEmpty());

      return false;
   }

   public BoneBehaviorType.BehaviorProvider<T> getBehaviorProvider() {
      return this.behaviorProvider;
   }

   public BoneBehaviorType.BehaviorManagerProvider<T> getBehaviorManagerProvider() {
      return this.behaviorManagerProvider;
   }

   public String getId() {
      return this.id;
   }

   public Map<String, Class<?>> getRequiredArguments() {
      return this.requiredArguments;
   }

   public Map<String, Class<?>> getOptionalArguments() {
      return this.optionalArguments;
   }

   public Map<Class<?>, JsonDeserializer<?>> getDataDeserializer() {
      return this.dataDeserializer;
   }

   public IRenderType getRenderType() {
      return this.renderType;
   }

   public Set<ProceduralType> getProceduralTypes() {
      return this.proceduralTypes;
   }

   public Predicate<Set<BoneBehaviorType<?>>> getPredicate() {
      return this.predicate;
   }

   public BoneBehaviorType.BehaviorProvider<T> getForcedBehaviorProvider() {
      return this.forcedBehaviorProvider;
   }

   public boolean isIgnoreCubes() {
      return this.ignoreCubes;
   }

   protected BoneBehaviorType(BoneBehaviorType.BehaviorProvider<T> behaviorProvider, BoneBehaviorType.BehaviorManagerProvider<T> behaviorManagerProvider, String id, Map<String, Class<?>> requiredArguments, Map<String, Class<?>> optionalArguments, Map<Class<?>, JsonDeserializer<?>> dataDeserializer, IRenderType renderType, Set<ProceduralType> proceduralTypes, Predicate<Set<BoneBehaviorType<?>>> predicate, BoneBehaviorType.BehaviorProvider<T> forcedBehaviorProvider, boolean ignoreCubes) {
      this.behaviorProvider = behaviorProvider;
      this.behaviorManagerProvider = behaviorManagerProvider;
      this.id = id;
      this.requiredArguments = requiredArguments;
      this.optionalArguments = optionalArguments;
      this.dataDeserializer = dataDeserializer;
      this.renderType = renderType;
      this.proceduralTypes = proceduralTypes;
      this.predicate = predicate;
      this.forcedBehaviorProvider = forcedBehaviorProvider;
      this.ignoreCubes = ignoreCubes;
   }

   public static class CachedProvider<T extends BoneBehavior> {
      private final BoneBehaviorType.BehaviorProvider<T> behaviorProvider;
      private final BoneBehaviorType<T> type;
      private final BoneBehaviorData data;

      public T create(ModelBone bone) {
         return this.behaviorProvider.create(bone, this.type, this.data);
      }

      public CachedProvider(BoneBehaviorType.BehaviorProvider<T> behaviorProvider, BoneBehaviorType<T> type, BoneBehaviorData data) {
         this.behaviorProvider = behaviorProvider;
         this.type = type;
         this.data = data;
      }

      public BoneBehaviorType<T> getType() {
         return this.type;
      }

      public BoneBehaviorData getData() {
         return this.data;
      }
   }

   @FunctionalInterface
   public interface BehaviorProvider<T extends BoneBehavior> {
      T create(ModelBone var1, BoneBehaviorType<T> var2, BoneBehaviorData var3);
   }

   @FunctionalInterface
   public interface BehaviorManagerProvider<T extends BoneBehavior> {
      BehaviorManager<T> create(ActiveModel var1, BoneBehaviorType<T> var2);
   }

   public static class Builder<T extends BoneBehavior> {
      private final BoneBehaviorType.BehaviorProvider<T> behaviorProvider;
      private final BoneBehaviorType.BehaviorManagerProvider<T> behaviorManagerProvider;
      private final String id;
      private final Map<String, Class<?>> requiredArguments = new HashMap();
      private final Map<String, Class<?>> optionalArguments = new HashMap();
      private final Map<Class<?>, JsonDeserializer<?>> dataDeserializer = new HashMap();
      private final Set<ProceduralType> proceduralTypes = new HashSet();
      private IRenderType renderType;
      private Predicate<Set<BoneBehaviorType<?>>> predicate;
      private BoneBehaviorType.BehaviorProvider<T> forcedBehaviorProvider;
      private boolean ignoreCubes;

      public static <T extends BoneBehavior> BoneBehaviorType.Builder<T> of(BoneBehaviorType.BehaviorProvider<T> behaviorProvider, @Nullable BoneBehaviorType.BehaviorManagerProvider<T> behaviorManagerProvider, String id) {
         return new BoneBehaviorType.Builder(behaviorProvider, behaviorManagerProvider, id);
      }

      public BoneBehaviorType.Builder<T> required(String key, Class<?> type) {
         this.requiredArguments.put(key, type);
         return this;
      }

      public <S> BoneBehaviorType.Builder<T> required(String key, Class<S> type, JsonDeserializer<S> deserializer) {
         this.requiredArguments.put(key, type);
         this.dataDeserializer.put(type, deserializer);
         return this;
      }

      public BoneBehaviorType.Builder<T> optional(String key, Class<?> type) {
         this.optionalArguments.put(key, type);
         return this;
      }

      public <S> BoneBehaviorType.Builder<T> optional(String key, Class<S> type, JsonDeserializer<S> deserializer) {
         this.optionalArguments.put(key, type);
         this.dataDeserializer.put(type, deserializer);
         return this;
      }

      public BoneBehaviorType.Builder<T> renderType(IRenderType type) {
         this.renderType = type;
         return this;
      }

      public BoneBehaviorType.Builder<T> procedural(ProceduralType... types) {
         this.proceduralTypes.addAll(Arrays.asList(types));
         return this;
      }

      public BoneBehaviorType.Builder<T> predicate(Predicate<Set<BoneBehaviorType<?>>> predicate) {
         this.predicate = predicate;
         return this;
      }

      public BoneBehaviorType.Builder<T> forced(BoneBehaviorType.BehaviorProvider<T> forcedBehaviorProvider) {
         this.forcedBehaviorProvider = forcedBehaviorProvider;
         return this;
      }

      public BoneBehaviorType.Builder<T> ignoreCubes() {
         this.ignoreCubes = true;
         return this;
      }

      public BoneBehaviorType<T> build() {
         return new BoneBehaviorType(this.behaviorProvider, this.behaviorManagerProvider, this.id, this.requiredArguments, this.optionalArguments, this.dataDeserializer, this.renderType, this.proceduralTypes, this.predicate, this.forcedBehaviorProvider, this.ignoreCubes);
      }

      protected Builder(BoneBehaviorType.BehaviorProvider<T> behaviorProvider, BoneBehaviorType.BehaviorManagerProvider<T> behaviorManagerProvider, String id) {
         this.renderType = DefaultRenderType.ANY;
         this.predicate = (boneBehaviorTypes) -> {
            return true;
         };
         this.behaviorProvider = behaviorProvider;
         this.behaviorManagerProvider = behaviorManagerProvider;
         this.id = id;
      }
   }
}
