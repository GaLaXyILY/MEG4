package com.ticxo.modelengine.core.vfx.render;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.nms.entity.EntityHandler;
import com.ticxo.modelengine.api.utils.data.tracker.DataTracker;
import com.ticxo.modelengine.api.utils.data.tracker.UpdateDataTracker;
import com.ticxo.modelengine.api.vfx.VFX;
import com.ticxo.modelengine.api.vfx.render.VFXDisplayRenderer;
import com.ticxo.modelengine.api.vfx.render.VFXRendererParser;
import java.util.UUID;
import org.bukkit.inventory.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class VFXDisplayRendererImpl implements VFXDisplayRenderer {
   private final VFX vfx;
   private final EntityHandler entityHandler;
   private final VFXRendererParser<VFXDisplayRenderer> parser;
   private VFXDisplayRendererImpl.VFXModelImpl vfxModel;
   private boolean respawnRequired;
   private boolean initialized;

   public VFXDisplayRendererImpl(VFX vfx) {
      this.vfx = vfx;
      this.entityHandler = ModelEngineAPI.getEntityHandler();
      this.parser = ModelEngineAPI.getNMSHandler().getVFXRendererParser(this);
   }

   public VFX getVFX() {
      return this.vfx;
   }

   public void initialize() {
      this.vfxModel = new VFXDisplayRendererImpl.VFXModelImpl(this.entityHandler.getNextEntityId(), UUID.randomUUID(), this.entityHandler.getNextEntityId(), UUID.randomUUID());
      this.vfxModel.getOrigin().set(this.vfx.getOrigin().toVector3f());
      this.vfxModel.getPosition().set(this.calculatePosition());
      this.vfxModel.getLeftRotation().set(this.calculateRotation());
      this.vfxModel.getScale().set(this.vfx.getScale());
      this.vfxModel.getModel().set(this.vfx.isVisible() ? this.vfx.getModel() : null);
      this.initialized = true;
      this.respawnRequired = true;
   }

   public void readVFXData() {
      if (this.initialized) {
         this.vfxModel.getOrigin().set(this.vfx.getOrigin().toVector3f());
         this.vfxModel.getPosition().set(this.calculatePosition());
         this.vfxModel.getLeftRotation().set(this.calculateRotation());
         this.vfxModel.getScale().set(this.vfx.getScale());
         if (!this.vfx.isVisible()) {
            this.vfxModel.getModel().set((Object)null);
         } else {
            this.vfxModel.getModel().set(this.vfx.getModel());
            if (this.vfx.getModelTracker().isDirty()) {
               this.vfx.getModelTracker().clearDirty();
               this.vfxModel.getModel().markDirty();
            }
         }

      }
   }

   public void sendToClient() {
      if (this.initialized) {
         this.parser.sendToClients(this);
      }
   }

   public void destroy() {
      if (this.initialized) {
         this.parser.destroy(this);
      }
   }

   public VFXDisplayRenderer.VFXModel getVFXModel() {
      return this.vfxModel;
   }

   private Vector3f calculatePosition() {
      float yaw = this.vfx.getYaw();
      float pitch = this.vfx.getPitch();
      Vector3f pos = this.vfx.getPosition();
      return (new Vector3f(pos)).rotateX(pitch * 0.017453292F).rotateY(-yaw * 0.017453292F);
   }

   private Quaternionf calculateRotation() {
      float yaw = this.vfx.getYaw();
      float pitch = this.vfx.getPitch();
      Vector3f rot = this.vfx.getRotation();
      return (new Quaternionf()).rotateY((180.0F - yaw) * 0.017453292F).rotateX(-pitch * 0.017453292F).rotateZYX(rot.z, rot.y, rot.x);
   }

   public boolean isRespawnRequired() {
      return this.respawnRequired;
   }

   public void setRespawnRequired(boolean respawnRequired) {
      this.respawnRequired = respawnRequired;
   }

   public boolean isInitialized() {
      return this.initialized;
   }

   public static class VFXModelImpl implements VFXDisplayRenderer.VFXModel {
      private final int pivotId;
      private final UUID pivotUuid;
      private final int modelId;
      private final UUID modelUuid;
      private final DataTracker<Vector3f> origin = new UpdateDataTracker(new Vector3f(), Vector3f::set);
      private final DataTracker<Vector3f> position = new UpdateDataTracker(new Vector3f(), Vector3f::set);
      private final DataTracker<Quaternionf> leftRotation = new UpdateDataTracker(new Quaternionf(), Quaternionf::set);
      private final DataTracker<Vector3f> scale = new UpdateDataTracker(new Vector3f(), Vector3f::set);
      private final DataTracker<ItemStack> model = new DataTracker();

      public int getPivotId() {
         return this.pivotId;
      }

      public UUID getPivotUuid() {
         return this.pivotUuid;
      }

      public int getModelId() {
         return this.modelId;
      }

      public UUID getModelUuid() {
         return this.modelUuid;
      }

      public DataTracker<Vector3f> getOrigin() {
         return this.origin;
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

      public DataTracker<ItemStack> getModel() {
         return this.model;
      }

      public VFXModelImpl(int pivotId, UUID pivotUuid, int modelId, UUID modelUuid) {
         this.pivotId = pivotId;
         this.pivotUuid = pivotUuid;
         this.modelId = modelId;
         this.modelUuid = modelUuid;
      }
   }
}
