package com.ticxo.modelengine.core.model.bone.behavior;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.bone.ModelBone;
import com.ticxo.modelengine.api.model.bone.behavior.AbstractBoneBehavior;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehaviorData;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehaviorType;
import com.ticxo.modelengine.api.model.bone.manager.MountManager;
import com.ticxo.modelengine.api.model.bone.type.Mount;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.joml.Vector3f;

public class MountImpl extends AbstractBoneBehavior<MountImpl> implements Mount {
   private final boolean driver;
   private final Vector3f location = new Vector3f();
   private final Vector3f globalLocation = new Vector3f();
   private final Set<Entity> passengers = Sets.newConcurrentHashSet();

   public MountImpl(ModelBone bone, BoneBehaviorType<MountImpl> type, BoneBehaviorData data) {
      super(bone, type, data);
      this.driver = (Boolean)data.get("driver", false);
   }

   public void onApply() {
      this.bone.getActiveModel().getMountManager().ifPresent((mountManager) -> {
         if (this.driver) {
            ((MountManager)mountManager).setDriverBone(this);
         } else {
            ((MountManager)mountManager).registerSeat(this);
         }

      });
      this.onFinalize();
   }

   public void onFinalize() {
      this.bone.getGlobalPosition().rotateY((180.0F - this.bone.getYaw()) * 0.017453292F, this.location);
      Location baseLocation = this.bone.getActiveModel().getModeledEntity().getBase().getLocation();
      this.globalLocation.set(this.location).add((float)baseLocation.getX(), (float)baseLocation.getY(), (float)baseLocation.getZ());
      this.passengers.removeIf((entity) -> {
         return ModelEngineAPI.getEntityHandler().isRemoved(entity);
      });
   }

   public boolean addPassenger(Entity entity) {
      if (this.canMountMore()) {
         this.passengers.add(entity);
         return true;
      } else {
         return false;
      }
   }

   public boolean addPassengers(Collection<Entity> entities) {
      Iterator var2 = entities.iterator();

      while(var2.hasNext()) {
         Entity entity = (Entity)var2.next();
         if (!this.canMountMore()) {
            return false;
         }

         this.passengers.add(entity);
      }

      return true;
   }

   public void removePassenger(Entity entity) {
      this.passengers.remove(entity);
   }

   public void removePassengers(Collection<Entity> entities) {
      this.passengers.removeAll(entities);
   }

   public Set<Entity> clearPassengers() {
      HashSet<Entity> set = new HashSet(this.passengers);
      this.passengers.clear();
      return set;
   }

   public Set<Entity> getPassengers() {
      return ImmutableSet.copyOf(this.passengers);
   }

   public boolean canMountMore() {
      return !this.isDriver() || this.passengers.isEmpty();
   }

   public boolean isDriver() {
      return this.driver;
   }

   public Vector3f getLocation() {
      return this.location;
   }

   public Vector3f getGlobalLocation() {
      return this.globalLocation;
   }
}
