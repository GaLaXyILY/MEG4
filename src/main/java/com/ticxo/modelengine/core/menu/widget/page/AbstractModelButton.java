package com.ticxo.modelengine.core.menu.widget.page;

import com.ticxo.modelengine.api.generator.blueprint.BlueprintBone;
import com.ticxo.modelengine.api.generator.blueprint.ModelBlueprint;
import com.ticxo.modelengine.api.utils.config.ConfigProperty;
import com.ticxo.modelengine.api.utils.data.ComponentUtil;
import com.ticxo.modelengine.api.utils.data.ItemUtils;
import com.ticxo.modelengine.core.menu.widget.PaginatorWidget;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public abstract class AbstractModelButton implements PaginatorWidget.PageButton {
   protected final ModelBlueprint blueprint;
   protected final ItemStack stack;

   public AbstractModelButton(ModelBlueprint blueprint) {
      this.blueprint = blueprint;
      BlueprintBone headBone = (BlueprintBone)blueprint.getFlatMap().get("head");
      if (headBone != null && headBone.getDataId() != 0) {
         this.stack = ConfigProperty.ITEM_MODEL.getBaseItem().create(Color.WHITE, headBone.getDataId());
         this.stack.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_DYE});
      } else {
         this.stack = new ItemStack(Material.ARMOR_STAND);
      }

      this.updateItem();
   }

   protected void updateItem() {
      ItemUtils.name(this.stack, Component.text(this.blueprint.getName(), ComponentUtil.reset()));
      ItemUtils.lore(this.stack, Component.empty(), Component.text("Model ID: " + this.blueprint.getName(), ComponentUtil.reset()), Component.text("Hitbox: " + this.blueprint.getMainHitbox().toSimpleString(), ComponentUtil.reset()), Component.text("Eye Height: " + this.blueprint.getMainHitbox().toEyeHeightString(), ComponentUtil.reset()), Component.text("Shadow: " + this.blueprint.getShadowRadius(), ComponentUtil.reset()), Component.text("Bone Count: " + this.blueprint.getFlatMap().size(), ComponentUtil.reset()));
   }

   public ItemStack getItemStack() {
      return this.stack;
   }
}
