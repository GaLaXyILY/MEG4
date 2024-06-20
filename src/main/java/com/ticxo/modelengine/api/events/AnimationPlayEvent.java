package com.ticxo.modelengine.api.events;

import com.ticxo.modelengine.api.animation.property.IAnimationProperty;
import com.ticxo.modelengine.api.model.ActiveModel;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class AnimationPlayEvent extends AbstractEvent implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private final ActiveModel model;
   private final IAnimationProperty property;
   private boolean cancelled;

   @NotNull
   public static HandlerList getHandlerList() {
      return handlers;
   }

   @NotNull
   public HandlerList getHandlers() {
      return handlers;
   }

   public ActiveModel getModel() {
      return this.model;
   }

   public IAnimationProperty getProperty() {
      return this.property;
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }

   public AnimationPlayEvent(ActiveModel model, IAnimationProperty property) {
      this.model = model;
      this.property = property;
   }
}
