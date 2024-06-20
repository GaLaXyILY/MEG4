package com.ticxo.modelengine.api.model.bone.render.renderer;

import java.util.Map;

public interface RenderQueues<T> {
   Map<String, T> getSpawnQueue();

   Map<String, T> getRendered();

   Map<String, T> getDestroyQueue();
}
