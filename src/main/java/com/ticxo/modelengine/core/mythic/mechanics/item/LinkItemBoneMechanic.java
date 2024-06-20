package com.ticxo.modelengine.core.mythic.mechanics.item;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.model.bone.BoneBehaviorTypes;
import com.ticxo.modelengine.api.model.bone.type.HeldItem;
import com.ticxo.modelengine.core.mythic.MythicUtils;
import com.ticxo.modelengine.core.mythic.utils.MythicMechanic;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import io.lumine.mythic.core.drops.EquipSlot;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlot;

@MythicMechanic(
   name = "linkitembone",
   aliases = {}
)
public class LinkItemBoneMechanic implements ITargetedEntitySkill {
   private final PlaceholderString modelId;
   private final PlaceholderString partId;
   private final PlaceholderString slot;

   public LinkItemBoneMechanic(MythicLineConfig mlc) {
      this.modelId = mlc.getPlaceholderString(new String[]{"m", "mid", "model", "modelid"}, (String)null, new String[0]);
      this.partId = mlc.getPlaceholderString(new String[]{"b", "bone", "p", "pid", "part", "partid"}, (String)null, new String[0]);
      this.slot = mlc.getPlaceholderString(new String[]{"s", "slot"}, (String)null, new String[0]);
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
                  String slotString = MythicUtils.getOrNull(this.slot, meta, target);
                  if (slotString == null) {
                     ((HeldItem)heldItem).clearItemProvider();
                  } else {
                     EquipmentSlot var10000;
                     switch(EquipSlot.of(slotString)) {
                     case HEAD:
                        var10000 = EquipmentSlot.HEAD;
                        break;
                     case CHEST:
                        var10000 = EquipmentSlot.CHEST;
                        break;
                     case LEGS:
                        var10000 = EquipmentSlot.LEGS;
                        break;
                     case FEET:
                        var10000 = EquipmentSlot.FEET;
                        break;
                     case HAND:
                        var10000 = EquipmentSlot.HAND;
                        break;
                     case OFFHAND:
                        var10000 = EquipmentSlot.OFF_HAND;
                        break;
                     case NONE:
                        var10000 = null;
                        break;
                     default:
                        throw new IncompatibleClassChangeError();
                     }

                     EquipmentSlot bukkitSlot = var10000;
                     if (bukkitSlot == null) {
                        ((HeldItem)heldItem).clearItemProvider();
                     } else {
                        Entity patt2596$temp = target.getBukkitEntity();
                        if (patt2596$temp instanceof LivingEntity) {
                           LivingEntity livingEntity = (LivingEntity)patt2596$temp;
                           if (livingEntity.getEquipment() == null) {
                              return;
                           }

                           ((HeldItem)heldItem).setItemProvider((HeldItem.ItemStackSupplier)(new HeldItem.EquipmentSupplier(livingEntity, bukkitSlot)));
                        }

                     }
                  }
               });
            });
         });
         return SkillResult.SUCCESS;
      }
   }
}
