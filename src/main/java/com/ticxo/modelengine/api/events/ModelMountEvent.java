package com.ticxo.modelengine.api.events;

import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.bone.type.Mount;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ModelMountEvent extends AbstractEvent implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private final ActiveModel vehicle;
   private final Entity passenger;
   private final boolean isDriver;
   private final Mount seat;
   private boolean cancelled;

   @NotNull
   public static HandlerList getHandlerList() {
      return handlers;
   }

   @NotNull
   public HandlerList getHandlers() {
      return handlers;
   }

   public ActiveModel getVehicle() {
      return this.vehicle;
   }

   public Entity getPassenger() {
      return this.passenger;
   }

   public boolean isDriver() {
      return this.isDriver;
   }

   public Mount getSeat() {
      return this.seat;
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }

   public ModelMountEvent(ActiveModel vehicle, Entity passenger, boolean isDriver, Mount seat) {
      this.vehicle = vehicle;
      this.passenger = passenger;
      this.isDriver = isDriver;
      this.seat = seat;
   }
}
