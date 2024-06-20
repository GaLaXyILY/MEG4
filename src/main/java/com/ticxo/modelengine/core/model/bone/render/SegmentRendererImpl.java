package com.ticxo.modelengine.core.model.bone.render;

import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.bone.render.renderer.SegmentRenderer;
import com.ticxo.modelengine.api.nms.RenderParsers;

public class SegmentRendererImpl extends AbstractBehaviorRenderer implements SegmentRenderer {
   public SegmentRendererImpl(ActiveModel activeModel) {
      super(activeModel);
   }

   public void initialize() {
   }

   public void readBoneData() {
   }

   public void sendToClient(RenderParsers parsers) {
   }

   public void destroy(RenderParsers parsers) {
   }
}
