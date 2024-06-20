package com.ticxo.modelengine.api.utils.ticker;

import java.util.function.BiConsumer;
import java.util.function.Function;

public interface LoadBalancer<KEY, SER extends LoadBalancer.Server> {
   SER getOrRegister(KEY var1);

   SER get(KEY var1);

   void unregister(KEY var1);

   void execute(KEY var1, BiConsumer<KEY, SER> var2);

   SER supply();

   default <VAL> VAL map(KEY key, Function<SER, VAL> mapper) {
      return mapper.apply(this.getOrRegister(key));
   }

   public interface Server {
      int getLoad();
   }
}
