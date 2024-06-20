package com.ticxo.modelengine.api.events;

import com.ticxo.modelengine.api.entity.BaseEntity;
import com.ticxo.modelengine.api.model.ActiveModel;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BaseEntityInteractEvent extends AbstractEvent {
   private static final HandlerList handlers = new HandlerList();
   private final Player player;
   private final BaseEntity<?> baseEntity;
   private final ActiveModel model;
   private final BaseEntityInteractEvent.Action action;
   private final EquipmentSlot slot;
   private final boolean isSecondary;
   private final ItemStack item;
   @Nullable
   private final Vector clickedPosition;

   @NotNull
   public static HandlerList getHandlerList() {
      return handlers;
   }

   @NotNull
   public HandlerList getHandlers() {
      return handlers;
   }

   public BaseEntityInteractEvent(Player player, BaseEntity<?> baseEntity, ActiveModel model, BaseEntityInteractEvent.Action action, EquipmentSlot slot, boolean isSecondary, ItemStack item, @Nullable Vector clickedPosition) {
      this.player = player;
      this.baseEntity = baseEntity;
      this.model = model;
      this.action = action;
      this.slot = slot;
      this.isSecondary = isSecondary;
      this.item = item;
      this.clickedPosition = clickedPosition;
   }

   public String toString() {
      Player var10000 = this.getPlayer();
      return "BaseEntityInteractEvent(player=" + var10000 + ", baseEntity=" + this.getBaseEntity() + ", model=" + this.getModel() + ", action=" + this.getAction() + ", slot=" + this.getSlot() + ", isSecondary=" + this.isSecondary() + ", item=" + this.getItem() + ", clickedPosition=" + this.getClickedPosition() + ")";
   }

   public Player getPlayer() {
      return this.player;
   }

   public BaseEntity<?> getBaseEntity() {
      return this.baseEntity;
   }

   public ActiveModel getModel() {
      return this.model;
   }

   public BaseEntityInteractEvent.Action getAction() {
      return this.action;
   }

   public EquipmentSlot getSlot() {
      return this.slot;
   }

   public boolean isSecondary() {
      return this.isSecondary;
   }

   public ItemStack getItem() {
      return this.item;
   }

   @Nullable
   public Vector getClickedPosition() {
      return this.clickedPosition;
   }

   public static enum Action {
      ATTACK,
      INTERACT,
      INTERACT_ON;

      // $FF: synthetic method
      private static BaseEntityInteractEvent.Action[] $values() {
         return new BaseEntityInteractEvent.Action[]{ATTACK, INTERACT, INTERACT_ON};
      }
   }
}
