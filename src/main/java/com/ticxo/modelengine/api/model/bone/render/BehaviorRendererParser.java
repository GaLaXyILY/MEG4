package com.ticxo.modelengine.api.model.bone.render;

public interface BehaviorRendererParser<T extends BehaviorRenderer> {
   void sendToClients(T var1);

   void destroy(T var1);
}
