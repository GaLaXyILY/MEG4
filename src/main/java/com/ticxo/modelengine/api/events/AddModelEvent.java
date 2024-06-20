package com.ticxo.modelengine.api.events;

import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class AddModelEvent extends AbstractEvent implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private final ModeledEntity target;
   private final ActiveModel model;
   private boolean overrideHitbox;
   private boolean cancelled;

   @NotNull
   public static HandlerList getHandlerList() {
      return handlers;
   }

   @NotNull
   public HandlerList getHandlers() {
      return handlers;
   }

   public ModeledEntity getTarget() {
      return this.target;
   }

   public ActiveModel getModel() {
      return this.model;
   }

   public boolean isOverrideHitbox() {
      return this.overrideHitbox;
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void setOverrideHitbox(boolean overrideHitbox) {
      this.overrideHitbox = overrideHitbox;
   }

   public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }

   public AddModelEvent(ModeledEntity target, ActiveModel model) {
      this.target = target;
      this.model = model;
   }
}
