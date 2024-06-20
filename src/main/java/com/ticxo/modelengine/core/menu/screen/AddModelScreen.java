package com.ticxo.modelengine.core.menu.screen;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.generator.blueprint.ModelBlueprint;
import com.ticxo.modelengine.api.menu.AbstractScreen;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModelRegistry;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.utils.data.ComponentUtil;
import com.ticxo.modelengine.api.utils.data.ItemUtils;
import com.ticxo.modelengine.core.menu.widget.BasicItemWidget;
import com.ticxo.modelengine.core.menu.widget.BorderWidget;
import com.ticxo.modelengine.core.menu.widget.CloseWidget;
import com.ticxo.modelengine.core.menu.widget.PaginatorWidget;
import com.ticxo.modelengine.core.menu.widget.page.AbstractModelButton;
import java.util.Iterator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class AddModelScreen extends AbstractScreen {
   private final Entity selected;
   private final PaginatorWidget page;

   public AddModelScreen(AbstractScreen rootScreen, Player viewer, Entity selected) {
      super(viewer, "Add Model", 6);
      this.selected = selected;
      this.addWidget(new BorderWidget());
      this.page = new PaginatorWidget();
      this.addWidget(this.page);
      ModelRegistry registry = ModelEngineAPI.getAPI().getModelRegistry();
      ItemStack statSign = new ItemStack(Material.OAK_SIGN);
      ItemUtils.name(statSign, Component.text("Stats", ComponentUtil.reset().decoration(TextDecoration.BOLD, true)));
      ItemUtils.lore(statSign, Component.empty(), Component.text("Models: " + registry.getKeys().size(), ComponentUtil.reset()));
      this.addWidget(new BasicItemWidget(53, statSign));
      this.addWidget(new CloseWidget(rootScreen));
   }

   public void openScreen() {
      this.updatePage();
      super.openScreen();
   }

   private void updatePage() {
      this.page.clearButtons();
      ModelRegistry registry = ModelEngineAPI.getAPI().getModelRegistry();
      ModeledEntity modeledEntity = ModelEngineAPI.getModeledEntity(this.selected);
      Iterator var3 = registry.getOrderedId().iterator();

      while(true) {
         String modelId;
         do {
            if (!var3.hasNext()) {
               return;
            }

            modelId = (String)var3.next();
         } while(modeledEntity != null && modeledEntity.getModel(modelId).isPresent());

         ModelBlueprint blueprint = (ModelBlueprint)registry.get(modelId);
         AddModelScreen.ModelButton button = new AddModelScreen.ModelButton(blueprint);
         this.page.addButton(button);
      }
   }

   class ModelButton extends AbstractModelButton {
      public ModelButton(ModelBlueprint blueprint) {
         super(blueprint);
      }

      public void onClick(AbstractScreen screen, Player player, int slot, InventoryClickEvent event) {
         ModeledEntity modeledEntity = ModelEngineAPI.getOrCreateModeledEntity(AddModelScreen.this.selected, (me) -> {
            me.setBaseEntityVisible(false);
         });
         ActiveModel activeModel = ModelEngineAPI.createActiveModel(this.blueprint);
         modeledEntity.addModel(activeModel, true).ifPresent(ActiveModel::destroy);
         AddModelScreen.this.updatePage();
         AddModelScreen.this.draw(true);
      }
   }
}
