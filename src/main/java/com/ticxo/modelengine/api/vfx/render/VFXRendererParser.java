package com.ticxo.modelengine.api.vfx.render;

public interface VFXRendererParser<T extends VFXRenderer> {
   void sendToClients(T var1);

   void destroy(T var1);
}
