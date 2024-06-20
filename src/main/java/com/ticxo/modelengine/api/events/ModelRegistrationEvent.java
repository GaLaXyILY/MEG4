package com.ticxo.modelengine.api.events;

import com.ticxo.modelengine.api.generator.ModelGenerator;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ModelRegistrationEvent extends AbstractEvent {
   private static final HandlerList handlers = new HandlerList();
   private final ModelGenerator.Phase phase;

   @NotNull
   public static HandlerList getHandlerList() {
      return handlers;
   }

   @NotNull
   public HandlerList getHandlers() {
      return handlers;
   }

   public ModelGenerator.Phase getPhase() {
      return this.phase;
   }

   public ModelRegistrationEvent(ModelGenerator.Phase phase) {
      this.phase = phase;
   }
}
