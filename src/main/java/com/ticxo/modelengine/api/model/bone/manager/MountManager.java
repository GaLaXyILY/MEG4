package com.ticxo.modelengine.api.model.bone.manager;

import com.ticxo.modelengine.api.model.bone.behavior.BoneBehavior;
import com.ticxo.modelengine.api.model.bone.type.Mount;
import com.ticxo.modelengine.api.mount.controller.MountController;
import com.ticxo.modelengine.api.mount.controller.MountControllerSupplier;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface MountManager {
   void setCanDrive(boolean var1);

   boolean canDrive();

   void setCanRide(boolean var1);

   boolean canRide();

   Entity getDriver();

   boolean isControlled();

   boolean hasPassengers();

   boolean hasRiders();

   @Nullable
   <T extends Mount & BoneBehavior> T getDriverBone();

   <T extends Mount & BoneBehavior> void setDriverBone(@Nullable T var1);

   <T extends Mount & BoneBehavior> void registerSeat(T var1);

   <T extends Mount & BoneBehavior> Map<String, T> getSeats();

   <T extends Mount & BoneBehavior> Optional<T> getSeat(String var1);

   <T extends Mount & BoneBehavior> Optional<T> getMount(Entity var1);

   Map<Entity, Mount> getPassengerSeatMap();

   boolean mountDriver(Entity var1, MountControllerSupplier var2);

   boolean mountDriver(Entity var1, MountControllerSupplier var2, @Nullable Consumer<MountController> var3);

   boolean mountPassenger(String var1, Entity var2, MountControllerSupplier var3);

   boolean mountPassenger(String var1, Entity var2, MountControllerSupplier var3, @Nullable Consumer<MountController> var4);

   boolean mountPassenger(Mount var1, Entity var2, MountControllerSupplier var3);

   boolean mountPassenger(Mount var1, Entity var2, MountControllerSupplier var3, @Nullable Consumer<MountController> var4);

   boolean mountAvailable(Entity var1, MountControllerSupplier var2);

   boolean mountAvailable(Entity var1, MountControllerSupplier var2, @Nullable Consumer<MountController> var3);

   Set<Entity> mountAvailable(Collection<Entity> var1, MountControllerSupplier var2);

   Set<Entity> mountAvailable(Collection<Entity> var1, MountControllerSupplier var2, @Nullable Consumer<MountController> var3);

   boolean mountAvailable(Entity var1, Collection<String> var2, MountControllerSupplier var3);

   boolean mountAvailable(Entity var1, Collection<String> var2, MountControllerSupplier var3, @Nullable Consumer<MountController> var4);

   Set<Entity> mountAvailable(Collection<Entity> var1, Collection<String> var2, MountControllerSupplier var3);

   Set<Entity> mountAvailable(Collection<Entity> var1, Collection<String> var2, MountControllerSupplier var3, @Nullable Consumer<MountController> var4);

   boolean mountLeastOccupied(Entity var1, MountControllerSupplier var2);

   boolean mountLeastOccupied(Entity var1, MountControllerSupplier var2, @Nullable Consumer<MountController> var3);

   Set<Entity> mountLeastOccupied(Collection<Entity> var1, MountControllerSupplier var2);

   Set<Entity> mountLeastOccupied(Collection<Entity> var1, MountControllerSupplier var2, @Nullable Consumer<MountController> var3);

   boolean mountLeastOccupied(Entity var1, Collection<String> var2, MountControllerSupplier var3);

   boolean mountLeastOccupied(Entity var1, Collection<String> var2, MountControllerSupplier var3, @Nullable Consumer<MountController> var4);

   Set<Entity> mountLeastOccupied(Collection<Entity> var1, Collection<String> var2, MountControllerSupplier var3);

   Set<Entity> mountLeastOccupied(Collection<Entity> var1, Collection<String> var2, MountControllerSupplier var3, @Nullable Consumer<MountController> var4);

   Entity dismountDriver();

   void dismountPassenger(@NotNull Entity var1);

   void dismountRider(@NotNull Entity var1);

   Set<Entity> dismountPassengers(String var1);

   Set<Entity> dismountAll();
}
