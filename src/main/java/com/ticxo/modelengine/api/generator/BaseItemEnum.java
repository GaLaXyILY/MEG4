package com.ticxo.modelengine.api.generator;

import java.util.function.BiConsumer;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.PotionMeta;

public enum BaseItemEnum {
   FILLED_MAP(BaseItemEnum.Cons.MAP_CONSUMER, Material.FILLED_MAP),
   LEATHER_BOOTS(BaseItemEnum.Cons.LEATHER_CONSUMER, Material.LEATHER_BOOTS),
   LEATHER_CHESTPLATE(BaseItemEnum.Cons.LEATHER_CONSUMER, Material.LEATHER_CHESTPLATE),
   LEATHER_HELMET(BaseItemEnum.Cons.LEATHER_CONSUMER, Material.LEATHER_HELMET),
   LEATHER_HORSE_ARMOR(BaseItemEnum.Cons.LEATHER_CONSUMER, Material.LEATHER_HORSE_ARMOR),
   LEATHER_LEGGINGS(BaseItemEnum.Cons.LEATHER_CONSUMER, Material.LEATHER_LEGGINGS),
   LINGERING_POTION(BaseItemEnum.Cons.POTION_CONSUMER, Material.LINGERING_POTION),
   POTION(BaseItemEnum.Cons.POTION_CONSUMER, Material.POTION),
   SPLASH_POTION(BaseItemEnum.Cons.POTION_CONSUMER, Material.SPLASH_POTION),
   TIPPED_ARROW(BaseItemEnum.Cons.POTION_CONSUMER, Material.TIPPED_ARROW);

   private final BiConsumer<ItemMeta, Color> metaConsumer;
   private final Material material;

   public static BaseItemEnum get(String value) {
      try {
         return valueOf(value);
      } catch (IllegalArgumentException var2) {
         return LEATHER_HORSE_ARMOR;
      }
   }

   public static BaseItemEnum fromMaterial(Material material) {
      BaseItemEnum var10000;
      switch(material) {
      case FILLED_MAP:
         var10000 = FILLED_MAP;
         break;
      case LEATHER_BOOTS:
         var10000 = LEATHER_BOOTS;
         break;
      case LEATHER_CHESTPLATE:
         var10000 = LEATHER_CHESTPLATE;
         break;
      case LEATHER_HELMET:
         var10000 = LEATHER_HELMET;
         break;
      case LEATHER_HORSE_ARMOR:
         var10000 = LEATHER_HORSE_ARMOR;
         break;
      case LEATHER_LEGGINGS:
         var10000 = LEATHER_LEGGINGS;
         break;
      case LINGERING_POTION:
         var10000 = LINGERING_POTION;
         break;
      case POTION:
         var10000 = POTION;
         break;
      case SPLASH_POTION:
         var10000 = SPLASH_POTION;
         break;
      case TIPPED_ARROW:
         var10000 = TIPPED_ARROW;
         break;
      default:
         var10000 = null;
      }

      return var10000;
   }

   public void color(ItemMeta meta, Color color) {
      this.metaConsumer.accept(meta, color);
   }

   public ItemStack create() {
      return new ItemStack(this.material);
   }

   public ItemStack create(Color color, int data) {
      ItemStack stack = this.create();
      ItemMeta meta = stack.getItemMeta();
      meta.setCustomModelData(data);
      this.color(meta, color);
      stack.setItemMeta(meta);
      return stack;
   }

   private BaseItemEnum(BiConsumer<ItemMeta, Color> metaConsumer, Material material) {
      this.metaConsumer = metaConsumer;
      this.material = material;
   }

   public Material getMaterial() {
      return this.material;
   }

   // $FF: synthetic method
   private static BaseItemEnum[] $values() {
      return new BaseItemEnum[]{FILLED_MAP, LEATHER_BOOTS, LEATHER_CHESTPLATE, LEATHER_HELMET, LEATHER_HORSE_ARMOR, LEATHER_LEGGINGS, LINGERING_POTION, POTION, SPLASH_POTION, TIPPED_ARROW};
   }

   private static class Cons {
      private static final BiConsumer<ItemMeta, Color> MAP_CONSUMER = (meta, color) -> {
         MapMeta map = (MapMeta)meta;
         map.setColor(color);
      };
      private static final BiConsumer<ItemMeta, Color> LEATHER_CONSUMER = (meta, color) -> {
         LeatherArmorMeta leather = (LeatherArmorMeta)meta;
         leather.setColor(color);
      };
      private static final BiConsumer<ItemMeta, Color> POTION_CONSUMER = (meta, color) -> {
         PotionMeta potion = (PotionMeta)meta;
         potion.setColor(color);
      };
   }
}
