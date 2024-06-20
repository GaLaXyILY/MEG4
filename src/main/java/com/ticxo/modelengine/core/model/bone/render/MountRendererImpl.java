package com.ticxo.modelengine.core.model.bone.render;

import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.model.bone.BoneBehaviorTypes;
import com.ticxo.modelengine.api.model.bone.ModelBone;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehavior;
import com.ticxo.modelengine.api.model.bone.render.renderer.MountRenderer;
import com.ticxo.modelengine.api.nms.RenderParsers;
import com.ticxo.modelengine.api.utils.data.tracker.CollectionDataTracker;
import com.ticxo.modelengine.api.utils.data.tracker.DataTracker;
import com.ticxo.modelengine.api.utils.data.tracker.UpdateDataTracker;
import com.ticxo.modelengine.api.utils.math.TMath;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.joml.Vector3f;

public class MountRendererImpl extends AbstractBehaviorRenderer implements MountRenderer {
   private final Map<String, MountRenderer.Mount> spawnQueue = new HashMap();
   private final Map<String, MountRenderer.Mount> rendered = new HashMap();
   private final Map<String, MountRenderer.Mount> destroyQueue = new HashMap();
   private boolean initialized;
   private int lerpTick;
   private Vector lerpLocation;
   private Vector targetLocation;

   public MountRendererImpl(ActiveModel activeModel) {
      super(activeModel);
   }

   public void initialize() {
      ModeledEntity modeledEntity = this.activeModel.getModeledEntity();
      this.targetLocation = modeledEntity.getBase().getLocation().toVector();
      this.lerpLocation = this.targetLocation.clone();
      Iterator var2 = this.activeModel.getBones().entrySet().iterator();

      while(var2.hasNext()) {
         Entry<String, ModelBone> boneEntry = (Entry)var2.next();
         String boneId = (String)boneEntry.getKey();
         ModelBone modelBone = (ModelBone)boneEntry.getValue();
         this.create(boneId, modelBone);
      }

      this.initialized = true;
   }

   private void create(String boneId, ModelBone modelBone) {
      Optional maybeData = modelBone.getBoneBehavior(BoneBehaviorTypes.MOUNT);
      if (!maybeData.isEmpty()) {
         BoneBehavior mountData = (BoneBehavior)maybeData.get();
         MountRendererImpl.MountImpl mount = new MountRendererImpl.MountImpl(this.nmsHandler.getEntityHandler().getNextEntityId(), UUID.randomUUID(), this.nmsHandler.getEntityHandler().getNextEntityId(), UUID.randomUUID());
         mount.position.set(this.lerpLocation.toVector3f().add(((com.ticxo.modelengine.api.model.bone.type.Mount)mountData).getLocation()));
         mount.yaw.set(TMath.rotToByte(modelBone.getYaw()));
         Iterator var6 = ((com.ticxo.modelengine.api.model.bone.type.Mount)mountData).getPassengers().iterator();

         while(var6.hasNext()) {
            Entity entity = (Entity)var6.next();
            ((Collection)mount.passengers.get()).add(entity.getEntityId());
         }

         this.spawnQueue.put(boneId, mount);
         this.destroyQueue.remove(boneId);
      }
   }

   public void readBoneData() {
      if (this.initialized) {
         ModeledEntity modeledEntity = this.activeModel.getModeledEntity();
         this.lerpLocation = modeledEntity.getBase().getLocation().toVector();
         this.destroyQueue.putAll(this.rendered);
         Iterator var2 = this.activeModel.getBones().entrySet().iterator();

         while(var2.hasNext()) {
            Entry<String, ModelBone> boneEntry = (Entry)var2.next();
            String boneId = (String)boneEntry.getKey();
            ModelBone modelBone = (ModelBone)boneEntry.getValue();
            MountRenderer.Mount renderer = (MountRenderer.Mount)this.rendered.get(boneId);
            if (renderer != null) {
               this.read(boneId, renderer, modelBone);
            } else {
               this.create(boneId, modelBone);
            }
         }

      }
   }

   private void read(String boneId, MountRenderer.Mount mount, ModelBone modelBone) {
      mount.getYaw().set(TMath.rotToByte(modelBone.getYaw()));
      Optional maybeData = modelBone.getBoneBehavior(BoneBehaviorTypes.MOUNT);
      maybeData.ifPresent((mountData) -> {
         mount.getPosition().set(this.lerpLocation.toVector3f().add(((com.ticxo.modelengine.api.model.bone.type.Mount)mountData).getLocation()));
         HashSet<Integer> newIds = new HashSet();
         Iterator var5 = ((com.ticxo.modelengine.api.model.bone.type.Mount)mountData).getPassengers().iterator();

         while(var5.hasNext()) {
            Entity entity = (Entity)var5.next();
            newIds.add(entity.getEntityId());
         }

         mount.getPassengers().retainAll(newIds);
         mount.getPassengers().addAll(newIds);
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

   public Map<String, MountRenderer.Mount> getSpawnQueue() {
      return this.spawnQueue;
   }

   public Map<String, MountRenderer.Mount> getRendered() {
      return this.rendered;
   }

   public Map<String, MountRenderer.Mount> getDestroyQueue() {
      return this.destroyQueue;
   }

   public static class MountImpl implements MountRenderer.Mount {
      private final int pivotId;
      private final UUID pivotUuid;
      private final int mountId;
      private final UUID mountUuid;
      private final DataTracker<Vector3f> position = new UpdateDataTracker(new Vector3f(), Vector3f::set);
      private final DataTracker<Byte> yaw = new DataTracker((byte)0);
      private final CollectionDataTracker<Integer> passengers = new CollectionDataTracker(new HashSet());

      public MountImpl(int pivotId, UUID pivotUuid, int mountId, UUID mountUuid) {
         this.pivotId = pivotId;
         this.pivotUuid = pivotUuid;
         this.mountId = mountId;
         this.mountUuid = mountUuid;
      }

      public int getPivotId() {
         return this.pivotId;
      }

      public UUID getPivotUuid() {
         return this.pivotUuid;
      }

      public int getMountId() {
         return this.mountId;
      }

      public UUID getMountUuid() {
         return this.mountUuid;
      }

      public DataTracker<Vector3f> getPosition() {
         return this.position;
      }

      public DataTracker<Byte> getYaw() {
         return this.yaw;
      }

      public CollectionDataTracker<Integer> getPassengers() {
         return this.passengers;
      }
   }
}
