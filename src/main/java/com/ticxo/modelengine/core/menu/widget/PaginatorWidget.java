package com.ticxo.modelengine.core.menu.widget;

import com.google.common.collect.ImmutableMap;
import com.ticxo.modelengine.api.menu.AbstractScreen;
import com.ticxo.modelengine.api.menu.Widget;
import com.ticxo.modelengine.api.utils.data.ComponentUtil;
import com.ticxo.modelengine.api.utils.data.ItemUtils;
import com.ticxo.modelengine.api.utils.math.TMath;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class PaginatorWidget implements Widget {
   private static final int PAGE_SLOT = 52;
   private static final int MAX_ITEM = 21;
   private static final Map<Integer, Integer> PAGE_PAIR;
   private final ItemStack pageSwitch;
   private final List<PaginatorWidget.PageButton> buttons = new ArrayList();
   private int currentPage;

   public PaginatorWidget() {
      this.pageSwitch = new ItemStack(Material.PAPER);
      ItemUtils.name(this.pageSwitch, Component.text("Turn Page", ComponentUtil.reset()));
   }

   public ItemStack getItemForSlot(int size, int slot) {
      if (slot == 52) {
         ItemUtils.lore(this.pageSwitch, ((TextComponent)((TextComponent)((TextComponent)Component.empty().style(ComponentUtil.reset())).append(Component.text("<<< Left Click").color(this.currentPage == 0 ? NamedTextColor.DARK_GRAY : NamedTextColor.WHITE))).append(Component.text(" | "))).append(Component.text("Right Click >>>").color(this.currentPage == this.getLastPage() ? NamedTextColor.DARK_GRAY : NamedTextColor.WHITE)));
         return this.pageSwitch;
      } else {
         PaginatorWidget.PageButton button = this.getButton(slot);
         return button == null ? null : button.getItemStack();
      }
   }

   public void onClick(AbstractScreen screen, Player player, int slot, InventoryClickEvent event) {
      if (slot == 52) {
         if (event.getClick().isLeftClick()) {
            --this.currentPage;
         } else if (event.getClick().isRightClick()) {
            ++this.currentPage;
         }

         this.currentPage = TMath.clamp(this.currentPage, 0, this.getLastPage());
         screen.markSlotsDirty(52);
         this.refreshPage(screen);
      } else {
         PaginatorWidget.PageButton button = this.getButton(slot);
         if (button != null) {
            button.onClick(screen, player, slot, event);
         }
      }

   }

   public void addButton(PaginatorWidget.PageButton button) {
      this.buttons.add(button);
   }

   public void clearButtons() {
      this.buttons.clear();
   }

   public PaginatorWidget.PageButton getButton(int slot) {
      Integer id = (Integer)PAGE_PAIR.get(slot);
      if (id == null) {
         return null;
      } else {
         id = this.currentPage * 21 + id;
         return id < this.buttons.size() ? (PaginatorWidget.PageButton)this.buttons.get(id) : null;
      }
   }

   public void refreshPage(AbstractScreen screen) {
      PAGE_PAIR.forEach((invSlot, id) -> {
         screen.markSlotDirty(invSlot);
      });
   }

   private int getLastPage() {
      return Math.max(TMath.ceil((double)((float)this.buttons.size() / 21.0F)) - 1, 0);
   }

   static {
      HashMap<Integer, Integer> pair = new HashMap();
      int id = 0;
      int i = 19;

      while(i <= 43) {
         switch(i % 9) {
         default:
            pair.put(i, id++);
         case 0:
         case 8:
            ++i;
         }
      }

      PAGE_PAIR = ImmutableMap.copyOf(pair);
   }

   public interface PageButton {
      ItemStack getItemStack();

      void onClick(AbstractScreen var1, Player var2, int var3, InventoryClickEvent var4);
   }
}
