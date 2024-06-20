package com.ticxo.modelengine.core;

import com.google.gson.GsonBuilder;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.ServerInfo;
import com.ticxo.modelengine.api.animation.AnimationHandlerRegistry;
import com.ticxo.modelengine.api.animation.AnimationPropertyRegistry;
import com.ticxo.modelengine.api.animation.ModelState;
import com.ticxo.modelengine.api.animation.handler.AnimationHandler;
import com.ticxo.modelengine.api.animation.keyframe.KeyframeTypeRegistry;
import com.ticxo.modelengine.api.animation.keyframe.KeyframeTypes;
import com.ticxo.modelengine.api.animation.keyframe.data.KeyframeReaderRegistry;
import com.ticxo.modelengine.api.animation.property.SimpleProperty;
import com.ticxo.modelengine.api.animation.script.ScriptReaderRegistry;
import com.ticxo.modelengine.api.command.AbstractCommand;
import com.ticxo.modelengine.api.entity.BaseEntity;
import com.ticxo.modelengine.api.entity.EntityDataTrackers;
import com.ticxo.modelengine.api.entity.Hitbox;
import com.ticxo.modelengine.api.entity.data.AbstractEntityData;
import com.ticxo.modelengine.api.generator.blueprint.ModelBlueprint;
import com.ticxo.modelengine.api.interaction.InteractionTracker;
import com.ticxo.modelengine.api.menu.ScreenManager;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModelRegistry;
import com.ticxo.modelengine.api.model.ModelUpdaters;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.model.bone.BoneBehaviorTypes;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehaviorRegistry;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehaviorType;
import com.ticxo.modelengine.api.model.bone.behavior.ProceduralType;
import com.ticxo.modelengine.api.model.bone.render.DefaultRenderType;
import com.ticxo.modelengine.api.model.bone.type.PlayerLimb;
import com.ticxo.modelengine.api.model.render.ModelRenderer;
import com.ticxo.modelengine.api.mount.MountControllerTypeRegistry;
import com.ticxo.modelengine.api.mount.MountPairManager;
import com.ticxo.modelengine.api.mount.controller.MountControllerTypes;
import com.ticxo.modelengine.api.utils.CompatibilityManager;
import com.ticxo.modelengine.api.utils.config.ConfigManager;
import com.ticxo.modelengine.api.utils.config.ConfigProperty;
import com.ticxo.modelengine.api.utils.logger.TLogger;
import com.ticxo.modelengine.api.utils.scheduling.BukkitPlatformScheduler;
import com.ticxo.modelengine.api.utils.scheduling.FoliaPlatformScheduler;
import com.ticxo.modelengine.api.utils.scheduling.PlatformScheduler;
import com.ticxo.modelengine.api.utils.ticker.DualTicker;
import com.ticxo.modelengine.api.vfx.VFX;
import com.ticxo.modelengine.api.vfx.VFXUpdater;
import com.ticxo.modelengine.api.vfx.render.VFXRenderer;
import com.ticxo.modelengine.core.animation.handler.PriorityHandler;
import com.ticxo.modelengine.core.animation.handler.StateMachineHandler;
import com.ticxo.modelengine.core.animation.script.ModelEngineScriptReader;
import com.ticxo.modelengine.core.citizens.CitizensCommand;
import com.ticxo.modelengine.core.citizens.ModelTrait;
import com.ticxo.modelengine.core.command.MECommand;
import com.ticxo.modelengine.core.generator.ModelGeneratorImpl;
import com.ticxo.modelengine.core.libsdisguises.DisguiseCompatibility;
import com.ticxo.modelengine.core.listener.ChatListener;
import com.ticxo.modelengine.core.listener.EntityListener;
import com.ticxo.modelengine.core.listener.InventoryListener;
import com.ticxo.modelengine.core.listener.PlayerListener;
import com.ticxo.modelengine.core.listener.WorldListener;
import com.ticxo.modelengine.core.model.ActiveModelImpl;
import com.ticxo.modelengine.core.model.ModeledEntityImpl;
import com.ticxo.modelengine.core.model.bone.behavior.GhostImpl;
import com.ticxo.modelengine.core.model.bone.behavior.HeadForcedImpl;
import com.ticxo.modelengine.core.model.bone.behavior.HeadImpl;
import com.ticxo.modelengine.core.model.bone.behavior.HeldItemImpl;
import com.ticxo.modelengine.core.model.bone.behavior.LeashImpl;
import com.ticxo.modelengine.core.model.bone.behavior.MountImpl;
import com.ticxo.modelengine.core.model.bone.behavior.NameTagImpl;
import com.ticxo.modelengine.core.model.bone.behavior.PlayerLimbImpl;
import com.ticxo.modelengine.core.model.bone.behavior.SegmentImpl;
import com.ticxo.modelengine.core.model.bone.behavior.SubHitboxImpl;
import com.ticxo.modelengine.core.model.bone.manager.LeashManagerImpl;
import com.ticxo.modelengine.core.model.bone.manager.MountManagerImpl;
import com.ticxo.modelengine.core.model.bone.render.HeldItemRendererImpl;
import com.ticxo.modelengine.core.model.bone.render.LeashRendererImpl;
import com.ticxo.modelengine.core.model.bone.render.MountRendererImpl;
import com.ticxo.modelengine.core.model.bone.render.NameTagRendererImpl;
import com.ticxo.modelengine.core.model.bone.render.SegmentRendererImpl;
import com.ticxo.modelengine.core.model.bone.render.SubHitboxRendererImpl;
import com.ticxo.modelengine.core.mythic.compatibility.MythicCompatibility;
import com.ticxo.modelengine.core.vfx.VFXImpl;
import com.ticxo.modelengine.v1_19_R3.NMSHandler_v1_19_R3;
import com.ticxo.modelengine.v1_20_R1.NMSHandler_v1_20_R1;
import com.ticxo.modelengine.v1_20_R2.NMSHandler_v1_20_R2;
import com.ticxo.modelengine.v1_20_R3.NMSHandler_v1_20_R3;
import com.ticxo.modelengine.v1_20_R4.NMSHandler_v1_20_R4;
import com.viaversion.viaversion.api.Via;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.TraitInfo;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.ItemDisplay.ItemDisplayTransform;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.joml.Vector3f;

public class ModelEngine extends ModelEngineAPI {
   public static ModelEngine CORE;
   private AbstractCommand commandRoot;
   private PluginManager pluginManager;
   private MythicCompatibility mythicCompatibility;
   private DisguiseCompatibility disguiseCompatibility;

   public void onLoad() {
      ModelEngineAPI.API = this;
      CORE = this;
      TLogger.logger = this.getLogger();
      this.pluginManager = Bukkit.getPluginManager();
   }

   public void onEnable() {
      this.configureConfig();
      this.configureNMSHandler();
      this.gson = (new GsonBuilder()).create();
      this.scheduler = (PlatformScheduler)(ServerInfo.IS_FOLIA ? new FoliaPlatformScheduler() : new BukkitPlatformScheduler());
      this.modelRegistry = new ModelRegistry();
      this.modelGenerator = new ModelGeneratorImpl(this);
      this.configureKeyframeTypeRegistry();
      this.keyframeReaderRegistry = new KeyframeReaderRegistry();
      this.configureScriptReaderRegistry();
      this.configureBoneBehaviorRegistry();
      this.configureTicker();
      this.configureMountControllerTypeRegistry();
      this.configureAnimationHandlerRegistry();
      this.configureAnimationPropertyRegistry();
      this.configureCommands();
      this.configureCompatibility();
      this.pluginManager.registerEvents(new PlayerListener(), this);
      this.pluginManager.registerEvents(new EntityListener(), this);
      this.pluginManager.registerEvents(new WorldListener(), this);
      this.pluginManager.registerEvents(new InventoryListener(), this);
      this.pluginManager.registerEvents(new ChatListener(), this);
      if (Bukkit.getPluginManager().getPlugin("MythicMobs") == null) {
         this.modelGenerator.importModels(true);
      }

   }

   public void onDisable() {
      this.ticker.stop();
      this.modelUpdaters.saveAllModels();
   }

   private void configureConfig() {
      this.configManager = new ConfigManager(this);
      ConfigProperty[] var1 = ConfigProperty.values();
      int var2 = var1.length;

      int var3;
      for(var3 = 0; var3 < var2; ++var3) {
         ConfigProperty value = var1[var3];
         this.configManager.register(value);
      }

      ModelState[] var5 = ModelState.values();
      var2 = var5.length;

      for(var3 = 0; var3 < var2; ++var3) {
         ModelState value = var5[var3];
         this.configManager.register(value);
      }

      this.configManager.save();
      this.configManager.registerReferenceUpdate(AbstractEntityData::updateConfig);
   }

   private void configureKeyframeTypeRegistry() {
      this.keyframeTypeRegistry = new KeyframeTypeRegistry();
      this.keyframeTypeRegistry.registerKeyframeType(KeyframeTypes.POSITION);
      this.keyframeTypeRegistry.registerKeyframeType(KeyframeTypes.ROTATION);
      this.keyframeTypeRegistry.registerKeyframeType(KeyframeTypes.SCALE);
      this.keyframeTypeRegistry.registerKeyframeType(KeyframeTypes.SCRIPT);
   }

   private void configureScriptReaderRegistry() {
      this.scriptReaderRegistry = new ScriptReaderRegistry();
      this.scriptReaderRegistry.registerAndDefault("meg", new ModelEngineScriptReader());
   }

   private void configureBoneBehaviorRegistry() {
      this.boneBehaviorRegistry = new BoneBehaviorRegistry();
      BoneBehaviorTypes.HEAD = BoneBehaviorType.Builder.of(HeadImpl::new, (BoneBehaviorType.BehaviorManagerProvider)null, "head").optional("local", Boolean.class).optional("inherited", Boolean.class).predicate(BoneBehaviorType::noProcedural).forced(HeadForcedImpl::new).build();
      BoneBehaviorTypes.GHOST = BoneBehaviorType.Builder.of(GhostImpl::new, (BoneBehaviorType.BehaviorManagerProvider)null, "ghost").renderType(DefaultRenderType.NONE).build();
      BoneBehaviorTypes.MOUNT = BoneBehaviorType.Builder.of(MountImpl::new, MountManagerImpl::new, "mount").required("driver", Boolean.class).renderType(MountRendererImpl::new).build();
      BoneBehaviorTypes.SUB_HITBOX = BoneBehaviorType.Builder.of(SubHitboxImpl::new, (BoneBehaviorType.BehaviorManagerProvider)null, "sub_hitbox").required("dimension", Hitbox.class).optional("obb", Boolean.class).optional("origin", Vector3f.class).renderType(SubHitboxRendererImpl::new).ignoreCubes().build();
      BoneBehaviorTypes.NAMETAG = BoneBehaviorType.Builder.of(NameTagImpl::new, (BoneBehaviorType.BehaviorManagerProvider)null, "nametag").renderType(NameTagRendererImpl::new).build();
      BoneBehaviorTypes.ITEM = BoneBehaviorType.Builder.of(HeldItemImpl::new, (BoneBehaviorType.BehaviorManagerProvider)null, "item").required("display", ItemDisplayTransform.class).renderType(HeldItemRendererImpl::new).build();
      BoneBehaviorTypes.SEGMENT = BoneBehaviorType.Builder.of(SegmentImpl::new, (BoneBehaviorType.BehaviorManagerProvider)null, "segment").renderType(SegmentRendererImpl::new).procedural(ProceduralType.ANIMATION, ProceduralType.TRANSFORM).build();
      BoneBehaviorTypes.LEASH = BoneBehaviorType.Builder.of(LeashImpl::new, LeashManagerImpl::new, "leash").optional("main", Boolean.class).renderType(LeashRendererImpl::new).build();
      BoneBehaviorTypes.PLAYER_LIMB = BoneBehaviorType.Builder.of(PlayerLimbImpl::new, (BoneBehaviorType.BehaviorManagerProvider)null, "player_limb").required("limb", PlayerLimb.Limb.class).renderType(DefaultRenderType.NONE).ignoreCubes().build();
      this.boneBehaviorRegistry.register(BoneBehaviorTypes.HEAD);
      this.boneBehaviorRegistry.register(BoneBehaviorTypes.GHOST);
      this.boneBehaviorRegistry.register(BoneBehaviorTypes.MOUNT);
      this.boneBehaviorRegistry.register(BoneBehaviorTypes.SUB_HITBOX);
      this.boneBehaviorRegistry.register(BoneBehaviorTypes.NAMETAG);
      this.boneBehaviorRegistry.register(BoneBehaviorTypes.ITEM);
      this.boneBehaviorRegistry.register(BoneBehaviorTypes.SEGMENT);
      this.boneBehaviorRegistry.register(BoneBehaviorTypes.LEASH);
      this.boneBehaviorRegistry.register(BoneBehaviorTypes.PLAYER_LIMB);
   }

   private void configureAnimationHandlerRegistry() {
      this.animationHandlerRegistry = new AnimationHandlerRegistry();
      this.animationHandlerRegistry.register("priority", PriorityHandler::create);
      this.animationHandlerRegistry.register("state_machine", StateMachineHandler::create);
   }

   private void configureAnimationPropertyRegistry() {
      this.animationPropertyRegistry = new AnimationPropertyRegistry();
      this.animationPropertyRegistry.register("simple", SimpleProperty::create);
   }

   private void configureTicker() {
      this.ticker = new DualTicker(this, this.getScheduler());
      this.mountPairManager = new MountPairManager();
      this.dataTrackers = new EntityDataTrackers(this, this.getScheduler());
      this.modelUpdaters = new ModelUpdaters(this, this.getScheduler());
      this.vfxUpdater = new VFXUpdater();
      this.interactionTracker = new InteractionTracker();
      this.screenManager = new ScreenManager();
      MountPairManager var10000 = this.mountPairManager;
      Objects.requireNonNull(var10000);
      DualTicker.queueRepeatingSyncTask((Runnable)(var10000::updatePassengerPosition), 0, 0);
      VFXUpdater var1 = this.vfxUpdater;
      Objects.requireNonNull(var1);
      DualTicker.queueRepeatingAsyncTask((Runnable)(var1::updateAllVFXs), 0, 0);
      InteractionTracker var2 = this.interactionTracker;
      Objects.requireNonNull(var2);
      DualTicker.queueRepeatingAsyncTask((Runnable)(var2::raytraceHitboxes), 0, 0);
      ScreenManager var3 = this.screenManager;
      Objects.requireNonNull(var3);
      DualTicker.queueRepeatingSyncTask((Runnable)(var3::updateAllScreens), 0, 0);
      this.ticker.start();
   }

   private void configureMountControllerTypeRegistry() {
      this.mountControllerTypeRegistry = new MountControllerTypeRegistry();
      this.mountControllerTypeRegistry.registerAndDefault("walking", MountControllerTypes.WALKING);
      this.mountControllerTypeRegistry.register("force_walking", MountControllerTypes.WALKING_FORCE);
      this.mountControllerTypeRegistry.register("flying", MountControllerTypes.FLYING);
      this.mountControllerTypeRegistry.register("force_flying", MountControllerTypes.FLYING_FORCE);
   }

   private void configureNMSHandler() {
      String var1 = ServerInfo.NMS_VERSION;
      byte var2 = -1;
      switch(var1.hashCode()) {
      case -1496956716:
         if (var1.equals("v1_19_R3")) {
            var2 = 0;
         }
         break;
      case -1496301316:
         if (var1.equals("v1_20_R1")) {
            var2 = 1;
         }
         break;
      case -1496301315:
         if (var1.equals("v1_20_R2")) {
            var2 = 2;
         }
         break;
      case -1496301314:
         if (var1.equals("v1_20_R3")) {
            var2 = 3;
         }
         break;
      case -1496301313:
         if (var1.equals("v1_20_R4")) {
            var2 = 4;
         }
      }

      switch(var2) {
      case 0:
         this.nmsHandler = new NMSHandler_v1_19_R3();
         break;
      case 1:
         this.nmsHandler = new NMSHandler_v1_20_R1();
         break;
      case 2:
         this.nmsHandler = new NMSHandler_v1_20_R2();
         break;
      case 3:
         this.nmsHandler = new NMSHandler_v1_20_R3();
         break;
      case 4:
         this.nmsHandler = new NMSHandler_v1_20_R4();
         break;
      default:
         throw new RuntimeException("Unsupported NMS Version: " + ServerInfo.NMS_VERSION);
      }

   }

   private void configureCommands() {
      PluginCommand command = this.getCommand("meg");
      if (command != null) {
         command.setExecutor(this.commandRoot = new MECommand(this));
      }

   }

   private void configureCompatibility() {
      this.compatibilityManager = new CompatibilityManager(this);
      this.compatibilityManager.registerSupport("MythicMobs", this.mythicCompatibility = new MythicCompatibility(this));
      this.compatibilityManager.registerSupport("Citizens", this::configureCitizensSupport);
      this.compatibilityManager.registerSupport("LibsDisguises", this::configureLibsDisguises);
      this.compatibilityManager.registerSupport("ViaVersion", (plugin) -> {
         ServerInfo.HAS_VIAVERSION = true;
         return true;
      });
   }

   private boolean configureCitizensSupport(Plugin plugin) {
      ServerInfo.HAS_CITIZENS = true;
      CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(ModelTrait.class));
      this.commandRoot.addSubCommands(new CitizensCommand(this.commandRoot));
      return true;
   }

   private boolean configureLibsDisguises(Plugin plugin) {
      this.pluginManager.registerEvents(this.disguiseCompatibility = new DisguiseCompatibility(), this);
      return true;
   }

   public ModeledEntity createModeledEntityImpl(BaseEntity<?> base, Consumer<ModeledEntity> consumer) {
      return new ModeledEntityImpl(base, consumer);
   }

   public ActiveModel createActiveModelImpl(ModelBlueprint blueprint, Function<ActiveModel, ModelRenderer> rendererSupplier, Function<ActiveModel, AnimationHandler> handlerSupplier) {
      return new ActiveModelImpl(blueprint, rendererSupplier, handlerSupplier);
   }

   public VFX createVFXImpl(BaseEntity<?> base, Function<VFX, VFXRenderer> rendererSupplier, Consumer<VFX> consumer) {
      return new VFXImpl(base, rendererSupplier, consumer);
   }

   public UUID getDisguiseRelayOrDefault(UUID uuid) {
      return this.disguiseCompatibility == null ? uuid : this.disguiseCompatibility.getRelayOrDefault(uuid);
   }

   public AnimationHandler getPriorityHandler(ActiveModel activeModel) {
      return new PriorityHandler(activeModel);
   }

   public AnimationHandler getStateMachineHandler(ActiveModel activeModel) {
      return new StateMachineHandler(activeModel);
   }

   public int playerProtocolVersion(UUID uuid) {
      return ServerInfo.HAS_VIAVERSION ? Via.getAPI().getPlayerVersion(uuid) : getNetworkHandler().getProtocolVersion();
   }

   public MythicCompatibility getMythicCompatibility() {
      return this.mythicCompatibility;
   }
}
