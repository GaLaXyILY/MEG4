package com.ticxo.modelengine.core.menu.widget;

import com.ticxo.modelengine.api.menu.AbstractScreen;
import com.ticxo.modelengine.api.menu.Widget;
import com.ticxo.modelengine.api.utils.data.ComponentUtil;
import com.ticxo.modelengine.api.utils.data.ItemUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class CloseWidget implements Widget {
   private final ItemStack close;
   private final AbstractScreen rootScreen;

   public CloseWidget(@Nullable AbstractScreen screen) {
      this.close = new ItemStack(Material.BARRIER);
      ItemUtils.name(this.close, Component.text("Close", ComponentUtil.reset()));
      this.rootScreen = screen;
   }

   public ItemStack getItemForSlot(int size, int slot) {
      return slot == size - 5 ? this.close : null;
   }

   public void onClick(AbstractScreen screen, Player player, int slot, InventoryClickEvent event) {
      if (slot == screen.getInventory().getSize() - 5) {
         if (this.rootScreen == null) {
            player.closeInventory();
         } else {
            this.rootScreen.openScreen();
         }
      }

   }
}
