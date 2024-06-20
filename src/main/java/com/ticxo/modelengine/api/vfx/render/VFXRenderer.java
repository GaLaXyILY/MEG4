package com.ticxo.modelengine.api.vfx.render;

import com.ticxo.modelengine.api.vfx.VFX;

public interface VFXRenderer {
   VFX getVFX();

   boolean isInitialized();

   void initialize();

   void readVFXData();

   void sendToClient();

   void destroy();
}
