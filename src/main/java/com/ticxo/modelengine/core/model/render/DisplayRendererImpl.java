package com.ticxo.modelengine.core.model.render;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.ServerInfo;
import com.ticxo.modelengine.api.entity.BaseEntity;
import com.ticxo.modelengine.api.generator.blueprint.ModelBlueprint;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.model.bone.BoneBehaviorTypes;
import com.ticxo.modelengine.api.model.bone.ModelBone;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehavior;
import com.ticxo.modelengine.api.model.bone.manager.BehaviorManager;
import com.ticxo.modelengine.api.model.bone.render.BehaviorRenderer;
import com.ticxo.modelengine.api.model.bone.render.renderer.HeldItemRenderer;
import com.ticxo.modelengine.api.model.bone.type.HeldItem;
import com.ticxo.modelengine.api.model.render.DisplayRenderer;
import com.ticxo.modelengine.api.nms.RenderParsers;
import com.ticxo.modelengine.api.nms.entity.EntityHandler;
import com.ticxo.modelengine.api.utils.data.tracker.CollectionDataTracker;
import com.ticxo.modelengine.api.utils.data.tracker.DataTracker;
import com.ticxo.modelengine.api.utils.data.tracker.UpdateDataTracker;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Consumer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.entity.ItemDisplay.ItemDisplayTransform;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class DisplayRendererImpl implements DisplayRenderer {
   private final ActiveModel activeModel;
   private final EntityHandler entityHandler;
   private final Map<String, DisplayRenderer.Bone> spawnQueue = new HashMap();
   private final Map<String, DisplayRenderer.Bone> rendered = new HashMap();
   private final Map<String, DisplayRenderer.Bone> destroyQueue = new HashMap();
   private final Set<UUID> fullUpdate = new HashSet();
   private DisplayRendererImpl.PivotImpl pivot;
   private DisplayRendererImpl.HitboxImpl hitbox;
   private boolean initialized;
   private boolean firstSpawned;

   public DisplayRendererImpl(ActiveModel activeModel) {
      this.activeModel = activeModel;
      this.entityHandler = ModelEngineAPI.getEntityHandler();
   }

   public void initialize() {
      ModeledEntity model = this.activeModel.getModeledEntity();
      BaseEntity<?> base = model.getBase();
      Location location = base.getLocation();
      ModelBlueprint blueprint = this.activeModel.getBlueprint();
      Vector3f scale = this.activeModel.getScale();
      Vector3f hitboxScale = this.activeModel.getHitboxScale();
      com.ticxo.modelengine.api.entity.Hitbox mainHitbox = blueprint.getMainHitbox();
      float scaledEyeHeight = (float)mainHitbox.getEyeHeight() * scale.y;
      float scaledHeight = (float)mainHitbox.getHeight() * hitboxScale.y;
      float scaledWidth = (float)mainHitbox.getMaxWidth() * hitboxScale.x;
      this.pivot = new DisplayRendererImpl.PivotImpl(this.entityHandler.getNextEntityId());
      this.pivot.updatePosition(location, scaledEyeHeight);
      this.pivot.getYaw().set((180.0F - model.getYBodyRot()) * 0.017453292F);
      Iterator var11 = this.activeModel.getBones().entrySet().iterator();

      while(var11.hasNext()) {
         Entry<String, ModelBone> entry = (Entry)var11.next();
         this.create((String)entry.getKey(), (ModelBone)entry.getValue(), scaledEyeHeight);
      }

      this.forBehaviorRenderer((behaviorRenderer) -> {
         behaviorRenderer.setModelRenderer(this);
         behaviorRenderer.initialize();
      });
      this.hitbox = new DisplayRendererImpl.HitboxImpl(this.entityHandler.getNextEntityId(), this.entityHandler.getNextEntityId(), this.entityHandler.getNextEntityId());
      this.hitbox.updatePosition(location);
      this.hitbox.getHeight().set(scaledHeight);
      this.hitbox.getWidth().set(scaledWidth);
      this.hitbox.getShadowRadius().set(blueprint.getShadowRadius() * scale.x);
      this.hitbox.getHitboxVisible().set(this.activeModel.isHitboxVisible());
      this.hitbox.getShadowVisible().set(this.activeModel.isShadowVisible());
      ModelEngineAPI.getInteractionTracker().setModelRelay(this.hitbox.hitboxId, this.activeModel);
      this.initialized = true;
      this.firstSpawned = true;
   }

   private void create(String boneId, ModelBone modelBone, float eyeHeight) {
      if (modelBone.isRenderer()) {
         DisplayRendererImpl.BoneImpl bone = new DisplayRendererImpl.BoneImpl(this.entityHandler.getNextEntityId());
         bone.getGlowing().set(modelBone.isGlowing());
         bone.getGlowColor().set(modelBone.getGlowColor());
         bone.getBrightness().set(modelBone.getBrightness());
         bone.getVisibility().set(modelBone.isVisible());
         bone.getModel().set(modelBone.getModel());
         bone.getPosition().set(modelBone.getGlobalPosition().add(0.0F, -eyeHeight, 0.0F, new Vector3f()).rotateY((Float)this.pivot.yaw.get()));
         bone.getLeftRotation().set(modelBone.getGlobalLeftRotation().rotateLocalY((Float)this.pivot.yaw.get(), new Quaternionf()));
         bone.getScale().set(modelBone.getGlobalScale());
         bone.getRightRotation().set(modelBone.getGlobalRightRotation());
         this.initializeSpecialBehaviorRender(modelBone, bone);
         this.spawnQueue.put(boneId, bone);
         this.destroyQueue.remove(boneId);
         this.pivot.passengers.add(bone.id);
      }
   }

   private void initializeSpecialBehaviorRender(ModelBone modelBone, DisplayRenderer.Bone bone) {
      modelBone.getBoneBehavior(BoneBehaviorTypes.ITEM).ifPresent((item) -> {
         bone.getDisplay().set(((HeldItem)item).getDisplay());
      });
      modelBone.getBoneBehavior(BoneBehaviorTypes.PLAYER_LIMB).ifPresent((limb) -> {
         bone.getDisplay().set(ItemDisplayTransform.THIRDPERSON_RIGHTHAND);
      });
   }

   public void readModelData() {
      if (this.initialized) {
         ModeledEntity model = this.activeModel.getModeledEntity();
         BaseEntity<?> base = model.getBase();
         Location location = base.getLocation();
         ModelBlueprint blueprint = this.activeModel.getBlueprint();
         Vector3f scale = this.activeModel.getScale();
         Vector3f hitboxScale = this.activeModel.getHitboxScale();
         com.ticxo.modelengine.api.entity.Hitbox mainHitbox = blueprint.getMainHitbox();
         float scaledEyeHeight = (float)mainHitbox.getEyeHeight() * scale.y;
         float scaledHeight = (float)mainHitbox.getHeight() * hitboxScale.y;
         float scaledWidth = (float)mainHitbox.getMaxWidth() * hitboxScale.x;
         this.pivot.updatePosition(location, scaledEyeHeight);
         this.pivot.getYaw().set((180.0F - model.getYBodyRot()) * 0.017453292F);
         this.destroyQueue.putAll(this.rendered);
         Iterator var11 = this.activeModel.getBones().entrySet().iterator();

         while(var11.hasNext()) {
            Entry<String, ModelBone> entry = (Entry)var11.next();
            DisplayRenderer.Bone bone = (DisplayRenderer.Bone)this.rendered.get(entry.getKey());
            if (bone == null) {
               this.create((String)entry.getKey(), (ModelBone)entry.getValue(), scaledEyeHeight);
            } else {
               this.read((String)entry.getKey(), bone, (ModelBone)entry.getValue(), scaledEyeHeight);
            }
         }

         this.destroyQueue.forEach((s, bonex) -> {
            this.pivot.passengers.remove(bonex.getId());
         });
         this.forBehaviorRenderer(BehaviorRenderer::readBoneData);
         this.hitbox.updatePosition(location);
         this.hitbox.getHeight().set(scaledHeight);
         this.hitbox.getWidth().set(scaledWidth);
         this.hitbox.getShadowRadius().set(blueprint.getShadowRadius() * scale.x);
         this.hitbox.getHitboxVisible().set(this.activeModel.isHitboxVisible());
         this.hitbox.getShadowVisible().set(this.activeModel.isShadowVisible());
      }
   }

   private void read(String boneId, DisplayRenderer.Bone bone, ModelBone modelBone, float eyeHeight) {
      DataTracker<Boolean> shouldRender = bone.getRender();
      shouldRender.set(!modelBone.isEffectivelyInvisible());
      if ((Boolean)shouldRender.get() || shouldRender.isDirty()) {
         bone.getStep().set(modelBone.pollModelScaleChanged() || modelBone.shouldStep() || shouldRender.isDirty());
         bone.getPosition().set(modelBone.getGlobalPosition().add(0.0F, -eyeHeight, 0.0F, new Vector3f()).rotateY((Float)this.pivot.getYaw().get()));
         bone.getLeftRotation().set(modelBone.getGlobalLeftRotation().rotateLocalY((Float)this.pivot.getYaw().get(), new Quaternionf()));
         bone.getScale().set(modelBone.getGlobalScale());
         bone.getRightRotation().set(modelBone.getGlobalRightRotation());
         bone.getVisibility().set(modelBone.isVisible());
         bone.getGlowing().set(modelBone.isGlowing());
         bone.getGlowColor().set(modelBone.getGlowColor());
         bone.getBrightness().set(modelBone.getBrightness());
         if (modelBone.getModelTracker().isDirty()) {
            modelBone.getModelTracker().clearDirty();
            bone.getModel().set(modelBone.getModel());
            bone.getModel().markDirty();
         }

         this.updateSpecialBehaviorRender(modelBone, bone);
      }

      shouldRender.clearDirty();
      this.destroyQueue.remove(boneId);
   }

   private void updateSpecialBehaviorRender(ModelBone modelBone, DisplayRenderer.Bone bone) {
      modelBone.getBoneBehavior(BoneBehaviorTypes.ITEM).ifPresent((item) -> {
         bone.getDisplay().set(((HeldItem)item).getDisplay());
      });
   }

   public void sendToClient(RenderParsers parsers) {
      if (this.initialized) {
         this.forManagers(BehaviorManager::preBoneRender);
         this.forBehavior(BoneBehavior::preRender);
         Set var10000 = this.destroyQueue.keySet();
         Map var10001 = this.rendered;
         Objects.requireNonNull(var10001);
         var10000.forEach(var10001::remove);
         parsers.getModelParser(this).sendToClients(this);
         this.rendered.putAll(this.spawnQueue);
         this.spawnQueue.clear();
         this.destroyQueue.clear();
         this.forBehaviorRenderer((behaviorRenderer) -> {
            behaviorRenderer.sendToClient(parsers);
         });
         this.forBehavior(BoneBehavior::onRender);
         this.forBehavior(BoneBehavior::postRender);
         this.forManagers(BehaviorManager::postBoneRender);
      }
   }

   public void destroy(RenderParsers parsers) {
      if (this.initialized) {
         this.forBehaviorRenderer((behaviorRenderer) -> {
            behaviorRenderer.destroy(parsers);
         });
         parsers.getModelParser(this).destroy(this);
         ModelEngineAPI.getInteractionTracker().removeModelRelay(this.hitbox.hitboxId);
      }
   }

   public void createRealEntities() {
      World world = this.activeModel.getModeledEntity().getBase().getLocation().getWorld();
      if (world != null) {
         Vector3f pos = (Vector3f)this.pivot.getPosition().get();
         Location location = new Location(world, (double)pos.x, (double)pos.y, (double)pos.z);
         Iterator var4 = this.rendered.values().iterator();

         while(var4.hasNext()) {
            DisplayRenderer.Bone bone = (DisplayRenderer.Bone)var4.next();
            world.spawn(location, ItemDisplay.class, (itemDisplay) -> {
               Quaternionf rotation = (Quaternionf)bone.getLeftRotation().get();
               if (ServerInfo.VERSION_NUMBER > 19) {
                  rotation = rotation.rotateY(3.1415927F, new Quaternionf());
               }

               itemDisplay.setTransformation(new Transformation((Vector3f)bone.getPosition().get(), rotation, (Vector3f)bone.getScale().get(), new Quaternionf()));
               itemDisplay.setItemStack(((ItemStack)bone.getModel().get()).clone());
               itemDisplay.setItemDisplayTransform((ItemDisplayTransform)bone.getDisplay().get());
            });
         }

      }
   }

   public boolean pollFirstSpawn() {
      if (!this.firstSpawned) {
         return false;
      } else {
         this.firstSpawned = false;
         return true;
      }
   }

   private void forManagers(Consumer<BehaviorManager<?>> consumer) {
      Iterator var2 = this.activeModel.getBehaviorManagers().values().iterator();

      while(var2.hasNext()) {
         BehaviorManager<?> manager = (BehaviorManager)var2.next();
         consumer.accept(manager);
      }

   }

   private void forBehavior(Consumer<BoneBehavior> consumer) {
      Iterator var2 = this.activeModel.getBlueprint().getBones().keySet().iterator();

      while(var2.hasNext()) {
         String boneId = (String)var2.next();
         Optional<ModelBone> maybeBone = this.activeModel.getBone(boneId);
         maybeBone.ifPresent((bone) -> {
            bone.getImmutableBoneBehaviors().values().forEach(consumer);
         });
      }

   }

   private void forBehaviorRenderer(Consumer<BehaviorRenderer> consumer) {
      Iterator var2 = this.activeModel.getBehaviorRenderers().values().iterator();

      while(var2.hasNext()) {
         BehaviorRenderer renderer = (BehaviorRenderer)var2.next();
         if (!(renderer instanceof HeldItemRenderer)) {
            consumer.accept(renderer);
         }
      }

   }

   public int getTick() {
      return this.activeModel.getModeledEntity().getTick();
   }

   public void pushFullUpdate(Player player) {
      this.fullUpdate.add(player.getUniqueId());
   }

   public boolean pollFullUpdate(Player player) {
      return this.fullUpdate.remove(player.getUniqueId());
   }

   public ActiveModel getActiveModel() {
      return this.activeModel;
   }

   public Map<String, DisplayRenderer.Bone> getSpawnQueue() {
      return this.spawnQueue;
   }

   public Map<String, DisplayRenderer.Bone> getRendered() {
      return this.rendered;
   }

   public Map<String, DisplayRenderer.Bone> getDestroyQueue() {
      return this.destroyQueue;
   }

   public DisplayRendererImpl.PivotImpl getPivot() {
      return this.pivot;
   }

   public DisplayRendererImpl.HitboxImpl getHitbox() {
      return this.hitbox;
   }

   public boolean isInitialized() {
      return this.initialized;
   }

   public static class PivotImpl implements DisplayRenderer.Pivot {
      private final int id;
      private final UUID uuid = UUID.randomUUID();
      private final DataTracker<Vector3f> position = new UpdateDataTracker(new Vector3f(), Vector3f::set);
      private final DataTracker<Float> yaw = new DataTracker();
      private final CollectionDataTracker<Integer> passengers = new CollectionDataTracker(new HashSet());

      public void updatePosition(Location location, float eyeHeight) {
         this.position.set((new Vector3f()).set(location.getX(), location.getY() + (double)eyeHeight, location.getZ()));
      }

      public void clearDirty() {
         this.yaw.clearDirty();
         this.passengers.clearDirty();
      }

      public int getId() {
         return this.id;
      }

      public UUID getUuid() {
         return this.uuid;
      }

      public DataTracker<Vector3f> getPosition() {
         return this.position;
      }

      public DataTracker<Float> getYaw() {
         return this.yaw;
      }

      public CollectionDataTracker<Integer> getPassengers() {
         return this.passengers;
      }

      public PivotImpl(int id) {
         this.id = id;
      }
   }

   public static class HitboxImpl implements DisplayRenderer.Hitbox {
      private final int pivotId;
      private final UUID pivotUuid = UUID.randomUUID();
      private final int hitboxId;
      private final UUID hitboxUuid = UUID.randomUUID();
      private final int shadowId;
      private final UUID shadowUuid = UUID.randomUUID();
      private final DataTracker<Vector3f> position = new UpdateDataTracker(new Vector3f(), Vector3f::set);
      private final DataTracker<Float> width = new DataTracker();
      private final DataTracker<Float> height = new DataTracker();
      private final DataTracker<Float> shadowRadius = new DataTracker();
      private final DataTracker<Boolean> hitboxVisible = new DataTracker();
      private final DataTracker<Boolean> shadowVisible = new DataTracker();

      public void updatePosition(Location location) {
         this.position.set((new Vector3f()).set(location.getX(), location.getY(), location.getZ()));
      }

      public void clearDirty() {
         this.width.clearDirty();
         this.height.clearDirty();
         this.shadowRadius.clearDirty();
         this.hitboxVisible.clearDirty();
         this.shadowVisible.clearDirty();
      }

      public int getPivotId() {
         return this.pivotId;
      }

      public UUID getPivotUuid() {
         return this.pivotUuid;
      }

      public int getHitboxId() {
         return this.hitboxId;
      }

      public UUID getHitboxUuid() {
         return this.hitboxUuid;
      }

      public int getShadowId() {
         return this.shadowId;
      }

      public UUID getShadowUuid() {
         return this.shadowUuid;
      }

      public DataTracker<Vector3f> getPosition() {
         return this.position;
      }

      public DataTracker<Float> getWidth() {
         return this.width;
      }

      public DataTracker<Float> getHeight() {
         return this.height;
      }

      public DataTracker<Float> getShadowRadius() {
         return this.shadowRadius;
      }

      public DataTracker<Boolean> getHitboxVisible() {
         return this.hitboxVisible;
      }

      public DataTracker<Boolean> getShadowVisible() {
         return this.shadowVisible;
      }

      public HitboxImpl(int pivotId, int hitboxId, int shadowId) {
         this.pivotId = pivotId;
         this.hitboxId = hitboxId;
         this.shadowId = shadowId;
      }
   }

   public static class BoneImpl implements DisplayRenderer.Bone {
      private final int id;
      private final UUID uuid = UUID.randomUUID();
      private final DataTracker<Boolean> render = new DataTracker(true);
      private final DataTracker<Boolean> step = new DataTracker(false);
      private final DataTracker<Vector3f> position = new UpdateDataTracker(new Vector3f(), Vector3f::set);
      private final DataTracker<Quaternionf> leftRotation = new UpdateDataTracker(new Quaternionf(), Quaternionf::set);
      private final DataTracker<Vector3f> scale = new UpdateDataTracker(new Vector3f(), Vector3f::set);
      private final DataTracker<Quaternionf> rightRotation = new UpdateDataTracker(new Quaternionf(), Quaternionf::set);
      private final DataTracker<ItemStack> model = new DataTracker();
      private final DataTracker<ItemDisplayTransform> display;
      private final DataTracker<Boolean> visibility;
      private final DataTracker<Boolean> glowing;
      private final DataTracker<Integer> glowColor;
      private final DataTracker<Integer> brightness;

      public int getId() {
         return this.id;
      }

      public UUID getUuid() {
         return this.uuid;
      }

      public DataTracker<Boolean> getRender() {
         return this.render;
      }

      public DataTracker<Boolean> getStep() {
         return this.step;
      }

      public DataTracker<Vector3f> getPosition() {
         return this.position;
      }

      public DataTracker<Quaternionf> getLeftRotation() {
         return this.leftRotation;
      }

      public DataTracker<Vector3f> getScale() {
         return this.scale;
      }

      public DataTracker<Quaternionf> getRightRotation() {
         return this.rightRotation;
      }

      public DataTracker<ItemStack> getModel() {
         return this.model;
      }

      public DataTracker<ItemDisplayTransform> getDisplay() {
         return this.display;
      }

      public DataTracker<Boolean> getVisibility() {
         return this.visibility;
      }

      public DataTracker<Boolean> getGlowing() {
         return this.glowing;
      }

      public DataTracker<Integer> getGlowColor() {
         return this.glowColor;
      }

      public DataTracker<Integer> getBrightness() {
         return this.brightness;
      }

      public BoneImpl(int id) {
         this.display = new DataTracker(ItemDisplayTransform.NONE);
         this.visibility = new DataTracker(true);
         this.glowing = new DataTracker(false);
         this.glowColor = new DataTracker(-1);
         this.brightness = new DataTracker(-1);
         this.id = id;
      }
   }
}
