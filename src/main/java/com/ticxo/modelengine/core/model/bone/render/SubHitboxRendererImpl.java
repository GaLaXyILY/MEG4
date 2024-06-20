package com.ticxo.modelengine.core.model.bone.render;

import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.bone.BoneBehaviorTypes;
import com.ticxo.modelengine.api.model.bone.ModelBone;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehavior;
import com.ticxo.modelengine.api.model.bone.render.renderer.SubHitboxRenderer;
import com.ticxo.modelengine.api.nms.RenderParsers;
import com.ticxo.modelengine.api.utils.data.tracker.DataTracker;
import com.ticxo.modelengine.api.utils.data.tracker.UpdateDataTracker;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import org.joml.Vector3f;

public class SubHitboxRendererImpl extends AbstractBehaviorRenderer implements SubHitboxRenderer {
   private final Map<String, SubHitboxRenderer.SubHitbox> spawnQueue = new HashMap();
   private final Map<String, SubHitboxRenderer.SubHitbox> rendered = new HashMap();
   private final Map<String, SubHitboxRenderer.SubHitbox> destroyQueue = new HashMap();
   private boolean initialized;

   public SubHitboxRendererImpl(ActiveModel activeModel) {
      super(activeModel);
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
      Optional maybeData = modelBone.getBoneBehavior(BoneBehaviorTypes.SUB_HITBOX);
      if (!maybeData.isEmpty()) {
         BoneBehavior subHitboxData = (BoneBehavior)maybeData.get();
         if (!((com.ticxo.modelengine.api.model.bone.type.SubHitbox)subHitboxData).isOBB()) {
            SubHitboxRendererImpl.SubHitboxImpl subHitbox = new SubHitboxRendererImpl.SubHitboxImpl(this.nmsHandler.getEntityHandler().getNextEntityId(), ((com.ticxo.modelengine.api.model.bone.type.SubHitbox)subHitboxData).getHitboxId(), UUID.randomUUID(), UUID.randomUUID());
            subHitbox.position.set(((com.ticxo.modelengine.api.model.bone.type.SubHitbox)subHitboxData).getLocation());
            subHitbox.width.set(((com.ticxo.modelengine.api.model.bone.type.SubHitbox)subHitboxData).getDimension().x);
            subHitbox.height.set(((com.ticxo.modelengine.api.model.bone.type.SubHitbox)subHitboxData).getDimension().y);
            this.spawnQueue.put(boneId, subHitbox);
            this.destroyQueue.remove(boneId);
         }
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
            SubHitboxRenderer.SubHitbox renderer = (SubHitboxRenderer.SubHitbox)this.rendered.get(boneId);
            if (renderer != null) {
               this.read(boneId, renderer, modelBone);
            } else {
               this.create(boneId, modelBone);
            }
         }

      }
   }

   private void read(String boneId, SubHitboxRenderer.SubHitbox subHitbox, ModelBone modelBone) {
      Optional maybeData = modelBone.getBoneBehavior(BoneBehaviorTypes.SUB_HITBOX);
      maybeData.ifPresent((subHitboxData) -> {
         subHitbox.getPosition().set(((com.ticxo.modelengine.api.model.bone.type.SubHitbox)subHitboxData).getLocation());
         subHitbox.getWidth().set(((com.ticxo.modelengine.api.model.bone.type.SubHitbox)subHitboxData).getDimension().x);
         subHitbox.getHeight().set(((com.ticxo.modelengine.api.model.bone.type.SubHitbox)subHitboxData).getDimension().y);
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

   public Map<String, SubHitboxRenderer.SubHitbox> getSpawnQueue() {
      return this.spawnQueue;
   }

   public Map<String, SubHitboxRenderer.SubHitbox> getRendered() {
      return this.rendered;
   }

   public Map<String, SubHitboxRenderer.SubHitbox> getDestroyQueue() {
      return this.destroyQueue;
   }

   public static class SubHitboxImpl implements SubHitboxRenderer.SubHitbox {
      private final int pivotId;
      private final int hitboxId;
      private final UUID pivotUuid;
      private final UUID hitboxUuid;
      private final DataTracker<Vector3f> position = new UpdateDataTracker(new Vector3f(), Vector3f::set);
      private final DataTracker<Float> width = new DataTracker();
      private final DataTracker<Float> height = new DataTracker();

      public SubHitboxImpl(int pivotId, int hitboxId, UUID pivotUuid, UUID hitboxUuid) {
         this.pivotId = pivotId;
         this.hitboxId = hitboxId;
         this.pivotUuid = pivotUuid;
         this.hitboxUuid = hitboxUuid;
      }

      public int getPivotId() {
         return this.pivotId;
      }

      public int getHitboxId() {
         return this.hitboxId;
      }

      public UUID getPivotUuid() {
         return this.pivotUuid;
      }

      public UUID getHitboxUuid() {
         return this.hitboxUuid;
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
   }
}
