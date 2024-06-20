package com.ticxo.modelengine.api;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.ticxo.modelengine.api.animation.AnimationHandlerRegistry;
import com.ticxo.modelengine.api.animation.AnimationPropertyRegistry;
import com.ticxo.modelengine.api.animation.handler.AnimationHandler;
import com.ticxo.modelengine.api.animation.keyframe.KeyframeTypeRegistry;
import com.ticxo.modelengine.api.animation.keyframe.data.KeyframeReaderRegistry;
import com.ticxo.modelengine.api.animation.script.ScriptReaderRegistry;
import com.ticxo.modelengine.api.entity.BaseEntity;
import com.ticxo.modelengine.api.entity.BukkitEntity;
import com.ticxo.modelengine.api.entity.BukkitPlayer;
import com.ticxo.modelengine.api.entity.EntityDataTrackers;
import com.ticxo.modelengine.api.generator.ModelGenerator;
import com.ticxo.modelengine.api.generator.blueprint.ModelBlueprint;
import com.ticxo.modelengine.api.interaction.InteractionTracker;
import com.ticxo.modelengine.api.menu.ScreenManager;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModelRegistry;
import com.ticxo.modelengine.api.model.ModelUpdaters;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehaviorRegistry;
import com.ticxo.modelengine.api.model.render.ModelRenderer;
import com.ticxo.modelengine.api.mount.MountControllerTypeRegistry;
import com.ticxo.modelengine.api.mount.MountPairManager;
import com.ticxo.modelengine.api.nms.NMSHandler;
import com.ticxo.modelengine.api.nms.entity.EntityHandler;
import com.ticxo.modelengine.api.nms.network.NetworkHandler;
import com.ticxo.modelengine.api.utils.CompatibilityManager;
import com.ticxo.modelengine.api.utils.config.ConfigManager;
import com.ticxo.modelengine.api.utils.scheduling.PlatformScheduler;
import com.ticxo.modelengine.api.utils.ticker.DualTicker;
import com.ticxo.modelengine.api.vfx.VFX;
import com.ticxo.modelengine.api.vfx.VFXUpdater;
import com.ticxo.modelengine.api.vfx.render.VFXRenderer;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class ModelEngineAPI extends JavaPlugin {
   protected static ModelEngineAPI API;
   protected final Set<Integer> renderCanceled = Sets.newConcurrentHashSet();
   protected ConfigManager configManager;
   protected Gson gson;
   protected PlatformScheduler scheduler;
   protected ModelRegistry modelRegistry;
   protected ModelGenerator modelGenerator;
   protected KeyframeTypeRegistry keyframeTypeRegistry;
   protected KeyframeReaderRegistry keyframeReaderRegistry;
   protected ScriptReaderRegistry scriptReaderRegistry;
   protected BoneBehaviorRegistry boneBehaviorRegistry;
   protected AnimationHandlerRegistry animationHandlerRegistry;
   protected AnimationPropertyRegistry animationPropertyRegistry;
   protected DualTicker ticker;
   protected ModelUpdaters modelUpdaters;
   protected VFXUpdater vfxUpdater;
   protected EntityDataTrackers dataTrackers;
   protected InteractionTracker interactionTracker;
   protected MountPairManager mountPairManager;
   protected MountControllerTypeRegistry mountControllerTypeRegistry;
   protected NMSHandler nmsHandler;
   protected CompatibilityManager compatibilityManager;
   protected ScreenManager screenManager;

   public static NMSHandler getNMSHandler() {
      return getAPI().nmsHandler;
   }

   public static EntityHandler getEntityHandler() {
      return getNMSHandler().getEntityHandler();
   }

   public static NetworkHandler getNetworkHandler() {
      return getNMSHandler().getNetworkHandler();
   }

   public static InteractionTracker getInteractionTracker() {
      return getAPI().interactionTracker;
   }

   public static MountPairManager getMountPairManager() {
      return getAPI().mountPairManager;
   }

   public static MountControllerTypeRegistry getMountControllerTypeRegistry() {
      return getAPI().mountControllerTypeRegistry;
   }

   public static AnimationHandlerRegistry getAnimationHandlerRegistry() {
      return getAPI().animationHandlerRegistry;
   }

   public static AnimationPropertyRegistry getAnimationPropertyRegistry() {
      return getAPI().animationPropertyRegistry;
   }

   public VFXUpdater getVFXUpdater() {
      return this.vfxUpdater;
   }

   public static ModeledEntity createModeledEntity(Entity base) {
      return createModeledEntity((Entity)base, (Consumer)null);
   }

   public static ModeledEntity createModeledEntity(Entity base, Consumer<ModeledEntity> consumer) {
      Object var10000;
      if (base instanceof Player) {
         Player player = (Player)base;
         var10000 = new BukkitPlayer(player);
      } else {
         var10000 = new BukkitEntity(base);
      }

      return createModeledEntity((BaseEntity)var10000, consumer);
   }

   public static ModeledEntity createModeledEntity(BaseEntity<?> base) {
      return createModeledEntity((BaseEntity)base, (Consumer)null);
   }

   public static ModeledEntity createModeledEntity(BaseEntity<?> base, Consumer<ModeledEntity> consumer) {
      return getAPI().createModeledEntityImpl(base, consumer);
   }

   public static ModeledEntity getModeledEntity(Entity entity) {
      return getModeledEntity(entity.getUniqueId());
   }

   public static ModeledEntity getModeledEntity(int id) {
      return getAPI().getModelUpdaters().getModeledEntity(id);
   }

   public static ModeledEntity getModeledEntity(UUID uuid) {
      return getAPI().getModelUpdaters().getModeledEntity(uuid);
   }

   public static ModeledEntity getOrCreateModeledEntity(Entity base) {
      ModeledEntity model = getModeledEntity(base);
      return model != null ? model : createModeledEntity((Entity)base, (Consumer)null);
   }

   public static ModeledEntity getOrCreateModeledEntity(Entity base, Consumer<ModeledEntity> consumer) {
      ModeledEntity model = getModeledEntity(base);
      ModeledEntity var10000;
      if (model != null) {
         var10000 = model;
      } else {
         Object var4;
         if (base instanceof Player) {
            Player player = (Player)base;
            var4 = new BukkitPlayer(player);
         } else {
            var4 = new BukkitEntity(base);
         }

         var10000 = createModeledEntity((BaseEntity)var4, consumer);
      }

      return var10000;
   }

   public static ModeledEntity getOrCreateModeledEntity(UUID uuid, Supplier<BaseEntity<?>> baseEntitySupplier) {
      ModeledEntity model = getModeledEntity(uuid);
      return model != null ? model : createModeledEntity((BaseEntity)((BaseEntity)baseEntitySupplier.get()), (Consumer)null);
   }

   public static ModeledEntity getOrCreateModeledEntity(UUID uuid, Supplier<BaseEntity<?>> baseEntitySupplier, Consumer<ModeledEntity> consumer) {
      ModeledEntity model = getModeledEntity(uuid);
      return model != null ? model : getAPI().createModeledEntityImpl((BaseEntity)baseEntitySupplier.get(), consumer);
   }

   public static ModeledEntity removeModeledEntity(Entity entity) {
      return removeModeledEntity(entity.getUniqueId());
   }

   public static ModeledEntity removeModeledEntity(int id) {
      return getAPI().getModelUpdaters().removeModeledEntity(id);
   }

   public static ModeledEntity removeModeledEntity(UUID uuid) {
      return getAPI().getModelUpdaters().removeModeledEntity(uuid);
   }

   public static boolean isModeledEntity(UUID uuid) {
      return getModeledEntity(uuid) != null;
   }

   public static ActiveModel createActiveModel(String modelId) {
      return createActiveModel((String)modelId, (Function)null, (Function)null);
   }

   public static ActiveModel createActiveModel(String modelId, Function<ActiveModel, ModelRenderer> rendererSupplier, Function<ActiveModel, AnimationHandler> handlerSupplier) {
      ModelBlueprint blueprint = (ModelBlueprint)getAPI().getModelRegistry().get(modelId);
      if (blueprint == null) {
         throw new RuntimeException();
      } else {
         return createActiveModel(blueprint, rendererSupplier, handlerSupplier);
      }
   }

   public static ActiveModel createActiveModel(ModelBlueprint blueprint) {
      return createActiveModel((ModelBlueprint)blueprint, (Function)null, (Function)null);
   }

   public static ActiveModel createActiveModel(ModelBlueprint blueprint, Function<ActiveModel, ModelRenderer> rendererSupplier, Function<ActiveModel, AnimationHandler> handlerSupplier) {
      return getAPI().createActiveModelImpl(blueprint, rendererSupplier, handlerSupplier);
   }

   public static AnimationHandler createPriorityHandler(ActiveModel activeModel) {
      return getAPI().getPriorityHandler(activeModel);
   }

   public static AnimationHandler createStateMachineHandler(ActiveModel activeModel) {
      return getAPI().getStateMachineHandler(activeModel);
   }

   public static VFX createVFX(Entity base) {
      return createVFX((Entity)base, (Consumer)null, (Function)null);
   }

   public static VFX createVFX(Entity base, Consumer<VFX> consumer) {
      return createVFX((Entity)base, consumer, (Function)null);
   }

   public static VFX createVFX(Entity base, Consumer<VFX> consumer, Function<VFX, VFXRenderer> rendererSupplier) {
      Object var10000;
      if (base instanceof Player) {
         Player player = (Player)base;
         var10000 = new BukkitPlayer(player);
      } else {
         var10000 = new BukkitEntity(base);
      }

      return createVFX((BaseEntity)var10000, consumer, rendererSupplier);
   }

   public static VFX createVFX(BaseEntity<?> base) {
      return createVFX((BaseEntity)base, (Consumer)null, (Function)null);
   }

   public static VFX createVFX(BaseEntity<?> base, Consumer<VFX> consumer) {
      return createVFX((BaseEntity)base, consumer, (Function)null);
   }

   public static VFX createVFX(BaseEntity<?> base, Consumer<VFX> consumer, Function<VFX, VFXRenderer> rendererSupplier) {
      return getAPI().createVFXImpl(base, rendererSupplier, consumer);
   }

   public static VFX getVFX(Entity entity) {
      return getVFX(entity.getUniqueId());
   }

   public static VFX getVFX(int id) {
      return getAPI().getVFXUpdater().getVFX(id);
   }

   public static VFX getVFX(UUID uuid) {
      return getAPI().getVFXUpdater().getVFX(uuid);
   }

   public static ModelBlueprint getBlueprint(String id) {
      return (ModelBlueprint)getAPI().getModelRegistry().get(id);
   }

   public static void setRenderCanceled(int id, boolean flag) {
      if (flag) {
         getAPI().renderCanceled.add(id);
      } else {
         getAPI().renderCanceled.remove(id);
      }

   }

   public static boolean isRenderCanceled(int id) {
      return getAPI().renderCanceled.contains(id);
   }

   public static void callEvent(Event event) {
      Bukkit.getPluginManager().callEvent(event);
   }

   public static int getPlayerProtocolVersion(UUID uuid) {
      return getAPI().playerProtocolVersion(uuid);
   }

   public abstract ModeledEntity createModeledEntityImpl(BaseEntity<?> var1, Consumer<ModeledEntity> var2);

   public abstract ActiveModel createActiveModelImpl(ModelBlueprint var1, Function<ActiveModel, ModelRenderer> var2, Function<ActiveModel, AnimationHandler> var3);

   public abstract VFX createVFXImpl(BaseEntity<?> var1, Function<VFX, VFXRenderer> var2, Consumer<VFX> var3);

   public abstract UUID getDisguiseRelayOrDefault(UUID var1);

   public abstract AnimationHandler getPriorityHandler(ActiveModel var1);

   public abstract AnimationHandler getStateMachineHandler(ActiveModel var1);

   public abstract int playerProtocolVersion(UUID var1);

   public Set<Integer> getRenderCanceled() {
      return this.renderCanceled;
   }

   public ConfigManager getConfigManager() {
      return this.configManager;
   }

   public Gson getGson() {
      return this.gson;
   }

   public PlatformScheduler getScheduler() {
      return this.scheduler;
   }

   public ModelRegistry getModelRegistry() {
      return this.modelRegistry;
   }

   public ModelGenerator getModelGenerator() {
      return this.modelGenerator;
   }

   public KeyframeTypeRegistry getKeyframeTypeRegistry() {
      return this.keyframeTypeRegistry;
   }

   public KeyframeReaderRegistry getKeyframeReaderRegistry() {
      return this.keyframeReaderRegistry;
   }

   public ScriptReaderRegistry getScriptReaderRegistry() {
      return this.scriptReaderRegistry;
   }

   public BoneBehaviorRegistry getBoneBehaviorRegistry() {
      return this.boneBehaviorRegistry;
   }

   public DualTicker getTicker() {
      return this.ticker;
   }

   public ModelUpdaters getModelUpdaters() {
      return this.modelUpdaters;
   }

   public EntityDataTrackers getDataTrackers() {
      return this.dataTrackers;
   }

   public CompatibilityManager getCompatibilityManager() {
      return this.compatibilityManager;
   }

   public ScreenManager getScreenManager() {
      return this.screenManager;
   }

   public static ModelEngineAPI getAPI() {
      return API;
   }
}
