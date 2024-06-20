package com.ticxo.modelengine.core.listener;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.menu.AbstractScreen;
import com.ticxo.modelengine.api.menu.ScreenManager;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

public class InventoryListener implements Listener {
   @EventHandler
   public void onInventoryClick(InventoryClickEvent event) {
      if (!event.isCancelled()) {
         Inventory inventory = event.getInventory();
         AbstractScreen screen = this.getScreenManager().getScreen(inventory);
         if (screen != null) {
            HumanEntity var5 = event.getWhoClicked();
            if (var5 instanceof Player) {
               Player player = (Player)var5;
               if (event.getClickedInventory() == inventory) {
                  event.setCancelled(true);
                  screen.onClick(player, event.getSlot(), event);
               } else if (event.isShiftClick()) {
                  event.setCancelled(true);
               }

               return;
            }
         }

      }
   }

   @EventHandler
   public void onInventoryDrag(InventoryDragEvent event) {
      if (this.getScreenManager().isScreen(event.getInventory())) {
         event.setCancelled(true);
      }

   }

   @EventHandler
   public void onInventoryClose(InventoryCloseEvent event) {
      this.getScreenManager().unregisterScreen(event.getInventory());
   }

   private ScreenManager getScreenManager() {
      return ModelEngineAPI.getAPI().getScreenManager();
   }
}
