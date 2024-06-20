package com.ticxo.modelengine.api.model.render;

import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.nms.RenderParsers;

public interface ModelRenderer {
   ActiveModel getActiveModel();

   boolean isInitialized();

   void initialize();

   void readModelData();

   void sendToClient(RenderParsers var1);

   void destroy(RenderParsers var1);

   void createRealEntities();

   boolean pollFirstSpawn();
}
