package com.ticxo.modelengine.api.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public interface Widget {
   static int toSlot(int x, int y) {
      return y * 9 + x;
   }

   ItemStack getItemForSlot(int var1, int var2);

   void onClick(AbstractScreen var1, Player var2, int var3, InventoryClickEvent var4);
}
