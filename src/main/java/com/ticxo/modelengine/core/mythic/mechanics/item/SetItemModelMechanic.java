package com.ticxo.modelengine.core.mythic.mechanics.item;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.generator.blueprint.BlueprintBone;
import com.ticxo.modelengine.api.generator.blueprint.ModelBlueprint;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.model.bone.BoneBehaviorTypes;
import com.ticxo.modelengine.api.model.bone.type.HeldItem;
import com.ticxo.modelengine.api.utils.config.ConfigProperty;
import com.ticxo.modelengine.core.mythic.MythicUtils;
import com.ticxo.modelengine.core.mythic.utils.MythicMechanic;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderInt;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import io.lumine.mythic.bukkit.adapters.BukkitItemStack;
import io.lumine.mythic.bukkit.utils.items.ItemFactory;
import io.lumine.mythic.core.items.MythicItem;
import java.util.Optional;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

@MythicMechanic(
   name = "setitemmodel",
   aliases = {}
)
public class SetItemModelMechanic implements ITargetedEntitySkill {
   private final PlaceholderString modelId;
   private final PlaceholderString partId;
   private final PlaceholderString strMat;
   private final PlaceholderInt data;
   private final PlaceholderString color;
   private final boolean enchanted;

   public SetItemModelMechanic(MythicLineConfig config) {
      this.modelId = config.getPlaceholderString(new String[]{"m", "mid", "model", "modelid"}, (String)null, new String[0]);
      this.partId = config.getPlaceholderString(new String[]{"b", "bone", "p", "pid", "part", "partid"}, (String)null, new String[0]);
      this.strMat = config.getPlaceholderString(new String[]{"i", "item", "mat", "material"}, (String)null, new String[0]);
      this.data = config.getPlaceholderInteger(new String[]{"data"}, 0, new String[0]);
      this.color = config.getPlaceholderString(new String[]{"color"}, (String)null, new String[0]);
      this.enchanted = config.getBoolean(new String[]{"enchant"}, false);
   }

   public SkillResult castAtEntity(SkillMetadata meta, AbstractEntity target) {
      ModeledEntity model = ModelEngineAPI.getModeledEntity(target.getUniqueId());
      if (model == null) {
         return SkillResult.CONDITION_FAILED;
      } else {
         String modelId = MythicUtils.getOrNullLowercase(this.modelId, meta, target);
         model.getModel(modelId).ifPresent((activeModel) -> {
            String partId = MythicUtils.getOrNullLowercase(this.partId, meta, target);
            activeModel.getBone(partId).ifPresent((modelBone) -> {
               modelBone.getBoneBehavior(BoneBehaviorTypes.ITEM).ifPresent((heldItem) -> {
                  ItemStack stack = this.getItem(meta, target).build();
                  ((HeldItem)heldItem).setItemProvider((HeldItem.ItemStackSupplier)(new HeldItem.StaticItemStackSupplier(stack)));
               });
            });
         });
         return SkillResult.SUCCESS;
      }
   }

   private ItemFactory getItem(SkillMetadata meta, AbstractEntity target) {
      String strMat = MythicUtils.getOrNull(this.strMat, meta, target);
      int data = this.data.get(meta, target);
      String color = MythicUtils.getOrNull(this.color, meta, target);
      boolean isMI = false;

      ItemFactory itemFactory;
      try {
         if (strMat.contains(":")) {
            String[] mat = strMat.split(":", 2);
            String mId = mat[0];
            String pId = mat[1];
            ModelBlueprint blueprint = MythicUtils.getBlueprintOrNull(mId);
            if (blueprint == null) {
               throw new RuntimeException("Invalid Model ID: " + mId);
            }

            BlueprintBone bone = (BlueprintBone)blueprint.getFlatMap().get(pId);
            if (bone == null) {
               throw new RuntimeException("Invalid Part ID: " + pId);
            }

            int mData = bone.getDataId();
            if (mData == -1) {
               throw new RuntimeException("Invalid Part ID: " + pId + ". Not a renderer bone.");
            }

            itemFactory = BukkitItemStack.of(ConfigProperty.ITEM_MODEL.getBaseItem().getMaterial()).model(mData);
         } else {
            Optional<MythicItem> maybeMI = this.getPlugin().getItemManager().getItem(strMat);
            if (maybeMI.isPresent()) {
               isMI = true;
               itemFactory = (ItemFactory)((MythicItem)maybeMI.get()).generateItemStack(1);
            } else {
               Material mat = Material.valueOf(strMat.toUpperCase());
               itemFactory = BukkitItemStack.of(mat).model(data);
            }
         }
      } catch (Exception var14) {
         var14.printStackTrace();
         itemFactory = BukkitItemStack.of(Material.STONE);
      }

      if (!isMI) {
         if (color != null) {
            itemFactory.color(color);
         }

         if (this.enchanted) {
            itemFactory.enchant(Enchantment.VANISHING_CURSE);
         }
      }

      return itemFactory;
   }
}
