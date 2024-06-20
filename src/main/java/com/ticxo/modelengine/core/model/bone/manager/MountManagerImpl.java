package com.ticxo.modelengine.core.model.bone.manager;

import com.google.common.collect.ImmutableMap;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.events.ModelDismountEvent;
import com.ticxo.modelengine.api.events.ModelMountEvent;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehavior;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehaviorType;
import com.ticxo.modelengine.api.model.bone.manager.AbstractBehaviorManager;
import com.ticxo.modelengine.api.model.bone.manager.MountManager;
import com.ticxo.modelengine.api.model.bone.type.Mount;
import com.ticxo.modelengine.api.mount.controller.MountController;
import com.ticxo.modelengine.api.mount.controller.MountControllerSupplier;
import com.ticxo.modelengine.core.model.bone.behavior.MountImpl;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MountManagerImpl extends AbstractBehaviorManager<MountImpl> implements MountManager {
   private final Map<String, ?> seats = new LinkedHashMap();
   private final Map<Entity, Mount> passengerSeatMap = new HashMap();
   boolean canDrive;
   boolean canRide;
   private Entity driver;
   private Mount driverBone;

   public MountManagerImpl(ActiveModel activeModel, BoneBehaviorType<MountImpl> supplier) {
      super(activeModel, supplier);
   }

   public void onDestroy() {
      this.dismountDriver();
      this.dismountAll();
   }

   public boolean canDrive() {
      return this.canDrive;
   }

   public boolean canRide() {
      return this.canRide;
   }

   public boolean isControlled() {
      return this.driver != null && this.driverBone != null;
   }

   public boolean hasPassengers() {
      return !this.passengerSeatMap.isEmpty();
   }

   public boolean hasRiders() {
      return this.driver != null || !this.passengerSeatMap.isEmpty();
   }

   @Nullable
   public <T extends Mount & BoneBehavior> T getDriverBone() {
      return this.driverBone;
   }

   public <T extends Mount & BoneBehavior> void setDriverBone(@Nullable T mount) {
      this.driverBone = mount;
   }

   public <T extends Mount & BoneBehavior> void registerSeat(T mount) {
      this.getSeats().put(((BoneBehavior)mount).getBone().getUniqueBoneId(), (BoneBehavior)mount);
   }

   public <T extends Mount & BoneBehavior> Map<String, T> getSeats() {
      return this.seats;
   }

   public <T extends Mount & BoneBehavior> Optional<T> getSeat(String boneId) {
      return Optional.ofNullable((Mount)this.getSeats().get(boneId));
   }

   public <T extends Mount & BoneBehavior> Optional<T> getMount(Entity entity) {
      if (entity == this.driver) {
         return Optional.ofNullable(this.getDriverBone());
      } else {
         Mount mount = (Mount)this.passengerSeatMap.get(entity);
         return Optional.ofNullable(mount);
      }
   }

   public Map<Entity, Mount> getPassengerSeatMap() {
      return ImmutableMap.copyOf(this.passengerSeatMap);
   }

   public boolean mountDriver(Entity entity, MountControllerSupplier supplier) {
      return this.mountDriver(entity, supplier, (Consumer)null);
   }

   public boolean mountDriver(Entity entity, MountControllerSupplier supplier, @Nullable Consumer<MountController> consumer) {
      if (this.driverBone != null && this.driverBone.getPassengers().isEmpty() && this.canDrive()) {
         ModelMountEvent event = new ModelMountEvent(this.activeModel, entity, true, this.driverBone);
         ModelEngineAPI.callEvent(event);
         if (event.isCancelled()) {
            return false;
         } else {
            boolean success = this.driverBone.addPassenger(entity);
            if (success) {
               this.driver = entity;
               this.registerMountPair(entity, this.driverBone, supplier, consumer);
            }

            return success;
         }
      } else {
         return false;
      }
   }

   public boolean mountPassenger(String boneId, Entity entity, MountControllerSupplier supplier) {
      return this.mountPassenger((String)boneId, entity, supplier, (Consumer)null);
   }

   public boolean mountPassenger(String boneId, Entity entity, MountControllerSupplier supplier, @Nullable Consumer<MountController> consumer) {
      return this.canRide() && (Boolean)this.getSeat(boneId).map((mount) -> {
         return this.mountPassenger((Mount)mount, entity, supplier, consumer);
      }).orElse(false);
   }

   public boolean mountPassenger(Mount mount, Entity entity, MountControllerSupplier supplier) {
      return this.mountPassenger((Mount)mount, entity, supplier, (Consumer)null);
   }

   public boolean mountPassenger(Mount mount, Entity entity, MountControllerSupplier supplier, @Nullable Consumer<MountController> consumer) {
      ModelMountEvent event = new ModelMountEvent(this.activeModel, entity, false, mount);
      ModelEngineAPI.callEvent(event);
      if (event.isCancelled()) {
         return false;
      } else {
         boolean success = mount.addPassenger(entity);
         if (success) {
            this.passengerSeatMap.put(entity, mount);
            this.registerMountPair(entity, mount, supplier, consumer);
         }

         return success;
      }
   }

   public boolean mountAvailable(Entity entity, MountControllerSupplier supplier) {
      return this.mountAvailable((Entity)entity, (MountControllerSupplier)supplier, (Consumer)null);
   }

   public boolean mountAvailable(Entity entity, MountControllerSupplier supplier, @Nullable Consumer<MountController> consumer) {
      return this.mountAvailable((Entity)entity, this.seats.keySet(), supplier, consumer);
   }

   public Set<Entity> mountAvailable(Collection<Entity> entities, MountControllerSupplier supplier) {
      return this.mountAvailable((Collection)entities, (MountControllerSupplier)supplier, (Consumer)null);
   }

   public Set<Entity> mountAvailable(Collection<Entity> entities, MountControllerSupplier supplier, @Nullable Consumer<MountController> consumer) {
      return this.mountAvailable((Collection)entities, this.seats.keySet(), supplier, consumer);
   }

   public boolean mountAvailable(Entity entity, Collection<String> seats, MountControllerSupplier supplier) {
      return this.mountAvailable((Entity)entity, seats, supplier, (Consumer)null);
   }

   public boolean mountAvailable(Entity entity, Collection<String> seats, MountControllerSupplier supplier, @Nullable Consumer<MountController> consumer) {
      Iterator var5 = seats.iterator();

      while(var5.hasNext()) {
         String seatId = (String)var5.next();
         Optional maybeSeat = this.getSeat(seatId);
         if (!maybeSeat.isEmpty()) {
            BoneBehavior seat = (BoneBehavior)maybeSeat.get();
            if (((Mount)seat).getPassengers().isEmpty()) {
               this.mountPassenger((Mount)seat, entity, supplier, consumer);
               return true;
            }
         }
      }

      return false;
   }

   public Set<Entity> mountAvailable(Collection<Entity> entities, Collection<String> seats, MountControllerSupplier supplier) {
      return this.mountAvailable((Collection)entities, seats, supplier, (Consumer)null);
   }

   public Set<Entity> mountAvailable(Collection<Entity> entities, Collection<String> seats, MountControllerSupplier supplier, @Nullable Consumer<MountController> consumer) {
      HashSet<Entity> unseated = new HashSet();
      boolean noSeats = false;
      Iterator var7 = entities.iterator();

      while(true) {
         label28:
         while(var7.hasNext()) {
            Entity entity = (Entity)var7.next();
            if (!noSeats) {
               noSeats = true;
               Iterator var9 = seats.iterator();

               while(var9.hasNext()) {
                  String seatId = (String)var9.next();
                  Optional maybeSeat = this.getSeat(seatId);
                  if (!maybeSeat.isEmpty()) {
                     BoneBehavior seat = (BoneBehavior)maybeSeat.get();
                     if (((Mount)seat).getPassengers().isEmpty()) {
                        this.mountPassenger((Mount)seat, entity, supplier, consumer);
                        noSeats = false;
                        continue label28;
                     }
                  }
               }
            }

            unseated.add(entity);
         }

         return unseated;
      }
   }

   public boolean mountLeastOccupied(Entity entity, MountControllerSupplier supplier) {
      return this.mountLeastOccupied((Entity)entity, (MountControllerSupplier)supplier, (Consumer)null);
   }

   public boolean mountLeastOccupied(Entity entity, MountControllerSupplier supplier, @Nullable Consumer<MountController> consumer) {
      return this.mountLeastOccupied((Entity)entity, this.seats.keySet(), supplier, consumer);
   }

   public Set<Entity> mountLeastOccupied(Collection<Entity> entities, MountControllerSupplier supplier) {
      return this.mountLeastOccupied((Collection)entities, (MountControllerSupplier)supplier, (Consumer)null);
   }

   public Set<Entity> mountLeastOccupied(Collection<Entity> entities, MountControllerSupplier supplier, @Nullable Consumer<MountController> consumer) {
      return this.mountLeastOccupied((Collection)entities, this.seats.keySet(), supplier, consumer);
   }

   public boolean mountLeastOccupied(Entity entity, Collection<String> seats, MountControllerSupplier supplier) {
      return this.mountLeastOccupied((Entity)entity, seats, supplier, (Consumer)null);
   }

   public boolean mountLeastOccupied(Entity entity, Collection<String> seats, MountControllerSupplier supplier, @Nullable Consumer<MountController> consumer) {
      int leastCount = Integer.MAX_VALUE;
      Mount leastOccupied = null;
      Iterator var7 = seats.iterator();

      while(var7.hasNext()) {
         String seatId = (String)var7.next();
         Optional maybeSeat = this.getSeat(seatId);
         if (!maybeSeat.isEmpty()) {
            BoneBehavior seat = (BoneBehavior)maybeSeat.get();
            if (((Mount)seat).canMountMore()) {
               int count = ((Mount)seat).getPassengers().size();
               if (count == 0) {
                  return this.mountPassenger((Mount)seat, entity, supplier, consumer);
               }

               if (leastCount > count) {
                  leastCount = count;
                  leastOccupied = (Mount)seat;
               }
            }
         }
      }

      if (leastOccupied != null) {
         return this.mountPassenger(leastOccupied, entity, supplier, consumer);
      } else {
         return false;
      }
   }

   public Set<Entity> mountLeastOccupied(Collection<Entity> entities, Collection<String> seats, MountControllerSupplier supplier) {
      return this.mountLeastOccupied((Collection)entities, seats, supplier, (Consumer)null);
   }

   public Set<Entity> mountLeastOccupied(Collection<Entity> entities, Collection<String> seats, MountControllerSupplier supplier, @Nullable Consumer<MountController> consumer) {
      HashSet<Entity> set = new HashSet();
      Iterator var6 = entities.iterator();

      while(var6.hasNext()) {
         Entity entity = (Entity)var6.next();
         if (!this.mountLeastOccupied(entity, seats, supplier, consumer)) {
            set.add(entity);
         }
      }

      return set;
   }

   public Entity dismountDriver() {
      if (this.driverBone != null && this.driver != null) {
         ModelDismountEvent event = new ModelDismountEvent(this.activeModel, this.driver, true, this.driverBone);
         ModelEngineAPI.callEvent(event);
         if (event.isCancelled()) {
            return null;
         } else {
            this.driverBone.removePassenger(this.driver);
            this.removeMountPair(this.driver);
            Entity t = this.driver;
            this.driver = null;
            return t;
         }
      } else {
         return null;
      }
   }

   public void dismountPassenger(@NotNull Entity entity) {
      Mount mount = (Mount)this.passengerSeatMap.remove(entity);
      if (mount != null) {
         ModelDismountEvent event = new ModelDismountEvent(this.activeModel, entity, false, mount);
         ModelEngineAPI.callEvent(event);
         if (event.isCancelled()) {
            return;
         }

         mount.removePassenger(entity);
         this.removeMountPair(entity);
      }

   }

   public void dismountRider(@NotNull Entity entity) {
      if (entity == this.driver) {
         this.dismountDriver();
      } else {
         this.dismountPassenger(entity);
      }

   }

   public Set<Entity> dismountPassengers(String boneId) {
      HashSet<Entity> removed = new HashSet();
      this.getSeat(boneId).ifPresent((mount) -> {
         Set<Entity> passengers = ((Mount)mount).getPassengers();
         Iterator var4 = passengers.iterator();

         while(var4.hasNext()) {
            Entity entity = (Entity)var4.next();
            ModelDismountEvent event = new ModelDismountEvent(this.activeModel, entity, false, (Mount)mount);
            ModelEngineAPI.callEvent(event);
            if (!event.isCancelled()) {
               ((Mount)mount).removePassenger(entity);
               this.removeMountPair(entity);
               removed.add(entity);
            }
         }

      });
      return removed;
   }

   public Set<Entity> dismountAll() {
      HashSet<Entity> set = new HashSet();
      Iterator var2 = this.getSeats().values().iterator();

      while(var2.hasNext()) {
         BoneBehavior val = (BoneBehavior)var2.next();
         Set<Entity> passengers = ((Mount)val).clearPassengers();
         Iterator var5 = passengers.iterator();

         while(var5.hasNext()) {
            Entity entity = (Entity)var5.next();
            ModelDismountEvent event = new ModelDismountEvent(this.activeModel, entity, false, (Mount)val);
            ModelEngineAPI.callEvent(event);
            if (!event.isCancelled()) {
               this.passengerSeatMap.remove(entity);
               this.removeMountPair(entity);
               set.add(entity);
            }
         }
      }

      return set;
   }

   private void registerMountPair(Entity entity, Mount mount, MountControllerSupplier supplier, @Nullable Consumer<MountController> consumer) {
      MountController controller = supplier.createController(entity, mount);
      if (consumer != null) {
         consumer.accept(controller);
      }

      ModelEngineAPI.getMountPairManager().registerMountedPair(entity, this.activeModel, controller);
      this.activeModel.getModeledEntity().getBase().setCollidableWith(entity, false);
   }

   private void removeMountPair(Entity entity) {
      ModelEngineAPI.getMountPairManager().unregisterMountedPair(entity.getUniqueId());
      this.activeModel.getModeledEntity().getBase().setCollidableWith(entity, true);
   }

   public void setCanDrive(boolean canDrive) {
      this.canDrive = canDrive;
   }

   public void setCanRide(boolean canRide) {
      this.canRide = canRide;
   }

   public Entity getDriver() {
      return this.driver;
   }
}
