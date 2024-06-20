package com.ticxo.modelengine.core.model.bone.render;

import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.bone.BoneBehaviorTypes;
import com.ticxo.modelengine.api.model.bone.ModelBone;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehavior;
import com.ticxo.modelengine.api.model.bone.render.renderer.LeashRenderer;
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
import org.bukkit.entity.Entity;
import org.joml.Vector3f;

public class LeashRendererImpl extends AbstractBehaviorRenderer implements LeashRenderer {
   private final Map<String, LeashRenderer.Leash> spawnQueue = new HashMap();
   private final Map<String, LeashRenderer.Leash> rendered = new HashMap();
   private final Map<String, LeashRenderer.Leash> destroyQueue = new HashMap();
   private boolean initialized;

   public LeashRendererImpl(ActiveModel activeModel) {
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
      Optional maybeData = modelBone.getBoneBehavior(BoneBehaviorTypes.LEASH);
      if (!maybeData.isEmpty()) {
         BoneBehavior leashData = (BoneBehavior)maybeData.get();
         LeashRendererImpl.LeashImpl leash = new LeashRendererImpl.LeashImpl(((com.ticxo.modelengine.api.model.bone.type.Leash)leashData).getId());
         leash.position.set(((com.ticxo.modelengine.api.model.bone.type.Leash)leashData).getLocation());
         leash.connected.set(this.getConnectedId((com.ticxo.modelengine.api.model.bone.type.Leash)leashData));
         this.spawnQueue.put(boneId, leash);
         this.destroyQueue.remove(boneId);
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
            LeashRenderer.Leash renderer = (LeashRenderer.Leash)this.rendered.get(boneId);
            if (renderer != null) {
               this.read(boneId, renderer, modelBone);
            } else {
               this.create(boneId, modelBone);
            }
         }

      }
   }

   private void read(String boneId, LeashRenderer.Leash leash, ModelBone modelBone) {
      Optional maybeData = modelBone.getBoneBehavior(BoneBehaviorTypes.LEASH);
      maybeData.ifPresent((leashData) -> {
         leash.getPosition().set(((com.ticxo.modelengine.api.model.bone.type.Leash)leashData).getLocation());
         leash.getConnected().set(this.getConnectedId((com.ticxo.modelengine.api.model.bone.type.Leash)leashData));
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

   private int getConnectedId(com.ticxo.modelengine.api.model.bone.type.Leash leash) {
      Entity entity = leash.getConnectedEntity();
      if (entity != null) {
         return entity.getEntityId();
      } else {
         BoneBehavior other = (BoneBehavior)leash.getConnectedLeash();
         return other != null ? ((com.ticxo.modelengine.api.model.bone.type.Leash)other).getId() : -1;
      }
   }

   public Map<String, LeashRenderer.Leash> getSpawnQueue() {
      return this.spawnQueue;
   }

   public Map<String, LeashRenderer.Leash> getRendered() {
      return this.rendered;
   }

   public Map<String, LeashRenderer.Leash> getDestroyQueue() {
      return this.destroyQueue;
   }

   public static class LeashImpl implements LeashRenderer.Leash {
      private final int id;
      private final UUID uuid = UUID.randomUUID();
      private final DataTracker<Vector3f> position = new UpdateDataTracker(new Vector3f(), Vector3f::set);
      private final DataTracker<Integer> connected = new DataTracker();

      public LeashImpl(int id) {
         this.id = id;
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

      public DataTracker<Integer> getConnected() {
         return this.connected;
      }
   }
}
