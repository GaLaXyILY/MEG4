package com.ticxo.modelengine.api.menu;

import com.ticxo.modelengine.api.ModelEngineAPI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public abstract class AbstractScreen {
   protected final Player viewer;
   protected final Inventory inventory;
   protected final List<Widget> widgets = new ArrayList();
   protected final Map<Integer, Widget> widgetReference = new HashMap();
   protected final Set<Integer> dirtySlots = new HashSet();

   public AbstractScreen(Player viewer, String title, int rows) {
      this.viewer = viewer;
      this.inventory = Bukkit.createInventory((InventoryHolder)null, rows * 9, title);
   }

   public void openScreen() {
      this.draw(true);
      this.viewer.openInventory(this.inventory);
      ModelEngineAPI.getAPI().getScreenManager().registerScreen(this);
   }

   public void onTick() {
      if (!this.dirtySlots.isEmpty()) {
         this.draw(false);
      }

   }

   public void onClick(Player player, int slot, InventoryClickEvent event) {
      Widget widget = (Widget)this.widgetReference.get(slot);
      if (widget != null) {
         widget.onClick(this, player, slot, event);
      }

   }

   public void addWidget(Widget widget) {
      this.widgets.add(widget);
   }

   public void markSlotDirty(int slot) {
      this.dirtySlots.add(slot);
   }

   public void markSlotsDirty(int... slots) {
      int[] var2 = slots;
      int var3 = slots.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         int slot = var2[var4];
         this.dirtySlots.add(slot);
      }

   }

   public void draw(boolean all) {
      int size = this.inventory.getSize();
      int slot;
      if (all) {
         this.inventory.clear();
         this.widgetReference.clear();

         for(int i = 0; i < size; ++i) {
            for(slot = this.widgets.size() - 1; slot >= 0; --slot) {
               Widget widget = (Widget)this.widgets.get(slot);
               ItemStack item = widget.getItemForSlot(size, i);
               if (item != null) {
                  this.inventory.setItem(i, item);
                  this.widgetReference.put(i, widget);
                  break;
               }
            }
         }
      } else {
         if (this.dirtySlots.isEmpty()) {
            return;
         }

         Iterator var8 = this.dirtySlots.iterator();

         while(true) {
            label38:
            while(var8.hasNext()) {
               slot = (Integer)var8.next();

               for(int j = this.widgets.size() - 1; j >= 0; --j) {
                  Widget widget = (Widget)this.widgets.get(j);
                  ItemStack item = widget.getItemForSlot(size, slot);
                  if (item != null) {
                     this.inventory.setItem(slot, item);
                     this.widgetReference.put(slot, widget);
                     continue label38;
                  }
               }

               this.inventory.setItem(slot, (ItemStack)null);
               this.widgetReference.remove(slot);
            }

            this.dirtySlots.clear();
            break;
         }
      }

   }

   public Player getViewer() {
      return this.viewer;
   }

   public Inventory getInventory() {
      return this.inventory;
   }
}
