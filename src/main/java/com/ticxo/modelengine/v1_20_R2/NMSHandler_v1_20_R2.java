package com.ticxo.modelengine.v1_20_R2;

import com.ticxo.modelengine.api.model.bone.render.BehaviorRenderer;
import com.ticxo.modelengine.api.model.bone.render.BehaviorRendererParser;
import com.ticxo.modelengine.api.model.bone.render.renderer.HeldItemRenderer;
import com.ticxo.modelengine.api.model.bone.render.renderer.LeashRenderer;
import com.ticxo.modelengine.api.model.bone.render.renderer.MountRenderer;
import com.ticxo.modelengine.api.model.bone.render.renderer.NameTagRenderer;
import com.ticxo.modelengine.api.model.bone.render.renderer.SubHitboxRenderer;
import com.ticxo.modelengine.api.model.render.DisplayRenderer;
import com.ticxo.modelengine.api.model.render.ModelRenderer;
import com.ticxo.modelengine.api.model.render.ModelRendererParser;
import com.ticxo.modelengine.api.nms.NMSHandler;
import com.ticxo.modelengine.api.nms.RenderParsers;
import com.ticxo.modelengine.api.nms.entity.EntityHandler;
import com.ticxo.modelengine.api.nms.network.NetworkHandler;
import com.ticxo.modelengine.api.vfx.render.VFXDisplayRenderer;
import com.ticxo.modelengine.api.vfx.render.VFXRenderer;
import com.ticxo.modelengine.api.vfx.render.VFXRendererParser;
import com.ticxo.modelengine.v1_20_R2.entity.EntityHandlerImpl;
import com.ticxo.modelengine.v1_20_R2.network.NetworkHandlerImpl;
import com.ticxo.modelengine.v1_20_R2.parser.behavior.HeldItemParser;
import com.ticxo.modelengine.v1_20_R2.parser.behavior.LeashParser;
import com.ticxo.modelengine.v1_20_R2.parser.behavior.MountParser;
import com.ticxo.modelengine.v1_20_R2.parser.behavior.NameTagParser;
import com.ticxo.modelengine.v1_20_R2.parser.behavior.SubHitboxParser;
import com.ticxo.modelengine.v1_20_R2.parser.model.DisplayParser;
import com.ticxo.modelengine.v1_20_R2.parser.vfx.VFXDisplayParser;

public class NMSHandler_v1_20_R2 implements NMSHandler {
   private final EntityHandler entityHandler = new EntityHandlerImpl();
   private final NetworkHandler networkHandler = new NetworkHandlerImpl();
   private final RenderParsers globalParsers = this.createParsers();
   private final VFXDisplayParser vfxDisplayParser = new VFXDisplayParser();

   public RenderParsers createParsers() {
      RenderParsers parsers = new RenderParsers();
      parsers.registerModelParser((renderer) -> {
         return renderer instanceof DisplayRenderer;
      }, new DisplayParser());
      parsers.registerBehaviorParser((renderer) -> {
         return renderer instanceof MountRenderer;
      }, new MountParser());
      parsers.registerBehaviorParser((renderer) -> {
         return renderer instanceof HeldItemRenderer;
      }, new HeldItemParser());
      parsers.registerBehaviorParser((renderer) -> {
         return renderer instanceof LeashRenderer;
      }, new LeashParser());
      parsers.registerBehaviorParser((renderer) -> {
         return renderer instanceof NameTagRenderer;
      }, new NameTagParser());
      parsers.registerBehaviorParser((renderer) -> {
         return renderer instanceof SubHitboxRenderer;
      }, new SubHitboxParser());
      return parsers;
   }

   public <T extends ModelRenderer> ModelRendererParser<T> getModelRendererParser(T renderer) {
      return this.globalParsers.getModelParser(renderer);
   }

   public <T extends BehaviorRenderer> BehaviorRendererParser<T> getBehaviorRendererParser(T renderer) {
      return this.globalParsers.getBehaviorParser(renderer);
   }

   public <T extends VFXRenderer> VFXRendererParser<T> getVFXRendererParser(T renderer) {
      return renderer instanceof VFXDisplayRenderer ? this.vfxDisplayParser : null;
   }

   public EntityHandler getEntityHandler() {
      return this.entityHandler;
   }

   public NetworkHandler getNetworkHandler() {
      return this.networkHandler;
   }

   public RenderParsers getGlobalParsers() {
      return this.globalParsers;
   }
}
