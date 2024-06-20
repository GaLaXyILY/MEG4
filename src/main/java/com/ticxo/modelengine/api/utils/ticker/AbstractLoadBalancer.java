package com.ticxo.modelengine.api.utils.ticker;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public abstract class AbstractLoadBalancer<KEY, SER extends LoadBalancer.Server> implements LoadBalancer<KEY, SER> {
   protected final Set<SER> available = new HashSet();
   protected final Map<KEY, SER> reference = new HashMap();

   public AbstractLoadBalancer(int counts) {
      if (counts <= 0) {
         throw new RuntimeException("Cannot create load balancer with less than 1 server: " + counts);
      } else {
         for(int i = 0; i < counts; ++i) {
            this.available.add(this.supply());
         }

      }
   }

   public SER getOrRegister(KEY key) {
      SER current = (LoadBalancer.Server)this.reference.get(key);
      if (current != null) {
         return current;
      } else {
         int load = Integer.MAX_VALUE;
         SER leastLoad = null;
         Iterator var5 = this.available.iterator();

         while(var5.hasNext()) {
            SER server = (LoadBalancer.Server)var5.next();
            if (server.getLoad() < load) {
               leastLoad = server;
               load = server.getLoad();
            }
         }

         if (leastLoad == null) {
            throw new RuntimeException("No server found.");
         } else {
            this.reference.put(key, leastLoad);
            return leastLoad;
         }
      }
   }

   public SER get(KEY key) {
      return (LoadBalancer.Server)this.reference.get(key);
   }

   public void unregister(KEY key) {
      this.reference.remove(key);
   }

   public void execute(KEY key, BiConsumer<KEY, SER> balConsumer) {
      balConsumer.accept(key, this.getOrRegister(key));
   }

   public Set<SER> getAvailable() {
      return this.available;
   }
}
