package com.ticxo.modelengine.core.model;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.animation.handler.AnimationHandler;
import com.ticxo.modelengine.api.entity.Hitbox;
import com.ticxo.modelengine.api.entity.data.IEntityData;
import com.ticxo.modelengine.api.generator.blueprint.BlueprintBone;
import com.ticxo.modelengine.api.generator.blueprint.ModelBlueprint;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.model.bone.ModelBone;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehavior;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehaviorType;
import com.ticxo.modelengine.api.model.bone.manager.BehaviorManager;
import com.ticxo.modelengine.api.model.bone.render.BehaviorRenderer;
import com.ticxo.modelengine.api.model.render.ModelRenderer;
import com.ticxo.modelengine.api.nms.entity.EntityHandler;
import com.ticxo.modelengine.api.utils.config.ConfigProperty;
import com.ticxo.modelengine.api.utils.data.io.SavedData;
import com.ticxo.modelengine.api.utils.logger.TLogger;
import com.ticxo.modelengine.core.animation.handler.PriorityHandler;
import com.ticxo.modelengine.core.animation.handler.StateMachineHandler;
import com.ticxo.modelengine.core.model.bone.ModelBoneImpl;
import com.ticxo.modelengine.core.model.render.DisplayRendererImpl;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;
import org.bukkit.Color;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class ActiveModelImpl implements ActiveModel {
   private final ModelBlueprint blueprint;
   private final ModelRenderer modelRenderer;
   private final AnimationHandler animationHandler;
   private final Map<String, ModelBone> bones = Maps.newConcurrentMap();
   private final Map<String, ModelBone> roots = Maps.newConcurrentMap();
   private final Map<BoneBehaviorType<?>, BehaviorManager<?>> behaviorManagers = new LinkedHashMap();
   private final Map<BoneBehaviorType<?>, BehaviorRenderer> behaviorRenderers = new LinkedHashMap();
   private final Vector3f scale = new Vector3f(1.0F, 1.0F, 1.0F);
   private final Vector3f hitboxScale = new Vector3f(1.0F, 1.0F, 1.0F);
   private ModeledEntity modeledEntity;
   private boolean mainHitbox;
   private boolean generated;
   private boolean destroyed;
   private boolean removed;
   private boolean autoRendererInitialization = true;
   private boolean hitboxVisible = true;
   private boolean shadowVisible = true;
   private boolean canHurt = true;
   private Color defaultTint = Color.fromRGB(16777215);
   private Color damageTint = Color.fromRGB(16737894);
   private boolean wasMarkedHurt;
   private Boolean glowing;
   private Integer glowColor;
   private int blockLight = -1;
   private int skyLight = -1;
   private boolean lockPitch;
   private boolean lockYaw;

   public ActiveModelImpl(@NotNull ModelBlueprint blueprint, @Nullable Function<ActiveModel, ModelRenderer> rendererSupplier, @Nullable Function<ActiveModel, AnimationHandler> handlerSupplier) {
      this.blueprint = blueprint;
      ModelRenderer renderer = rendererSupplier == null ? new DisplayRendererImpl(this) : (ModelRenderer)rendererSupplier.apply(this);
      this.modelRenderer = (ModelRenderer)(renderer == null ? new DisplayRendererImpl(this) : renderer);
      AnimationHandler handler = handlerSupplier == null ? createDefaultHandler(this) : (AnimationHandler)handlerSupplier.apply(this);
      this.animationHandler = handler == null ? createDefaultHandler(this) : handler;
   }

   private static AnimationHandler createDefaultHandler(ActiveModel activeModel) {
      return (AnimationHandler)(ConfigProperty.USE_STATE_MACHINE.getBoolean() ? new StateMachineHandler(activeModel) : new PriorityHandler(activeModel));
   }

   public static ActiveModel fromData(SavedData data) {
      try {
         Optional<SavedData> optionalHandler = data.getData("animation_handler");
         return ModelEngineAPI.createActiveModel((String)data.getString("blueprint"), (Function)null, (activeModel) -> {
            return (AnimationHandler)optionalHandler.map((handlerData) -> {
               return ModelEngineAPI.getAnimationHandlerRegistry().createHandler(activeModel, handlerData);
            }).orElse((Object)null);
         });
      } catch (Exception var2) {
         var2.printStackTrace();
         return null;
      }
   }

   public Map<String, ModelBone> getBones() {
      return ImmutableMap.copyOf(this.bones);
   }

   public Map<BoneBehaviorType<?>, BehaviorManager<?>> getBehaviorManagers() {
      synchronized(this.behaviorManagers) {
         return ImmutableMap.copyOf(this.behaviorManagers);
      }
   }

   public Map<BoneBehaviorType<?>, BehaviorRenderer> getBehaviorRenderers() {
      synchronized(this.behaviorRenderers) {
         return ImmutableMap.copyOf(this.behaviorRenderers);
      }
   }

   public void setScale(double scale) {
      this.getScale().set(scale);
      if (this.mainHitbox && this.modeledEntity != null) {
         Vector3f scaleVec = this.getScale();
         Hitbox mainHitbox = this.blueprint.getMainHitbox();
         Hitbox scaledRenderHitbox = new Hitbox(mainHitbox.getWidth() * (double)scaleVec.x, mainHitbox.getHeight() * (double)scaleVec.y, mainHitbox.getDepth() * (double)scaleVec.z, mainHitbox.getEyeHeight() * (double)scaleVec.y);
         this.modeledEntity.getBase().getData().setCullHitbox(scaledRenderHitbox);
      }
   }

   public void setHitboxScale(double scale) {
      this.getHitboxScale().set(scale);
      if (this.mainHitbox && this.modeledEntity != null) {
         Object var4 = this.modeledEntity.getBase().getOriginal();
         if (var4 instanceof Entity) {
            Entity entity = (Entity)var4;
            EntityHandler entityHandler = ModelEngineAPI.getEntityHandler();
            Vector3f hitboxScale = this.getHitboxScale();
            Hitbox mainHitbox = this.blueprint.getMainHitbox();
            Hitbox scaledHitbox = new Hitbox(mainHitbox.getWidth() * (double)hitboxScale.x, mainHitbox.getHeight() * (double)hitboxScale.y, mainHitbox.getDepth() * (double)hitboxScale.z, mainHitbox.getEyeHeight() * (double)hitboxScale.y);
            entityHandler.setHitbox(entity, scaledHitbox);
         }

      }
   }

   public void tick() {
      if (!this.isDestroyed()) {
         this.animationHandler.prepare();
         this.forManagers(BehaviorManager::preBoneTick);
         this.forBones(ModelBone::tick);
         this.forManagers(BehaviorManager::postBoneTick);
         this.forManagers(BehaviorManager::preScriptTick);
         this.animationHandler.tickGlobal();
         this.forManagers(BehaviorManager::postScriptTick);
         this.modelRenderer.readModelData();
         this.wasMarkedHurt = this.isMarkedHurt();
      }
   }

   public void destroy() {
      this.forBones(ModelBone::destroy);
      this.forManagers(BehaviorManager::onDestroy);
      this.bones.clear();
      this.getData().markModelGlowing(this, false);
      this.modelRenderer.destroy(ModelEngineAPI.getNMSHandler().getGlobalParsers());
      this.destroyed = true;
   }

   public void initializeRenderer() {
      if (!this.modelRenderer.isInitialized()) {
         this.modelRenderer.initialize();
      }

   }

   public void generateModel() {
      if (!this.generated) {
         this.generated = true;
         Iterator var1 = this.blueprint.getFlatMap().entrySet().iterator();

         while(var1.hasNext()) {
            Entry<String, BlueprintBone> entry = (Entry)var1.next();
            BlueprintBone blueprint = (BlueprintBone)entry.getValue();
            ModelBone parent = blueprint.getParent() == null ? null : (ModelBone)this.bones.get(blueprint.getParent().getName());
            ModelBoneImpl bone = new ModelBoneImpl(this, blueprint);
            if (parent != null) {
               bone.setParent(parent);
            } else {
               this.roots.put(bone.getUniqueBoneId(), bone);
            }

            Iterator var6 = blueprint.getCachedBehaviorProvider().entrySet().iterator();

            while(var6.hasNext()) {
               Entry<BoneBehaviorType<?>, BoneBehaviorType.CachedProvider<?>> behaviorEntry = (Entry)var6.next();
               BoneBehaviorType<?> type = (BoneBehaviorType)behaviorEntry.getKey();
               BoneBehaviorType.CachedProvider<?> provider = (BoneBehaviorType.CachedProvider)behaviorEntry.getValue();
               this.getBehaviorManager(type);
               this.getBehaviorRenderer(type);
               bone.addBoneBehavior(provider.create(bone));
            }

            this.bones.put(bone.getUniqueBoneId(), bone);
         }

         if (this.autoRendererInitialization) {
            this.modelRenderer.initialize();
         }

      }
   }

   public void forceGenerateBone(String parentId, String prefix, final BlueprintBone blueprintBone) {
      ModelBone parentBone = parentId == null ? null : (ModelBone)this.getBone(parentId).orElse((Object)null);
      HashMap<String, ModelBone> map = new HashMap();
      LinkedList queue = new LinkedList<BlueprintBone>() {
         {
            this.add(blueprintBone);
         }
      };

      while(true) {
         while(!queue.isEmpty()) {
            BlueprintBone blueprint = (BlueprintBone)queue.pop();
            queue.addAll(blueprint.getChildren().values());
            String name = blueprint.getName();
            String customId = prefix + name;
            if (this.bones.containsKey(customId)) {
               TLogger.error("Unable to force generate custom bone: ID " + customId + " already exists.");
            } else {
               ModelBone parent = blueprint.getParent() == null ? null : (ModelBone)map.get(blueprint.getParent().getName());
               ModelBoneImpl bone = new ModelBoneImpl(this, blueprint);
               bone.setCustomId(customId);
               if (parent != null) {
                  bone.setParent(parent);
               } else if (parentBone != null) {
                  bone.setParent(parentBone);
               } else {
                  this.roots.put(bone.getUniqueBoneId(), bone);
               }

               Iterator var12 = blueprint.getCachedBehaviorProvider().entrySet().iterator();

               while(var12.hasNext()) {
                  Entry<BoneBehaviorType<?>, BoneBehaviorType.CachedProvider<?>> behaviorEntry = (Entry)var12.next();
                  BoneBehaviorType<?> type = (BoneBehaviorType)behaviorEntry.getKey();
                  BoneBehaviorType.CachedProvider<?> provider = (BoneBehaviorType.CachedProvider)behaviorEntry.getValue();
                  this.getBehaviorManager(type);
                  this.getBehaviorRenderer(type);
                  bone.addBoneBehavior(provider.create(bone));
               }

               map.put(name, bone);
               this.bones.put(bone.getUniqueBoneId(), bone);
            }
         }

         return;
      }
   }

   public void removeBone(String bone) {
      ModelBone removed = (ModelBone)this.bones.remove(bone);
      if (removed != null) {
         this.roots.remove(bone);
      }
   }

   public boolean canHurt() {
      return this.canHurt;
   }

   public boolean wasMarkedHurt() {
      return this.wasMarkedHurt;
   }

   public boolean isMarkedHurt() {
      return this.canHurt && this.modeledEntity != null && this.modeledEntity.getHurtTick() > 0;
   }

   public boolean isGlowing() {
      return this.glowing == null ? this.modeledEntity.isGlowing() : this.glowing;
   }

   public void setGlowing(@Nullable Boolean flag) {
      this.glowing = flag;
      this.getData().markModelGlowing(this, this.glowing != null && this.glowing);
   }

   public int getGlowColor() {
      return this.glowColor == null ? this.modeledEntity.getGlowColor() : this.glowColor;
   }

   public float getXHeadRot() {
      return this.lockPitch ? 0.0F : this.modeledEntity.getXHeadRot();
   }

   public float getYHeadRot() {
      return this.lockYaw ? this.modeledEntity.getYBodyRot() : this.modeledEntity.getYHeadRot();
   }

   public <T extends BoneBehavior> Optional<BehaviorManager<T>> getBehaviorManager(BoneBehaviorType<T> type) {
      BoneBehaviorType.BehaviorManagerProvider<T> provider = type.getBehaviorManagerProvider();
      if (provider == null) {
         return Optional.empty();
      } else {
         synchronized(this.behaviorManagers) {
            return Optional.ofNullable((BehaviorManager)this.behaviorManagers.computeIfAbsent(type, (boneBehaviorType) -> {
               BehaviorManager<T> manager = provider.create(this, type);
               if (manager == null) {
                  return null;
               } else {
                  manager.onCreate();
                  return manager;
               }
            }));
         }
      }
   }

   public Optional<BehaviorRenderer> getBehaviorRenderer(BoneBehaviorType<?> type) {
      synchronized(this.behaviorRenderers) {
         BehaviorRenderer renderer = (BehaviorRenderer)this.behaviorRenderers.get(type);
         if (renderer != null) {
            return Optional.of(renderer);
         } else {
            renderer = type.getRenderType().createBehaviorRenderer(this);
            if (renderer != null) {
               this.behaviorRenderers.put(type, renderer);
            }

            return Optional.ofNullable(renderer);
         }
      }
   }

   private void forBones(Consumer<ModelBone> consumer) {
      Iterator var2 = this.bones.values().iterator();

      while(var2.hasNext()) {
         ModelBone bone = (ModelBone)var2.next();
         if (bone.getParent() == null) {
            consumer.accept(bone);
         }
      }

   }

   private void forManagers(Consumer<BehaviorManager<?>> consumer) {
      synchronized(this.behaviorManagers) {
         Iterator var3 = this.behaviorManagers.values().iterator();

         while(var3.hasNext()) {
            BehaviorManager<?> manager = (BehaviorManager)var3.next();
            consumer.accept(manager);
         }

      }
   }

   public void save(SavedData data) {
      data.putString("blueprint", this.blueprint.getName());
      data.putFloat("render_scale", this.getScale().x);
      data.putFloat("hitbox_scale", this.getHitboxScale().x);
      data.putBoolean("can_hurt", this.canHurt());
      data.putInt("default_tint", this.defaultTint.asRGB());
      data.putInt("damage_tint", this.damageTint.asRGB());
      data.putBoolean("lock_pitch", this.lockPitch);
      data.putBoolean("lock_yaw", this.lockYaw);
      data.putBoolean("hitbox_visible", this.hitboxVisible);
      data.putBoolean("shadow_visible", this.shadowVisible);
      data.putBoolean("main_hitbox", this.mainHitbox);
      data.putBoolean("glowing", this.glowing);
      data.putInt("glow_color", this.glowColor);
      data.putInt("block_light", this.blockLight);
      data.putInt("sky_light", this.skyLight);
      HashSet<String> removed = new HashSet();
      SavedData boneDataMap = new SavedData();
      Iterator var4 = this.blueprint.getFlatMap().keySet().iterator();

      while(var4.hasNext()) {
         String boneId = (String)var4.next();
         ModelBone bone = (ModelBone)this.bones.get(boneId);
         if (bone == null) {
            removed.add(boneId);
         } else {
            bone.save().ifPresent((boneData) -> {
               boneDataMap.putData(boneId, boneData);
            });
         }
      }

      data.putList("removed", removed);
      data.putData("default_bones", boneDataMap);
      this.animationHandler.save().ifPresent((animationData) -> {
         data.putData("animation_handler", animationData);
      });
   }

   public void load(SavedData data) {
      this.setScale((double)data.getFloat("render_scale"));
      this.setHitboxScale((double)data.getFloat("hitbox_scale"));
      this.setCanHurt(data.getBoolean("can_hurt"));
      this.setDefaultTint(Color.fromRGB(data.getInt("default_tint")));
      this.setDamageTint(Color.fromRGB(data.getInt("damage_tint")));
      this.setLockPitch(data.getBoolean("lock_pitch"));
      this.setLockYaw(data.getBoolean("lock_yaw"));
      this.setHitboxVisible(data.getBoolean("hitbox_visible"));
      this.setShadowVisible(data.getBoolean("shadow_visible"));
      this.setMainHitbox(data.getBoolean("main_hitbox"));
      this.setGlowing(data.getBoolean("glowing"));
      this.setGlowColor(data.getInt("glow_color"));
      this.setBlockLight(data.getInt("block_light", -1));
      this.setSkyLight(data.getInt("sky_light", -1));
      Iterator var2 = data.getList("removed").iterator();

      while(var2.hasNext()) {
         String remove = (String)var2.next();
         this.removeBone(remove);
      }

      data.getData("default_bones").ifPresent((boneDataMap) -> {
         Iterator var2 = boneDataMap.keySet().iterator();

         while(var2.hasNext()) {
            String key = (String)var2.next();
            this.getBone(key).ifPresent((modelBone) -> {
               Optional var10000 = boneDataMap.getData(key);
               Objects.requireNonNull(modelBone);
               var10000.ifPresent(modelBone::load);
            });
         }

      });
   }

   private IEntityData getData() {
      return this.modeledEntity.getBase().getData();
   }

   public ModelBlueprint getBlueprint() {
      return this.blueprint;
   }

   public ModelRenderer getModelRenderer() {
      return this.modelRenderer;
   }

   public AnimationHandler getAnimationHandler() {
      return this.animationHandler;
   }

   public Vector3f getScale() {
      return this.scale;
   }

   public Vector3f getHitboxScale() {
      return this.hitboxScale;
   }

   public ModeledEntity getModeledEntity() {
      return this.modeledEntity;
   }

   public boolean isMainHitbox() {
      return this.mainHitbox;
   }

   public boolean isGenerated() {
      return this.generated;
   }

   public boolean isDestroyed() {
      return this.destroyed;
   }

   public boolean isRemoved() {
      return this.removed;
   }

   public boolean isAutoRendererInitialization() {
      return this.autoRendererInitialization;
   }

   public boolean isHitboxVisible() {
      return this.hitboxVisible;
   }

   public boolean isShadowVisible() {
      return this.shadowVisible;
   }

   public boolean isCanHurt() {
      return this.canHurt;
   }

   public Color getDefaultTint() {
      return this.defaultTint;
   }

   public Color getDamageTint() {
      return this.damageTint;
   }

   public boolean isWasMarkedHurt() {
      return this.wasMarkedHurt;
   }

   public int getBlockLight() {
      return this.blockLight;
   }

   public int getSkyLight() {
      return this.skyLight;
   }

   public boolean isLockPitch() {
      return this.lockPitch;
   }

   public boolean isLockYaw() {
      return this.lockYaw;
   }

   public void setModeledEntity(ModeledEntity modeledEntity) {
      this.modeledEntity = modeledEntity;
   }

   public void setMainHitbox(boolean mainHitbox) {
      this.mainHitbox = mainHitbox;
   }

   public void setRemoved(boolean removed) {
      this.removed = removed;
   }

   public void setAutoRendererInitialization(boolean autoRendererInitialization) {
      this.autoRendererInitialization = autoRendererInitialization;
   }

   public void setHitboxVisible(boolean hitboxVisible) {
      this.hitboxVisible = hitboxVisible;
   }

   public void setShadowVisible(boolean shadowVisible) {
      this.shadowVisible = shadowVisible;
   }

   public void setCanHurt(boolean canHurt) {
      this.canHurt = canHurt;
   }

   public void setDefaultTint(Color defaultTint) {
      this.defaultTint = defaultTint;
   }

   public void setDamageTint(Color damageTint) {
      this.damageTint = damageTint;
   }

   public void setGlowColor(Integer glowColor) {
      this.glowColor = glowColor;
   }

   public void setBlockLight(int blockLight) {
      this.blockLight = blockLight;
   }

   public void setSkyLight(int skyLight) {
      this.skyLight = skyLight;
   }

   public void setLockPitch(boolean lockPitch) {
      this.lockPitch = lockPitch;
   }

   public void setLockYaw(boolean lockYaw) {
      this.lockYaw = lockYaw;
   }
}
