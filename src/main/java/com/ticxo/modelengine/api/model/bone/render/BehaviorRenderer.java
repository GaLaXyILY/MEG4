package com.ticxo.modelengine.api.model.bone.render;

import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.render.ModelRenderer;
import com.ticxo.modelengine.api.nms.RenderParsers;

public interface BehaviorRenderer {
   ModelRenderer getModelRenderer();

   void setModelRenderer(ModelRenderer var1);

   ActiveModel getActiveModel();

   void initialize();

   void readBoneData();

   void sendToClient(RenderParsers var1);

   void destroy(RenderParsers var1);
}
