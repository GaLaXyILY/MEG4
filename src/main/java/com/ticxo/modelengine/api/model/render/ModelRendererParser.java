package com.ticxo.modelengine.api.model.render;

public interface ModelRendererParser<T extends ModelRenderer> {
   void sendToClients(T var1);

   void destroy(T var1);
}
