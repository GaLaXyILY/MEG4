package com.ticxo.modelengine.api.model.bone.type;

import com.ticxo.modelengine.api.utils.data.io.DataIO;
import com.ticxo.modelengine.api.utils.data.io.SavedData;
import com.ticxo.modelengine.api.utils.logger.TLogger;
import java.util.UUID;
import java.util.function.Supplier;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.ItemDisplay.ItemDisplayTransform;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public interface HeldItem {
   ItemStack EMPTY = new ItemStack(Material.AIR);

   Vector3f getLocation();

   Quaternionf getRotation();

   ItemDisplayTransform getDisplay();

   void setDisplay(ItemDisplayTransform var1);

   HeldItem.ItemStackSupplier getItemProvider();

   void setItemProvider(Supplier<ItemStack> var1);

   void setItemProvider(HeldItem.ItemStackSupplier var1);

   void clearItemProvider();

   ItemStack getItem();

   public static class EquipmentSupplier implements HeldItem.ItemStackSupplier {
      private LivingEntity target;
      private EquipmentSlot slot;

      public EquipmentSupplier() {
      }

      public ItemStack supply() {
         if (this.target != null && this.slot != null) {
            EntityEquipment eq = this.target.getEquipment();
            return eq == null ? HeldItem.EMPTY : eq.getItem(this.slot);
         } else {
            return HeldItem.EMPTY;
         }
      }

      public void save(SavedData data) {
         data.putString("type", "equipment");
         data.putUUID("target", this.target.getUniqueId());
         data.put("slot", this.slot.name());
      }

      public void load(SavedData data) {
         UUID uuid = data.getUUID("target");
         String slotString = data.getString("slot");
         if (uuid != null && slotString != null) {
            Entity entity = Bukkit.getEntity(uuid);
            if (entity instanceof LivingEntity) {
               LivingEntity livingEntity = (LivingEntity)entity;

               try {
                  this.slot = EquipmentSlot.valueOf(slotString);
               } catch (Throwable var7) {
                  TLogger.error(1, "Failed to load EquipmentSupplier: Invalid slot " + slotString + ".");
                  return;
               }

               this.target = livingEntity;
            } else {
               TLogger.error(1, "Failed to load EquipmentSupplier: Target entity does not exist or is not LivingEntity.");
            }
         }
      }

      public EquipmentSupplier(LivingEntity target, EquipmentSlot slot) {
         this.target = target;
         this.slot = slot;
      }
   }

   public static class StaticItemStackSupplier implements HeldItem.ItemStackSupplier {
      private ItemStack itemStack;

      public StaticItemStackSupplier() {
      }

      public ItemStack supply() {
         return this.itemStack;
      }

      public void save(SavedData data) {
         data.putString("type", "static");
         data.putItemStack("item", this.itemStack);
      }

      public void load(SavedData data) {
         this.itemStack = data.getItemStack("item", HeldItem.EMPTY);
      }

      public StaticItemStackSupplier(ItemStack itemStack) {
         this.itemStack = itemStack;
      }
   }

   public static class TemporaryItemStackSupplier implements HeldItem.ItemStackSupplier {
      private final Supplier<ItemStack> stackSupplier;

      public ItemStack supply() {
         return (ItemStack)this.stackSupplier.get();
      }

      public void save(SavedData data) {
      }

      public void load(SavedData data) {
      }

      public TemporaryItemStackSupplier(Supplier<ItemStack> stackSupplier) {
         this.stackSupplier = stackSupplier;
      }
   }

   public interface ItemStackSupplier extends DataIO {
      ItemStack supply();
   }
}
