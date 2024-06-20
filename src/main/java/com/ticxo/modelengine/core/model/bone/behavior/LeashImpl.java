package com.ticxo.modelengine.core.model.bone.behavior;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.bone.ModelBone;
import com.ticxo.modelengine.api.model.bone.behavior.AbstractBoneBehavior;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehavior;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehaviorData;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehaviorType;
import com.ticxo.modelengine.api.model.bone.manager.LeashManager;
import com.ticxo.modelengine.api.model.bone.type.Leash;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.joml.Vector3f;

public class LeashImpl extends AbstractBoneBehavior<LeashImpl> implements Leash {
   private final boolean mainLeash;
   private final int id;
   private final Vector3f location = new Vector3f();
   private Entity connectedEntity;
   private Leash connectedLeash;

   public LeashImpl(ModelBone bone, BoneBehaviorType<LeashImpl> type, BoneBehaviorData data) {
      super(bone, type, data);
      this.mainLeash = (Boolean)data.get("main", false);
      this.id = ModelEngineAPI.getEntityHandler().getNextEntityId();
   }

   public void onApply() {
      this.bone.getActiveModel().getLeashManager().ifPresent((leashManager) -> {
         ((LeashManager)leashManager).registerLeash(this);
      });
   }

   public void onFinalize() {
      Location baseLocation = this.bone.getActiveModel().getModeledEntity().getBase().getLocation();
      this.bone.getGlobalPosition().rotateY((180.0F - this.bone.getYaw()) * 0.017453292F, this.location).add((float)baseLocation.getX(), (float)baseLocation.getY(), (float)baseLocation.getZ());
   }

   public void connect(Entity entity) {
      this.connectedEntity = entity;
   }

   public <T extends Leash & BoneBehavior> void connect(T leash) {
      if (leash != this) {
         this.connectedLeash = leash;
      }
   }

   public void disconnect() {
      this.connectedEntity = null;
      this.connectedLeash = null;
   }

   public <T extends Leash & BoneBehavior> T getConnectedLeash() {
      return this.connectedLeash;
   }

   public boolean isMainLeash() {
      return this.mainLeash;
   }

   public int getId() {
      return this.id;
   }

   public Vector3f getLocation() {
      return this.location;
   }

   public Entity getConnectedEntity() {
      return this.connectedEntity;
   }
}
