package com.ticxo.modelengine.core.model.bone.render;

import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.bone.BoneBehaviorTypes;
import com.ticxo.modelengine.api.model.bone.ModelBone;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehavior;
import com.ticxo.modelengine.api.model.bone.render.renderer.NameTagRenderer;
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

public class NameTagRendererImpl extends AbstractBehaviorRenderer implements NameTagRenderer {
   private final Map<String, NameTagRenderer.NameTag> spawnQueue = new HashMap();
   private final Map<String, NameTagRenderer.NameTag> rendered = new HashMap();
   private final Map<String, NameTagRenderer.NameTag> destroyQueue = new HashMap();
   private boolean initialized;

   public NameTagRendererImpl(ActiveModel activeModel) {
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
      Optional maybeData = modelBone.getBoneBehavior(BoneBehaviorTypes.NAMETAG);
      if (!maybeData.isEmpty()) {
         BoneBehavior nameTagData = (BoneBehavior)maybeData.get();
         NameTagRendererImpl.NameTagImpl nameTag = new NameTagRendererImpl.NameTagImpl(this.nmsHandler.getEntityHandler().getNextEntityId(), UUID.randomUUID(), this.nmsHandler.getEntityHandler().getNextEntityId(), UUID.randomUUID());
         nameTag.position.set(((com.ticxo.modelengine.api.model.bone.type.NameTag)nameTagData).getLocation());
         nameTag.jsonString.set(((com.ticxo.modelengine.api.model.bone.type.NameTag)nameTagData).getJsonString());
         nameTag.visibility.set(((com.ticxo.modelengine.api.model.bone.type.NameTag)nameTagData).isVisible());
         this.spawnQueue.put(boneId, nameTag);
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
            NameTagRenderer.NameTag renderer = (NameTagRenderer.NameTag)this.rendered.get(boneId);
            if (renderer != null) {
               this.read(boneId, renderer, modelBone);
            } else {
               this.create(boneId, modelBone);
            }
         }

      }
   }

   private void read(String boneId, NameTagRenderer.NameTag nameTag, ModelBone modelBone) {
      Optional maybeData = modelBone.getBoneBehavior(BoneBehaviorTypes.NAMETAG);
      maybeData.ifPresent((nameTagData) -> {
         nameTag.getPosition().set(((com.ticxo.modelengine.api.model.bone.type.NameTag)nameTagData).getLocation());
         nameTag.getJsonString().set(((com.ticxo.modelengine.api.model.bone.type.NameTag)nameTagData).getJsonString());
         nameTag.getVisibility().set(((com.ticxo.modelengine.api.model.bone.type.NameTag)nameTagData).isVisible());
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

   public Map<String, NameTagRenderer.NameTag> getSpawnQueue() {
      return this.spawnQueue;
   }

   public Map<String, NameTagRenderer.NameTag> getRendered() {
      return this.rendered;
   }

   public Map<String, NameTagRenderer.NameTag> getDestroyQueue() {
      return this.destroyQueue;
   }

   public static class NameTagImpl implements NameTagRenderer.NameTag {
      private final int pivotId;
      private final UUID pivotUuid;
      private final int tagId;
      private final UUID tagUuid;
      private final DataTracker<Vector3f> position = new UpdateDataTracker(new Vector3f(), Vector3f::set);
      private final DataTracker<String> jsonString = new DataTracker("{\"text\":\"\"}");
      private final DataTracker<Boolean> visibility = new DataTracker(true);

      public NameTagImpl(int pivotId, UUID pivotUuid, int tagId, UUID tagUuid) {
         this.pivotId = pivotId;
         this.pivotUuid = pivotUuid;
         this.tagId = tagId;
         this.tagUuid = tagUuid;
      }

      public int getPivotId() {
         return this.pivotId;
      }

      public UUID getPivotUuid() {
         return this.pivotUuid;
      }

      public int getTagId() {
         return this.tagId;
      }

      public UUID getTagUuid() {
         return this.tagUuid;
      }

      public DataTracker<Vector3f> getPosition() {
         return this.position;
      }

      public DataTracker<String> getJsonString() {
         return this.jsonString;
      }

      public DataTracker<Boolean> getVisibility() {
         return this.visibility;
      }
   }
}
