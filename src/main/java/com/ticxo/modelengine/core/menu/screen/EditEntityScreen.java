package com.ticxo.modelengine.core.menu.screen;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.menu.AbstractScreen;
import com.ticxo.modelengine.api.menu.Widget;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.utils.MiscUtils;
import com.ticxo.modelengine.api.utils.data.ComponentUtil;
import com.ticxo.modelengine.api.utils.data.ItemUtils;
import com.ticxo.modelengine.api.utils.ticker.DualTicker;
import com.ticxo.modelengine.core.listener.ChatListener;
import com.ticxo.modelengine.core.menu.widget.BorderWidget;
import com.ticxo.modelengine.core.menu.widget.CloseWidget;
import com.ticxo.modelengine.core.menu.widget.PaginatorWidget;
import com.ticxo.modelengine.core.menu.widget.TabWidget;
import com.ticxo.modelengine.core.menu.widget.page.AbstractModelButton;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class EditEntityScreen extends AbstractScreen {
   private final Entity selected;
   private final TabWidget tab;

   public EditEntityScreen(AbstractScreen rootScreen, Player viewer, @NotNull Entity selected) {
      super(viewer, "Editing: " + selected.getName(), 6);
      this.selected = selected;
      this.addWidget(new BorderWidget());
      this.tab = new TabWidget();
      this.tab.addTab(new EditEntityScreen.SettingsTab());
      this.tab.addTab(new EditEntityScreen.ModelTab());
      this.addWidget(this.tab);
      this.addWidget(new EditEntityScreen.EntityStatsWidget());
      this.addWidget(new CloseWidget(rootScreen));
   }

   public void openScreen() {
      TabWidget.Tab var2 = this.tab.getSelectedTab();
      if (var2 instanceof EditEntityScreen.ModelTab) {
         EditEntityScreen.ModelTab modelTab = (EditEntityScreen.ModelTab)var2;
         modelTab.updatePage();
      }

      super.openScreen();
   }

   class SettingsTab implements TabWidget.Tab {
      private final ItemStack stack;
      private final EditEntityScreen.SettingsWidget widget;

      public SettingsTab() {
         this.stack = new ItemStack(Material.COMMAND_BLOCK);
         ItemUtils.name(this.stack, Component.text("Entity Settings", ComponentUtil.reset()));
         this.widget = EditEntityScreen.this.new SettingsWidget();
      }

      public ItemStack getItemStack() {
         return this.stack;
      }

      public Widget getWidget() {
         return this.widget;
      }

      public void onSelect() {
      }
   }

   class ModelTab implements TabWidget.Tab {
      private final ItemStack stack;
      private final PaginatorWidget page;

      public ModelTab() {
         this.stack = new ItemStack(Material.ARMOR_STAND);
         ItemUtils.name(this.stack, Component.text("Models", ComponentUtil.reset()));
         this.page = new PaginatorWidget();
      }

      public ItemStack getItemStack() {
         return this.stack;
      }

      public Widget getWidget() {
         return this.page;
      }

      public void onSelect() {
         this.updatePage();
      }

      private void updatePage() {
         this.page.clearButtons();
         ModeledEntity modeledEntity = ModelEngineAPI.getModeledEntity(EditEntityScreen.this.selected);
         if (modeledEntity != null) {
            Iterator var2 = modeledEntity.getModels().values().iterator();

            while(var2.hasNext()) {
               ActiveModel model = (ActiveModel)var2.next();
               EditEntityScreen.ModelTab.ModelButton button = new EditEntityScreen.ModelTab.ModelButton(model);
               this.page.addButton(button);
            }
         }

         this.page.addButton(new EditEntityScreen.ModelTab.AddModelButton());
      }

      class ModelButton extends AbstractModelButton {
         private final ActiveModel model;

         public ModelButton(ActiveModel model) {
            super(model.getBlueprint());
            this.model = model;
         }

         protected void updateItem() {
            ItemUtils.name(this.stack, Component.text(this.blueprint.getName(), ComponentUtil.reset()));
            ItemUtils.lore(this.stack, Component.empty(), Component.text("Model ID: " + this.blueprint.getName(), ComponentUtil.reset()), Component.text("Hitbox: " + this.blueprint.getMainHitbox().toSimpleString(), ComponentUtil.reset()), Component.text("Eye Height: " + this.blueprint.getMainHitbox().toEyeHeightString(), ComponentUtil.reset()), Component.text("Shadow: " + this.blueprint.getShadowRadius(), ComponentUtil.reset()), Component.text("Bone Count: " + this.blueprint.getFlatMap().size(), ComponentUtil.reset()), Component.empty(), Component.text("Shift-click to remove model.", ComponentUtil.color(NamedTextColor.DARK_GRAY)));
         }

         public void onClick(AbstractScreen screen, Player player, int slot, InventoryClickEvent event) {
            if (event.isShiftClick()) {
               ModeledEntity modeledEntity = ModelEngineAPI.getModeledEntity(EditEntityScreen.this.selected);
               if (modeledEntity == null) {
                  return;
               }

               modeledEntity.removeModel(this.model.getBlueprint().getName()).ifPresent(ActiveModel::destroy);
               if (modeledEntity.getModels().isEmpty()) {
                  modeledEntity.setBaseEntityVisible(true);
                  ModelEngineAPI.removeModeledEntity(EditEntityScreen.this.selected);
               }

               ModelTab.this.updatePage();
               EditEntityScreen.this.draw(true);
            } else {
               (new EditModelScreen(screen, player, this.model)).openScreen();
            }

         }
      }

      class AddModelButton implements PaginatorWidget.PageButton {
         private final ItemStack stack;

         public AddModelButton() {
            this.stack = new ItemStack(Material.NETHER_STAR);
            ItemUtils.name(this.stack, Component.text("Add Model", ComponentUtil.reset()));
         }

         public ItemStack getItemStack() {
            return this.stack;
         }

         public void onClick(AbstractScreen screen, Player player, int slot, InventoryClickEvent event) {
            (new AddModelScreen(screen, player, EditEntityScreen.this.selected)).openScreen();
         }
      }
   }

   class EntityStatsWidget implements Widget {
      private static final int SLOT = 53;
      private final ItemStack stack;

      public EntityStatsWidget() {
         this.stack = new ItemStack(Material.OAK_SIGN);
         this.updateStats();
      }

      public ItemStack getItemForSlot(int size, int slot) {
         return slot == 53 ? this.stack : null;
      }

      public void onClick(AbstractScreen screen, Player player, int slot, InventoryClickEvent event) {
         if (slot == 53) {
            if (event.isLeftClick()) {
               this.updateStats();
               EditEntityScreen.this.markSlotDirty(53);
            } else if (event.isRightClick()) {
               player.teleport(EditEntityScreen.this.selected.getLocation());
            }
         }

      }

      private void updateStats() {
         int var10000 = EditEntityScreen.this.selected.getLocation().getBlockX();
         String location = var10000 + ", " + EditEntityScreen.this.selected.getLocation().getBlockY() + ", " + EditEntityScreen.this.selected.getLocation().getBlockZ();
         ItemUtils.name(this.stack, Component.text("Stats", ComponentUtil.reset().decoration(TextDecoration.BOLD, true)));
         ItemStack var2 = this.stack;
         Component[] var10001 = new Component[]{Component.empty(), Component.text("Type: ", ComponentUtil.reset()).append(Component.translatable(EditEntityScreen.this.selected.getType().getTranslationKey())), null, null, null, null, null, null};
         UUID var10004 = EditEntityScreen.this.selected.getUniqueId();
         var10001[2] = Component.text("UUID: " + var10004, ComponentUtil.reset());
         var10001[3] = Component.text("Position: " + location, ComponentUtil.reset());
         var10001[4] = Component.text("Status: " + (EditEntityScreen.this.selected.isDead() ? "Dead" : "Alive"), ComponentUtil.reset());
         var10001[5] = Component.empty();
         var10001[6] = Component.text("Left click to refresh stats.", ComponentUtil.color(NamedTextColor.DARK_GRAY));
         var10001[7] = Component.text("Right click to teleport to entity.", ComponentUtil.color(NamedTextColor.DARK_GRAY));
         ItemUtils.lore(var2, var10001);
      }
   }

   class SettingsWidget implements Widget {
      private static final int VISIBLE_SLOT = 29;
      private static final int SAVED_SLOT = 30;
      private static final int ROT_LOCK_SLOT = 31;
      private static final int RENDER_RADIUS_SLOT = 32;
      private static final int STEP_HEIGHT_SLOT = 33;
      private final ModeledEntity modeledEntity;
      private final ItemStack visible;
      private final ItemStack saved;
      private final ItemStack rotLock;
      private final ItemStack renderRadius;
      private final ItemStack stepHeight;

      public SettingsWidget() {
         this.modeledEntity = ModelEngineAPI.getOrCreateModeledEntity(EditEntityScreen.this.selected, (me) -> {
            me.setBaseEntityVisible(false);
         });
         this.visible = new ItemStack(Material.ENDER_EYE);
         ItemStack var10000 = this.visible;
         boolean var10001 = this.modeledEntity.isBaseEntityVisible();
         ItemUtils.name(var10000, Component.text("Base Entity Visible: " + var10001, ComponentUtil.color(this.modeledEntity.isBaseEntityVisible() ? NamedTextColor.GREEN : NamedTextColor.RED)));
         ItemUtils.lore(this.visible, Component.empty(), Component.text("Is the actual entity visible.", ComponentUtil.reset()));
         this.saved = new ItemStack(Material.CHEST);
         var10000 = this.saved;
         var10001 = this.modeledEntity.shouldBeSaved();
         ItemUtils.name(var10000, Component.text("Model Persistent: " + var10001, ComponentUtil.color(this.modeledEntity.shouldBeSaved() ? NamedTextColor.GREEN : NamedTextColor.RED)));
         ItemUtils.lore(this.saved, Component.empty(), Component.text("Should the models be saved on unload.", ComponentUtil.reset()));
         this.rotLock = new ItemStack(Material.COMPASS);
         var10000 = this.rotLock;
         var10001 = this.modeledEntity.isModelRotationLocked();
         ItemUtils.name(var10000, Component.text("Rotation Locked: " + var10001, ComponentUtil.color(this.modeledEntity.isModelRotationLocked() ? NamedTextColor.GREEN : NamedTextColor.RED)));
         ItemUtils.lore(this.rotLock, Component.empty(), Component.text("Can the models rotate with the entity.", ComponentUtil.reset()));
         this.renderRadius = new ItemStack(Material.SPYGLASS);
         ItemUtils.name(this.renderRadius, Component.text("Render Radius: " + this.modeledEntity.getBase().getRenderRadius(), ComponentUtil.reset()));
         ItemUtils.lore(this.renderRadius, Component.empty(), Component.text("The render radius of the entity in blocks.", ComponentUtil.reset()));
         this.stepHeight = new ItemStack(Material.STONE_STAIRS);
         DecimalFormat var2 = MiscUtils.FORMATTER;
         ItemUtils.name(this.stepHeight, Component.text("Step Height: " + var2.format(this.modeledEntity.getBase().getMaxStepHeight()), ComponentUtil.reset()));
         ItemUtils.lore(this.stepHeight, Component.empty(), Component.text("The step height of the entity in blocks.", ComponentUtil.reset()));
      }

      public ItemStack getItemForSlot(int size, int slot) {
         ItemStack var10000;
         switch(slot) {
         case 29:
            var10000 = this.visible;
            break;
         case 30:
            var10000 = this.saved;
            break;
         case 31:
            var10000 = this.rotLock;
            break;
         case 32:
            var10000 = this.renderRadius;
            break;
         case 33:
            var10000 = this.stepHeight;
            break;
         default:
            var10000 = null;
         }

         return var10000;
      }

      public void onClick(AbstractScreen screen, Player player, int slot, InventoryClickEvent event) {
         ItemStack var10000;
         boolean var10001;
         switch(slot) {
         case 29:
            this.modeledEntity.setBaseEntityVisible(!this.modeledEntity.isBaseEntityVisible());
            var10000 = this.visible;
            var10001 = this.modeledEntity.isBaseEntityVisible();
            ItemUtils.name(var10000, Component.text("Base Entity Visible: " + var10001, ComponentUtil.color(this.modeledEntity.isBaseEntityVisible() ? NamedTextColor.GREEN : NamedTextColor.RED)));
            screen.markSlotDirty(29);
            break;
         case 30:
            this.modeledEntity.setSaved(!this.modeledEntity.shouldBeSaved());
            var10000 = this.saved;
            var10001 = this.modeledEntity.shouldBeSaved();
            ItemUtils.name(var10000, Component.text("Model Persistent: " + var10001, ComponentUtil.color(this.modeledEntity.shouldBeSaved() ? NamedTextColor.GREEN : NamedTextColor.RED)));
            screen.markSlotDirty(30);
            break;
         case 31:
            this.modeledEntity.setModelRotationLocked(!this.modeledEntity.isModelRotationLocked());
            var10000 = this.rotLock;
            var10001 = this.modeledEntity.isModelRotationLocked();
            ItemUtils.name(var10000, Component.text("Rotation Locked: " + var10001, ComponentUtil.color(this.modeledEntity.isModelRotationLocked() ? NamedTextColor.GREEN : NamedTextColor.RED)));
            screen.markSlotDirty(31);
            break;
         case 32:
            player.closeInventory();
            ComponentUtil.sendMessage(player, Component.text("[ModelEngine] Enter render radius:", ComponentUtil.color(NamedTextColor.AQUA)));
            ChatListener.fetch(player, (s) -> {
               try {
                  this.modeledEntity.getBase().setRenderRadius(Integer.parseInt(s));
                  ComponentUtil.sendMessage(player, Component.text("[ModelEngine] Set render radius of model to " + s + ".", ComponentUtil.color(NamedTextColor.GREEN)));
                  screen.markSlotDirty(32);
                  DualTicker.queueSyncTask(EditEntityScreen.this::openScreen);
                  return true;
               } catch (NumberFormatException var5) {
                  ComponentUtil.sendMessage(player, Component.text("[ModelEngine] \"" + s + "\" is not an integer. Try again.", ComponentUtil.color(NamedTextColor.RED)));
                  return false;
               }
            });
            break;
         case 33:
            player.closeInventory();
            ComponentUtil.sendMessage(player, Component.text("[ModelEngine] Enter step height:", ComponentUtil.color(NamedTextColor.AQUA)));
            ChatListener.fetch(player, (s) -> {
               try {
                  this.modeledEntity.getBase().setMaxStepHeight(Double.parseDouble(s));
                  ComponentUtil.sendMessage(player, Component.text("[ModelEngine] Set step height of model to " + s + ".", ComponentUtil.color(NamedTextColor.GREEN)));
                  screen.markSlotDirty(33);
                  DualTicker.queueSyncTask(EditEntityScreen.this::openScreen);
                  return true;
               } catch (NumberFormatException var5) {
                  ComponentUtil.sendMessage(player, Component.text("[ModelEngine] \"" + s + "\" is not a number. Try again.", ComponentUtil.color(NamedTextColor.RED)));
                  return false;
               }
            });
         }

      }
   }
}
