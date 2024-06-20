package com.ticxo.modelengine.core.model.bone.render;

import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.bone.BoneBehaviorTypes;
import com.ticxo.modelengine.api.model.bone.ModelBone;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehavior;
import com.ticxo.modelengine.api.model.bone.render.renderer.HeldItemRenderer;
import com.ticxo.modelengine.api.model.bone.type.HeldItem;
import com.ticxo.modelengine.api.nms.RenderParsers;
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
import org.bukkit.entity.ItemDisplay.ItemDisplayTransform;
import org.bukkit.inventory.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class HeldItemRendererImpl extends AbstractBehaviorRenderer implements HeldItemRenderer {
   private final int id;
   private final UUID uuid;
   private final Map<String, HeldItemRenderer.Item> spawnQueue = new HashMap();
   private final Map<String, HeldItemRenderer.Item> rendered = new HashMap();
   private final Map<String, HeldItemRenderer.Item> destroyQueue = new HashMap();
   private final CollectionDataTracker<Integer> passengers = new CollectionDataTracker(new HashSet());
   private boolean initialized;

   public HeldItemRendererImpl(ActiveModel activeModel) {
      super(activeModel);
      this.id = this.nmsHandler.getEntityHandler().getNextEntityId();
      this.uuid = UUID.randomUUID();
   }

   public void initialize() {
      Iterator var1 = this.activeModel.getBones().entrySet().iterator();

      while(var1.hasNext()) {
         Entry<String, ModelBone> boneEntry = (Entry)var1.next();
         String boneId = (String)boneEntry.getKey();
         ModelBone modelBone = (ModelBone)boneEntry.getValue();
         this.create(boneId, modelBone);
      }

      this.initialized = true;
   }

   private void create(String boneId, ModelBone modelBone) {
      Optional maybeData = modelBone.getBoneBehavior(BoneBehaviorTypes.ITEM);
      if (!maybeData.isEmpty()) {
         BoneBehavior itemData = (BoneBehavior)maybeData.get();
         HeldItemRendererImpl.ItemImpl item = new HeldItemRendererImpl.ItemImpl(this.nmsHandler.getEntityHandler().getNextEntityId(), UUID.randomUUID());
         item.position.set(((HeldItem)itemData).getLocation());
         item.scale.set(modelBone.getGlobalScale());
         item.rotation.set(((HeldItem)itemData).getRotation());
         item.model.set(modelBone.isVisible() ? modelBone.getModel() : null);
         item.display.set(((HeldItem)itemData).getDisplay());
         item.glowing.set(modelBone.isGlowing());
         item.glowColor.set(modelBone.getGlowColor());
         this.spawnQueue.put(boneId, item);
         this.destroyQueue.remove(boneId);
         this.passengers.add(item.id);
      }
   }

   public void readBoneData() {
      if (this.initialized) {
         this.destroyQueue.putAll(this.rendered);
         Iterator var1 = this.activeModel.getBones().entrySet().iterator();

         while(var1.hasNext()) {
            Entry<String, ModelBone> boneEntry = (Entry)var1.next();
            String boneId = (String)boneEntry.getKey();
            ModelBone modelBone = (ModelBone)boneEntry.getValue();
            HeldItemRenderer.Item renderer = (HeldItemRenderer.Item)this.rendered.get(boneId);
            if (renderer != null) {
               this.read(boneId, renderer, modelBone);
            } else {
               this.create(boneId, modelBone);
            }
         }

         this.destroyQueue.forEach((s, item) -> {
            this.passengers.remove(item.getId());
         });
      }
   }

   private void read(String boneId, HeldItemRenderer.Item item, ModelBone modelBone) {
      Optional maybeData = modelBone.getBoneBehavior(BoneBehaviorTypes.ITEM);
      maybeData.ifPresent((heldItem) -> {
         item.getPosition().set(((HeldItem)heldItem).getLocation());
         item.getScale().set(modelBone.getGlobalScale());
         item.getRotation().set(((HeldItem)heldItem).getRotation());
         item.getGlowing().set(modelBone.isGlowing());
         item.getGlowColor().set(modelBone.getGlowColor());
         if (!modelBone.isVisible()) {
            item.getModel().set((Object)null);
         } else {
            item.getModel().set(modelBone.getModel());
            if (modelBone.getModelTracker().isDirty()) {
               modelBone.getModelTracker().clearDirty();
               item.getModel().markDirty();
            }
         }

         item.getDisplay().set(((HeldItem)heldItem).getDisplay());
         this.destroyQueue.remove(boneId);
      });
   }

   public void sendToClient(RenderParsers parsers) {
      if (this.initialized) {
         Set var10000 = this.destroyQueue.keySet();
         Map var10001 = this.rendered;
         Objects.requireNonNull(var10001);
         var10000.forEach(var10001::remove);
         parsers.getBehaviorParser(this).sendToClients(this);
         this.rendered.putAll(this.spawnQueue);
         this.spawnQueue.clear();
         this.destroyQueue.clear();
      }
   }

   public void destroy(RenderParsers parsers) {
      if (this.initialized) {
         parsers.getBehaviorParser(this).destroy(this);
      }
   }

   public int getId() {
      return this.id;
   }

   public UUID getUuid() {
      return this.uuid;
   }

   public Map<String, HeldItemRenderer.Item> getSpawnQueue() {
      return this.spawnQueue;
   }

   public Map<String, HeldItemRenderer.Item> getRendered() {
      return this.rendered;
   }

   public Map<String, HeldItemRenderer.Item> getDestroyQueue() {
      return this.destroyQueue;
   }

   public CollectionDataTracker<Integer> getPassengers() {
      return this.passengers;
   }

   public static class ItemImpl implements HeldItemRenderer.Item {
      private final int id;
      private final UUID uuid;
      private final DataTracker<Vector3f> position = new UpdateDataTracker(new Vector3f(), Vector3f::set);
      private final DataTracker<Vector3f> scale = new UpdateDataTracker(new Vector3f(), Vector3f::set);
      private final DataTracker<Quaternionf> rotation = new UpdateDataTracker(new Quaternionf(), Quaternionf::set);
      private final DataTracker<ItemStack> model = new DataTracker();
      private final DataTracker<ItemDisplayTransform> display;
      private final DataTracker<Boolean> glowing;
      private final DataTracker<Integer> glowColor;

      public int getId() {
         return this.id;
      }

      public UUID getUuid() {
         return this.uuid;
      }

      public DataTracker<Vector3f> getPosition() {
         return this.position;
      }

      public DataTracker<Vector3f> getScale() {
         return this.scale;
      }

      public DataTracker<Quaternionf> getRotation() {
         return this.rotation;
      }

      public DataTracker<ItemStack> getModel() {
         return this.model;
      }

      public DataTracker<ItemDisplayTransform> getDisplay() {
         return this.display;
      }

      public DataTracker<Boolean> getGlowing() {
         return this.glowing;
      }

      public DataTracker<Integer> getGlowColor() {
         return this.glowColor;
      }

      public ItemImpl(int id, UUID uuid) {
         this.display = new DataTracker(ItemDisplayTransform.NONE);
         this.glowing = new DataTracker(false);
         this.glowColor = new DataTracker(-1);
         this.id = id;
         this.uuid = uuid;
      }
   }
}
