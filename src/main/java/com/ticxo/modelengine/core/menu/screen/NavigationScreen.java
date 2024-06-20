package com.ticxo.modelengine.core.menu.screen;

import com.ticxo.modelengine.api.menu.AbstractScreen;
import com.ticxo.modelengine.api.menu.Widget;
import com.ticxo.modelengine.api.utils.data.ComponentUtil;
import com.ticxo.modelengine.api.utils.data.ItemUtils;
import com.ticxo.modelengine.core.menu.SelectionManager;
import com.ticxo.modelengine.core.menu.widget.BorderWidget;
import com.ticxo.modelengine.core.menu.widget.CloseWidget;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public class NavigationScreen extends AbstractScreen {
   public NavigationScreen(Player player) {
      super(player, "Model Engine", 6);
      this.addWidget(new BorderWidget());
      this.addWidget(new NavigationScreen.Navigator());
      this.addWidget(new CloseWidget((AbstractScreen)null));
   }

   class Navigator implements Widget {
      private static final int SPAWN_SLOT = 29;
      private static final int SELECT_SLOT = 31;
      private static final int EDIT_SLOT = 33;
      private final ItemStack spawn;
      private final ItemStack select;
      private final ItemStack edit;
      private Entity selected;

      public Navigator() {
         if (SelectionManager.isSelecting(NavigationScreen.this.viewer)) {
            SelectionManager.setSelecting(NavigationScreen.this.viewer, false);
            NavigationScreen.this.viewer.sendActionBar(ComponentUtil.base(((TextComponent)Component.empty().style(ComponentUtil.reset())).append(Component.text("Canceled selection mode."))));
         }

         this.spawn = new ItemStack(Material.PIG_SPAWN_EGG);
         ItemUtils.name(this.spawn, Component.text("Spawn Model", ComponentUtil.reset()));
         ItemUtils.lore(this.spawn, Component.empty(), Component.text("Spawn a model at where you are standing.", ComponentUtil.reset()));
         this.selected = SelectionManager.getSelected(NavigationScreen.this.viewer);
         this.select = new ItemStack(Material.SPYGLASS);
         this.updateSelect();
         this.edit = new ItemStack(Material.WRITABLE_BOOK);
         ItemUtils.name(this.edit, Component.text("Edit Entity", ComponentUtil.reset()));
         ItemUtils.lore(this.edit, Component.empty(), Component.text("Add or remove models, states, and edit", ComponentUtil.reset()), Component.text("bone behaviors of the selected entity.", ComponentUtil.reset()));
      }

      public ItemStack getItemForSlot(int size, int slot) {
         ItemStack var10000;
         switch(slot) {
         case 29:
            var10000 = this.spawn;
            break;
         case 30:
         case 32:
         default:
            var10000 = null;
            break;
         case 31:
            var10000 = this.select;
            break;
         case 33:
            var10000 = this.edit;
         }

         return var10000;
      }

      public void onClick(AbstractScreen screen, Player player, int slot, InventoryClickEvent event) {
         ClickType click = event.getClick();
         if (!click.isKeyboardClick()) {
            switch(slot) {
            case 29:
               (new SpawnModelScreen(NavigationScreen.this, player)).openScreen();
            case 30:
            case 32:
            default:
               break;
            case 31:
               switch(click) {
               case RIGHT:
                  SelectionManager.setSelected(player, player);
                  player.sendActionBar(ComponentUtil.base(Component.text("Select " + player.getName() + ".", ComponentUtil.reset())));
                  this.selected = player;
                  this.updateSelect();
                  NavigationScreen.this.markSlotDirty(31);
                  return;
               case LEFT:
                  SelectionManager.setSelecting(player, true);
                  player.sendActionBar(ComponentUtil.base(Component.text("Select an entity by attacking the target.", ComponentUtil.reset())));
                  player.closeInventory();
                  return;
               case SHIFT_LEFT:
               case SHIFT_RIGHT:
                  SelectionManager.setSelected(player, (Entity)null);
                  this.selected = null;
                  this.updateSelect();
                  NavigationScreen.this.markSlotDirty(31);
                  return;
               default:
                  return;
               }
            case 33:
               Entity entity = SelectionManager.getSelected(player);
               if (entity != null) {
                  (new EditEntityScreen(NavigationScreen.this, player, entity)).openScreen();
               }
            }

         }
      }

      private void updateSelect() {
         ItemUtils.name(this.select, Component.text("Select Entity", ComponentUtil.reset()));
         ItemUtils.lore(this.select, Component.empty(), this.selected == null ? null : ((TextComponent)Component.empty().style(ComponentUtil.reset().decoration(TextDecoration.BOLD, true).color(NamedTextColor.AQUA))).append(Component.text("Selected: " + this.selected.getName())), this.selected == null ? null : Component.empty(), Component.text("Left click to select entity.", ComponentUtil.reset()), Component.text("Right click to select yourself.", ComponentUtil.reset()), Component.text("Shift click to clear selection.", ComponentUtil.reset()));
         this.select.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
         if (this.selected != null) {
            this.select.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);
         } else {
            this.select.removeEnchantment(Enchantment.VANISHING_CURSE);
         }

      }
   }
}
