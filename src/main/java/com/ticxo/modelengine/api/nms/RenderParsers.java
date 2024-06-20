package com.ticxo.modelengine.api.nms;

import com.ticxo.modelengine.api.model.bone.render.BehaviorRenderer;
import com.ticxo.modelengine.api.model.bone.render.BehaviorRendererParser;
import com.ticxo.modelengine.api.model.render.ModelRenderer;
import com.ticxo.modelengine.api.model.render.ModelRendererParser;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

public class RenderParsers {
   private final Map<Predicate<?>, ModelRendererParser<?>> modelParsers = new HashMap();
   private final Map<Predicate<?>, BehaviorRendererParser<?>> behaviorParsers = new HashMap();

   public void registerModelParser(Predicate<ModelRenderer> predicate, ModelRendererParser<?> parser) {
      this.modelParsers.put(predicate, parser);
   }

   public void registerBehaviorParser(Predicate<BehaviorRenderer> predicate, BehaviorRendererParser<?> parser) {
      this.behaviorParsers.put(predicate, parser);
   }

   public <T extends ModelRenderer> ModelRendererParser<T> getModelParser(T renderer) {
      Iterator var2 = this.modelParsers.entrySet().iterator();

      Entry entry;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         entry = (Entry)var2.next();
      } while(!((Predicate)entry.getKey()).test(renderer));

      return (ModelRendererParser)entry.getValue();
   }

   public <T extends BehaviorRenderer> BehaviorRendererParser<T> getBehaviorParser(T renderer) {
      Iterator var2 = this.behaviorParsers.entrySet().iterator();

      Entry entry;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         entry = (Entry)var2.next();
      } while(!((Predicate)entry.getKey()).test(renderer));

      return (BehaviorRendererParser)entry.getValue();
   }
}
