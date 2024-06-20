package com.ticxo.modelengine.core.generator;

import com.ticxo.modelengine.api.generator.blueprint.BlueprintBone;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.Map.Entry;

public class ModelIdCache {
   protected final Map<String, Integer> cachedId = new HashMap();
   protected final Queue<Integer> unused = new LinkedList();
   private final transient Map<String, BlueprintBone> requested = new LinkedHashMap();
   private final transient Set<String> pendingRemove = new HashSet();
   protected int nextId = 1;

   public void endSession() {
      int maxId = 0;
      Iterator var2 = this.cachedId.entrySet().iterator();

      Entry entry;
      String modelId;
      while(var2.hasNext()) {
         entry = (Entry)var2.next();
         modelId = (String)entry.getKey();
         Integer dataId = (Integer)entry.getValue();
         if (this.requested.containsKey(modelId)) {
            ((BlueprintBone)this.requested.remove(modelId)).setDataId(dataId);
            maxId = Math.max(dataId, maxId);
         } else {
            this.pendingRemove.add(modelId);
            this.unused.add((Integer)this.cachedId.get(modelId));
         }
      }

      Set var10000 = this.pendingRemove;
      Map var10001 = this.cachedId;
      Objects.requireNonNull(var10001);
      var10000.forEach(var10001::remove);
      this.pendingRemove.clear();
      var2 = this.requested.entrySet().iterator();

      while(var2.hasNext()) {
         entry = (Entry)var2.next();
         modelId = (String)entry.getKey();
         BlueprintBone bone = (BlueprintBone)entry.getValue();
         if (!this.unused.isEmpty()) {
            bone.setDataId((Integer)this.unused.poll());
         } else {
            bone.setDataId(this.nextId++);
         }

         maxId = Math.max(bone.getDataId(), maxId);
         this.cachedId.put(modelId, bone.getDataId());
      }

      int id;
      for(var2 = this.unused.iterator(); var2.hasNext(); maxId = Math.max(maxId, id)) {
         id = (Integer)var2.next();
      }

      this.nextId = maxId + 1;
      this.requested.clear();
   }

   public void requestId(String modelId, BlueprintBone bone) {
      this.requested.put(modelId + ":" + bone.getName(), bone);
   }
}
