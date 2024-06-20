package com.ticxo.modelengine.core.menu.screen;

import com.ticxo.modelengine.api.animation.BlueprintAnimation;
import com.ticxo.modelengine.api.animation.handler.AnimationHandler;
import com.ticxo.modelengine.api.animation.property.IAnimationProperty;
import com.ticxo.modelengine.api.animation.property.SimpleProperty;
import com.ticxo.modelengine.api.generator.blueprint.BlueprintBone;
import com.ticxo.modelengine.api.generator.blueprint.ModelBlueprint;
import com.ticxo.modelengine.api.menu.AbstractScreen;
import com.ticxo.modelengine.api.menu.Widget;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.bone.ModelBone;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehavior;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehaviorType;
import com.ticxo.modelengine.api.utils.MiscUtils;
import com.ticxo.modelengine.api.utils.config.ConfigProperty;
import com.ticxo.modelengine.api.utils.data.ComponentUtil;
import com.ticxo.modelengine.api.utils.data.ItemUtils;
import com.ticxo.modelengine.api.utils.ticker.DualTicker;
import com.ticxo.modelengine.core.animation.handler.StateMachineHandler;
import com.ticxo.modelengine.core.listener.ChatListener;
import com.ticxo.modelengine.core.menu.widget.BorderWidget;
import com.ticxo.modelengine.core.menu.widget.CloseWidget;
import com.ticxo.modelengine.core.menu.widget.CompositeWidget;
import com.ticxo.modelengine.core.menu.widget.PaginatorWidget;
import com.ticxo.modelengine.core.menu.widget.TabWidget;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public class EditModelScreen extends AbstractScreen {
   private final ActiveModel model;
   private final TabWidget tab;

   public EditModelScreen(AbstractScreen rootScreen, Player viewer, ActiveModel model) {
      super(viewer, model.getBlueprint().getName(), 6);
      this.model = model;
      this.addWidget(new BorderWidget());
      this.tab = new TabWidget();
      this.tab.addTab(new EditModelScreen.SettingsTab());
      this.tab.addTab(new EditModelScreen.BoneTab());
      this.tab.addTab(new EditModelScreen.AnimationTab());
      this.addWidget(this.tab);
      this.addWidget(new CloseWidget(rootScreen));
   }

   public void onTick() {
      TabWidget.Tab var2 = this.tab.getSelectedTab();
      if (var2 instanceof EditModelScreen.AnimationTab) {
         EditModelScreen.AnimationTab animationTab = (EditModelScreen.AnimationTab)var2;
         animationTab.onTick();
      }

      super.onTick();
   }

   class SettingsTab implements TabWidget.Tab {
      private final ItemStack stack;
      private final EditModelScreen.SettingsWidget widget;

      public SettingsTab() {
         this.stack = new ItemStack(Material.COMMAND_BLOCK);
         ItemUtils.name(this.stack, Component.text("Model Settings", ComponentUtil.reset()));
         this.widget = EditModelScreen.this.new SettingsWidget();
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

   class BoneTab implements TabWidget.Tab {
      private final ItemStack stack;
      private final PaginatorWidget page;

      public BoneTab() {
         this.stack = new ItemStack(Material.BONE);
         ItemUtils.name(this.stack, Component.text("Bones", ComponentUtil.reset()));
         this.page = new PaginatorWidget();
      }

      public ItemStack getItemStack() {
         return this.stack;
      }

      public Widget getWidget() {
         return this.page;
      }

      public void onSelect() {
         this.page.clearButtons();
         Iterator var1 = EditModelScreen.this.model.getBones().values().iterator();

         while(var1.hasNext()) {
            ModelBone bone = (ModelBone)var1.next();
            this.page.addButton(new EditModelScreen.BoneButton(bone));
         }

      }
   }

   class AnimationTab implements TabWidget.Tab {
      private final ItemStack stack;
      private final CompositeWidget composite;
      private final PaginatorWidget page;
      private final EditModelScreen.AnimationTab.PriorityButton priority;

      public AnimationTab() {
         this.stack = new ItemStack(Material.ARMOR_STAND);
         ItemUtils.name(this.stack, Component.text("Animations", ComponentUtil.reset()));
         this.page = new PaginatorWidget();
         if (EditModelScreen.this.model.getAnimationHandler() instanceof StateMachineHandler) {
            this.priority = new EditModelScreen.AnimationTab.PriorityButton();
            this.composite = new CompositeWidget(this.page, this.priority);
         } else {
            this.priority = null;
            this.composite = null;
         }

      }

      public ItemStack getItemStack() {
         return this.stack;
      }

      public Widget getWidget() {
         return (Widget)(this.composite != null ? this.composite : this.page);
      }

      public void onSelect() {
         this.page.clearButtons();
         ModelBlueprint blueprint = EditModelScreen.this.model.getBlueprint();
         Map<String, BlueprintAnimation> pairs = blueprint.getAnimations();
         Iterator var3 = pairs.entrySet().iterator();

         while(var3.hasNext()) {
            Entry<String, BlueprintAnimation> entry = (Entry)var3.next();
            this.page.addButton(new EditModelScreen.AnimationTab.AnimationButton((BlueprintAnimation)entry.getValue()));
         }

      }

      protected void onTick() {
         int i = 19;

         while(i <= 43) {
            switch(i % 9) {
            default:
               PaginatorWidget.PageButton var3 = this.page.getButton(i);
               if (var3 instanceof EditModelScreen.AnimationTab.AnimationButton) {
                  EditModelScreen.AnimationTab.AnimationButton button = (EditModelScreen.AnimationTab.AnimationButton)var3;
                  if (button.update(false)) {
                     EditModelScreen.this.markSlotDirty(i);
                  }
               }
            case 0:
            case 8:
               ++i;
            }
         }

      }

      class PriorityButton implements Widget {
         private static final int SLOT = 51;
         private final ItemStack stack;
         private int priority = 0;

         public PriorityButton() {
            this.stack = new ItemStack(Material.COMPARATOR);
            ItemUtils.name(this.stack, Component.text("Priority: " + this.priority, ComponentUtil.reset()));
            ItemUtils.lore(this.stack, ((TextComponent)((TextComponent)((TextComponent)Component.empty().style(ComponentUtil.reset())).append(Component.text("<<< Left Click"))).append(Component.text(" | "))).append(Component.text("Right Click >>>")));
         }

         public ItemStack getItemForSlot(int size, int slot) {
            return 51 == slot ? this.stack : null;
         }

         public void onClick(AbstractScreen screen, Player player, int slot, InventoryClickEvent event) {
            if (slot == 51) {
               if (event.getClick().isLeftClick()) {
                  --this.priority;
               } else if (event.getClick().isRightClick()) {
                  ++this.priority;
               }

               AnimationTab.this.page.refreshPage(screen);
               ItemUtils.name(this.stack, Component.text("Priority: " + this.priority, ComponentUtil.reset()));
               screen.markSlotsDirty(51);
            }
         }
      }

      class AnimationButton implements PaginatorWidget.PageButton {
         private final BlueprintAnimation animation;
         private ItemStack stack;
         private IAnimationProperty trackedProperty;
         private int lastHash;

         public AnimationButton(BlueprintAnimation animation) {
            this.animation = animation;
            this.update(true);
         }

         public ItemStack getItemStack() {
            return this.stack;
         }

         public void onClick(AbstractScreen screen, Player player, int slot, InventoryClickEvent event) {
            AnimationHandler handler = EditModelScreen.this.model.getAnimationHandler();
            if (handler instanceof StateMachineHandler) {
               StateMachineHandler stateMachineHandler = (StateMachineHandler)handler;
               if (stateMachineHandler.isPlayingAnimation(AnimationTab.this.priority.priority, this.animation.getName())) {
                  stateMachineHandler.stopAnimation(AnimationTab.this.priority.priority, this.animation.getName());
               } else {
                  stateMachineHandler.playAnimation(AnimationTab.this.priority.priority, new SimpleProperty(EditModelScreen.this.model, this.animation), true);
               }
            } else if (handler.isPlayingAnimation(this.animation.getName())) {
               handler.stopAnimation(this.animation.getName());
            } else {
               handler.playAnimation(new SimpleProperty(EditModelScreen.this.model, this.animation), true);
            }

         }

         public boolean update(boolean force) {
            AnimationHandler handler = EditModelScreen.this.model.getAnimationHandler();
            IAnimationProperty var10000;
            if (handler instanceof StateMachineHandler) {
               StateMachineHandler stateMachineHandler = (StateMachineHandler)handler;
               var10000 = stateMachineHandler.getAnimation(AnimationTab.this.priority.priority, this.animation.getName());
            } else {
               var10000 = handler.getAnimation(this.animation.getName());
            }

            IAnimationProperty property = var10000;
            ItemStack var7;
            Component[] var10001;
            DecimalFormat var10004;
            if (property == null) {
               if (!force && this.trackedProperty == null) {
                  return false;
               }

               this.trackedProperty = null;
               this.stack = new ItemStack(Material.RED_CONCRETE);
               ItemUtils.name(this.stack, Component.text(this.animation.getName(), ComponentUtil.color(NamedTextColor.RED)));
               var7 = this.stack;
               var10001 = new Component[]{Component.empty(), Component.text("Animation:", ComponentUtil.reset()), Component.text("- Animation ID: " + this.animation.getName(), ComponentUtil.reset()), Component.text("- Loop Mode: " + this.animation.getLoopMode(), ComponentUtil.reset()), Component.text("- Override: " + this.animation.isOverride(), ComponentUtil.reset()), null};
               var10004 = MiscUtils.FORMATTER;
               var10001[5] = Component.text("- Length: " + var10004.format(this.animation.getLength()) + "s", ComponentUtil.reset());
               ItemUtils.lore(var7, var10001);
            } else {
               if (!force && this.trackedProperty != null) {
                  int hash = this.trackedProperty.hashCode();
                  if (hash == this.lastHash) {
                     return false;
                  }

                  this.lastHash = hash;
               }

               this.trackedProperty = property;
               this.stack = new ItemStack(Material.LIME_CONCRETE);
               ItemUtils.name(this.stack, Component.text(this.animation.getName(), ComponentUtil.color(NamedTextColor.GREEN)));
               var7 = this.stack;
               var10001 = new Component[]{Component.empty(), Component.text("Animation:", ComponentUtil.reset()), Component.text("- Animation ID: " + this.animation.getName(), ComponentUtil.reset()), Component.text("- Loop Mode: " + this.animation.getLoopMode(), ComponentUtil.reset()), Component.text("- Override: " + this.animation.isOverride(), ComponentUtil.reset()), null, null, null, null, null, null, null, null, null};
               var10004 = MiscUtils.FORMATTER;
               var10001[5] = Component.text("- Length: " + var10004.format(this.animation.getLength()) + "s", ComponentUtil.reset());
               var10001[6] = Component.empty();
               var10001[7] = Component.text("Property:", ComponentUtil.reset());
               IAnimationProperty.Phase var8 = property.getPhase();
               String var5;
               if (property.getPhase() == IAnimationProperty.Phase.PLAY) {
                  DecimalFormat var10005 = MiscUtils.FORMATTER;
                  var5 = " (" + var10005.format(property.getTime()) + "s)";
               } else {
                  var5 = "";
               }

               var10001[8] = Component.text("- Phase: " + var8 + var5, ComponentUtil.reset());
               var10001[9] = Component.text("- Forced Loop Mode: " + property.getForceLoopMode(), ComponentUtil.reset());
               var10001[10] = Component.text("- Forced Override: " + property.isForceOverride(), ComponentUtil.reset());
               var10004 = MiscUtils.FORMATTER;
               var10001[11] = Component.text("- Speed: " + var10004.format(property.getSpeed()) + "x", ComponentUtil.reset());
               var10004 = MiscUtils.FORMATTER;
               var10001[12] = Component.text("- Lerp-In: " + var10004.format(property.getLerpIn()) + "s", ComponentUtil.reset());
               var10004 = MiscUtils.FORMATTER;
               var10001[13] = Component.text("- Lerp-Out: " + var10004.format(property.getLerpOut()) + "s", ComponentUtil.reset());
               ItemUtils.lore(var7, var10001);
            }

            return true;
         }
      }
   }

   static class BoneButton implements PaginatorWidget.PageButton {
      private final ModelBone bone;
      private ItemStack stack;

      public BoneButton(ModelBone bone) {
         this.bone = bone;
         this.updateItem();
      }

      public ItemStack getItemStack() {
         return this.stack;
      }

      public void onClick(AbstractScreen screen, Player player, int slot, InventoryClickEvent event) {
         this.bone.setVisible(!this.bone.isVisible());
         this.updateItem();
         screen.markSlotDirty(slot);
      }

      private void updateItem() {
         BlueprintBone blueprintBone = this.bone.getBlueprintBone();
         if (blueprintBone.getDataId() != 0) {
            this.stack = ConfigProperty.ITEM_MODEL.getBaseItem().create(this.bone.getDefaultTint(), blueprintBone.getDataId());
            this.stack.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_DYE});
         } else {
            this.stack = new ItemStack(Material.BONE);
         }

         if (this.bone.isEnchanted()) {
            this.stack.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);
            this.stack.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
         }

         ItemUtils.name(this.stack, Component.text(this.bone.getUniqueBoneId(), ComponentUtil.reset()));
         ArrayList<Component> lore = new ArrayList();
         lore.add(Component.empty());
         lore.add(Component.text("Bone ID: " + this.bone.getBoneId(), ComponentUtil.reset()));
         if (this.bone.getParent() != null) {
            lore.add(Component.text("Parent: " + this.bone.getParent().getBoneId(), ComponentUtil.reset()));
         }

         lore.add(Component.text("Visible: " + this.bone.isVisible(), ComponentUtil.reset()));
         int defColor = this.bone.getDefaultTint().asRGB();
         int dmgColor = this.bone.getDamageTint().asRGB();
         lore.add(Component.text("Default Color: ", ComponentUtil.reset()).append(Component.text("#" + Integer.toString(defColor, 16).toUpperCase(Locale.ENGLISH), Style.style(TextColor.color(defColor)))));
         lore.add(Component.text("Damage Color: ", ComponentUtil.reset()).append(Component.text("#" + Integer.toString(dmgColor, 16).toUpperCase(Locale.ENGLISH), Style.style(TextColor.color(dmgColor)))));
         lore.add(Component.text("Enchanted: " + this.bone.isEnchanted(), ComponentUtil.reset()));
         boolean hasBehavior = false;
         Iterator var6 = this.bone.getImmutableBoneBehaviors().entrySet().iterator();

         while(var6.hasNext()) {
            Entry<BoneBehaviorType<?>, BoneBehavior> behavior = (Entry)var6.next();
            if (!((BoneBehavior)behavior.getValue()).isHidden()) {
               if (!hasBehavior) {
                  hasBehavior = true;
                  lore.add(Component.text("Behavior: ", ComponentUtil.reset()));
               }

               lore.add(Component.text("- " + ((BoneBehaviorType)behavior.getKey()).getId(), ComponentUtil.reset()));
            }
         }

         ItemUtils.lore(this.stack, (Collection)lore);
      }
   }

   class SettingsWidget implements Widget {
      private static final int RENDER_SCALE_SLOT = 21;
      private static final int HITBOX_SCALE_SLOT = 22;
      private static final int HITBOX_VISIBLE_SLOT = 23;
      private static final int SHADOW_VISIBLE_SLOT = 30;
      private static final int DEFAULT_TINT_SLOT = 31;
      private static final int DAMAGE_TINT_SLOT = 32;
      private static final int DAMAGE_TINT_VISIBLE_SLOT = 39;
      private static final int PITCH_LOCK_SLOT = 40;
      private static final int YAW_LOCK_SLOT = 41;
      private final ItemStack renderScale;
      private final ItemStack hitboxScale;
      private final ItemStack hitboxVisible;
      private final ItemStack shadowVisible;
      private final ItemStack defaultTint;
      private final ItemStack damageTint;
      private final ItemStack damageTintVisible;
      private final ItemStack pitchLock;
      private final ItemStack yawLock;

      public SettingsWidget() {
         this.renderScale = new ItemStack(Material.SLIME_BALL);
         this.hitboxScale = new ItemStack(Material.MAGMA_CREAM);
         this.hitboxVisible = new ItemStack(Material.SHULKER_SHELL);
         this.shadowVisible = new ItemStack(Material.BLACK_DYE);
         this.defaultTint = new ItemStack(Material.WHITE_DYE);
         this.damageTint = new ItemStack(Material.RED_DYE);
         this.damageTintVisible = new ItemStack(Material.DIAMOND_SWORD);
         ItemUtils.meta(this.damageTintVisible, (meta) -> {
            meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES});
         });
         this.pitchLock = new ItemStack(Material.CLOCK);
         this.yawLock = new ItemStack(Material.COMPASS);
         this.update();
      }

      public ItemStack getItemForSlot(int size, int slot) {
         ItemStack var10000;
         switch(slot) {
         case 21:
            var10000 = this.renderScale;
            break;
         case 22:
            var10000 = this.hitboxScale;
            break;
         case 23:
            var10000 = this.hitboxVisible;
            break;
         case 24:
         case 25:
         case 26:
         case 27:
         case 28:
         case 29:
         case 33:
         case 34:
         case 35:
         case 36:
         case 37:
         case 38:
         default:
            var10000 = null;
            break;
         case 30:
            var10000 = this.shadowVisible;
            break;
         case 31:
            var10000 = this.defaultTint;
            break;
         case 32:
            var10000 = this.damageTint;
            break;
         case 39:
            var10000 = this.damageTintVisible;
            break;
         case 40:
            var10000 = this.pitchLock;
            break;
         case 41:
            var10000 = this.yawLock;
         }

         return var10000;
      }

      public void onClick(AbstractScreen screen, Player player, int slot, InventoryClickEvent event) {
         switch(slot) {
         case 21:
            player.closeInventory();
            ComponentUtil.sendMessage(player, Component.text("[ModelEngine] Enter render scale:", ComponentUtil.color(NamedTextColor.AQUA)));
            ChatListener.fetch(player, (s) -> {
               try {
                  EditModelScreen.this.model.setScale(Double.parseDouble(s));
                  ComponentUtil.sendMessage(player, Component.text("[ModelEngine] Set render scale of model to " + s + ".", ComponentUtil.color(NamedTextColor.GREEN)));
                  this.update(slot);
                  DualTicker.queueSyncTask(EditModelScreen.this::openScreen);
                  return true;
               } catch (NumberFormatException var5) {
                  ComponentUtil.sendMessage(player, Component.text("[ModelEngine] \"" + s + "\" is not a number. Try again.", ComponentUtil.color(NamedTextColor.RED)));
                  return false;
               }
            });
            break;
         case 22:
            player.closeInventory();
            ComponentUtil.sendMessage(player, Component.text("[ModelEngine] Enter hitbox scale:", ComponentUtil.color(NamedTextColor.AQUA)));
            ChatListener.fetch(player, (s) -> {
               try {
                  EditModelScreen.this.model.setHitboxScale(Double.parseDouble(s));
                  ComponentUtil.sendMessage(player, Component.text("[ModelEngine] Set hitbox scale of model to " + s + ".", ComponentUtil.color(NamedTextColor.GREEN)));
                  this.update(slot);
                  DualTicker.queueSyncTask(EditModelScreen.this::openScreen);
                  return true;
               } catch (NumberFormatException var5) {
                  ComponentUtil.sendMessage(player, Component.text("[ModelEngine] \"" + s + "\" is not a number. Try again.", ComponentUtil.color(NamedTextColor.RED)));
                  return false;
               }
            });
            break;
         case 23:
            EditModelScreen.this.model.setHitboxVisible(!EditModelScreen.this.model.isHitboxVisible());
         case 24:
         case 25:
         case 26:
         case 27:
         case 28:
         case 29:
         case 33:
         case 34:
         case 35:
         case 36:
         case 37:
         case 38:
         default:
            break;
         case 30:
            EditModelScreen.this.model.setShadowVisible(!EditModelScreen.this.model.isShadowVisible());
            break;
         case 31:
            player.closeInventory();
            ComponentUtil.sendMessage(player, Component.text("[ModelEngine] Enter default color in hex:", ComponentUtil.color(NamedTextColor.AQUA)));
            ChatListener.fetch(player, (s) -> {
               try {
                  if (s.startsWith("#")) {
                     s = s.substring(1);
                  }

                  int color = Integer.parseInt(s, 16);
                  EditModelScreen.this.model.setDefaultTint(Color.fromRGB(color));
                  ComponentUtil.sendMessage(player, ((TextComponent)Component.text("[ModelEngine] Set default color of model to ", ComponentUtil.color(NamedTextColor.GREEN)).append(Component.text("#" + s.toUpperCase(Locale.ENGLISH), ComponentUtil.color(TextColor.color(color))))).append(Component.text(".")));
                  this.update(slot);
                  DualTicker.queueSyncTask(EditModelScreen.this::openScreen);
                  return true;
               } catch (NumberFormatException var5) {
                  ComponentUtil.sendMessage(player, Component.text("[ModelEngine] \"" + s + "\" is not a valid color. Try again.", ComponentUtil.color(NamedTextColor.RED)));
                  return false;
               }
            });
            break;
         case 32:
            player.closeInventory();
            ComponentUtil.sendMessage(player, Component.text("[ModelEngine] Enter damage color in hex:", ComponentUtil.color(NamedTextColor.AQUA)));
            ChatListener.fetch(player, (s) -> {
               try {
                  if (s.startsWith("#")) {
                     s = s.substring(1);
                  }

                  int color = Integer.parseInt(s, 16);
                  EditModelScreen.this.model.setDamageTint(Color.fromRGB(color));
                  ComponentUtil.sendMessage(player, ((TextComponent)Component.text("[ModelEngine] Set damage color of model to ", ComponentUtil.color(NamedTextColor.GREEN)).append(Component.text("#" + s.toUpperCase(Locale.ENGLISH), ComponentUtil.color(TextColor.color(color))))).append(Component.text(".")));
                  this.update(slot);
                  DualTicker.queueSyncTask(EditModelScreen.this::openScreen);
                  return true;
               } catch (NumberFormatException var5) {
                  ComponentUtil.sendMessage(player, Component.text("[ModelEngine] \"" + s + "\" is not a valid color. Try again.", ComponentUtil.color(NamedTextColor.RED)));
                  return false;
               }
            });
            break;
         case 39:
            EditModelScreen.this.model.setCanHurt(!EditModelScreen.this.model.canHurt());
            break;
         case 40:
            EditModelScreen.this.model.setLockPitch(!EditModelScreen.this.model.isLockPitch());
            break;
         case 41:
            EditModelScreen.this.model.setLockYaw(!EditModelScreen.this.model.isLockYaw());
         }

         this.update(slot);
         EditModelScreen.this.markSlotDirty(slot);
      }

      public void update() {
         this.update(21);
         this.update(22);
         this.update(23);
         this.update(30);
         this.update(31);
         this.update(32);
         this.update(39);
         this.update(40);
         this.update(41);
      }

      public void update(int slot) {
         ItemStack var10000;
         boolean var10001;
         DecimalFormat var2;
         switch(slot) {
         case 21:
            var2 = MiscUtils.FORMATTER;
            ItemUtils.name(this.renderScale, Component.text("Render Scale: " + var2.format((double)EditModelScreen.this.model.getScale().x), ComponentUtil.reset()));
            break;
         case 22:
            var2 = MiscUtils.FORMATTER;
            ItemUtils.name(this.hitboxScale, Component.text("Hitbox Scale: " + var2.format((double)EditModelScreen.this.model.getHitboxScale().x), ComponentUtil.reset()));
            break;
         case 23:
            var10000 = this.hitboxVisible;
            var10001 = EditModelScreen.this.model.isHitboxVisible();
            ItemUtils.name(var10000, Component.text("Hitbox Visible: " + var10001, ComponentUtil.color(EditModelScreen.this.model.isHitboxVisible() ? NamedTextColor.GREEN : NamedTextColor.RED)));
         case 24:
         case 25:
         case 26:
         case 27:
         case 28:
         case 29:
         case 33:
         case 34:
         case 35:
         case 36:
         case 37:
         case 38:
         default:
            break;
         case 30:
            var10000 = this.shadowVisible;
            var10001 = EditModelScreen.this.model.isShadowVisible();
            ItemUtils.name(var10000, Component.text("Shadow Visible: " + var10001, ComponentUtil.color(EditModelScreen.this.model.isShadowVisible() ? NamedTextColor.GREEN : NamedTextColor.RED)));
            break;
         case 31:
            ItemUtils.name(this.defaultTint, Component.text("Default Tint: #" + Integer.toHexString(EditModelScreen.this.model.getDefaultTint().asARGB()).toUpperCase(Locale.ENGLISH).substring(2), ComponentUtil.color(TextColor.color(EditModelScreen.this.model.getDefaultTint().asRGB()))));
            break;
         case 32:
            ItemUtils.name(this.damageTint, Component.text("Damage Tint: #" + Integer.toHexString(EditModelScreen.this.model.getDamageTint().asARGB()).toUpperCase(Locale.ENGLISH).substring(2), ComponentUtil.color(TextColor.color(EditModelScreen.this.model.getDamageTint().asRGB()))));
            break;
         case 39:
            var10000 = this.damageTintVisible;
            var10001 = EditModelScreen.this.model.canHurt();
            ItemUtils.name(var10000, Component.text("Damage Tint Visible: " + var10001, ComponentUtil.color(EditModelScreen.this.model.canHurt() ? NamedTextColor.GREEN : NamedTextColor.RED)));
            break;
         case 40:
            var10000 = this.pitchLock;
            var10001 = EditModelScreen.this.model.isLockPitch();
            ItemUtils.name(var10000, Component.text("Pitch Lock: " + var10001, ComponentUtil.color(EditModelScreen.this.model.isLockPitch() ? NamedTextColor.GREEN : NamedTextColor.RED)));
            break;
         case 41:
            var10000 = this.yawLock;
            var10001 = EditModelScreen.this.model.isLockYaw();
            ItemUtils.name(var10000, Component.text("Yaw Lock: " + var10001, ComponentUtil.color(EditModelScreen.this.model.isLockYaw() ? NamedTextColor.GREEN : NamedTextColor.RED)));
         }

      }
   }
}
