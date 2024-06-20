package com.ticxo.modelengine.api.nms;

import com.ticxo.modelengine.api.model.bone.render.BehaviorRenderer;
import com.ticxo.modelengine.api.model.bone.render.BehaviorRendererParser;
import com.ticxo.modelengine.api.model.render.ModelRenderer;
import com.ticxo.modelengine.api.model.render.ModelRendererParser;
import com.ticxo.modelengine.api.nms.entity.EntityHandler;
import com.ticxo.modelengine.api.nms.network.NetworkHandler;
import com.ticxo.modelengine.api.vfx.render.VFXRenderer;
import com.ticxo.modelengine.api.vfx.render.VFXRendererParser;

public interface NMSHandler {
   EntityHandler getEntityHandler();

   NetworkHandler getNetworkHandler();

   RenderParsers getGlobalParsers();

   RenderParsers createParsers();

   <T extends ModelRenderer> ModelRendererParser<T> getModelRendererParser(T var1);

   <T extends BehaviorRenderer> BehaviorRendererParser<T> getBehaviorRendererParser(T var1);

   <T extends VFXRenderer> VFXRendererParser<T> getVFXRendererParser(T var1);
}
