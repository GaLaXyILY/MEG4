package com.ticxo.modelengine.api.utils.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Iterator;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Consumer;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

public class ItemUtils {
   public static byte[] encodeItemStack(ItemStack item) {
      try {
         ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

         byte[] var3;
         try {
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            try {
               dataOutput.writeObject(item);
               var3 = outputStream.toByteArray();
            } catch (Throwable var8) {
               try {
                  dataOutput.close();
               } catch (Throwable var7) {
                  var8.addSuppressed(var7);
               }

               throw var8;
            }

            dataOutput.close();
         } catch (Throwable var9) {
            try {
               outputStream.close();
            } catch (Throwable var6) {
               var9.addSuppressed(var6);
            }

            throw var9;
         }

         outputStream.close();
         return var3;
      } catch (IOException var10) {
         throw new RuntimeException(var10);
      }
   }

   public static String encodeItemStackToString(ItemStack item) {
      return encode(encodeItemStack(item));
   }

   public static ItemStack decodeItemStack(byte[] buf) {
      try {
         ByteArrayInputStream inputStream = new ByteArrayInputStream(buf);

         ItemStack var3;
         try {
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

            try {
               var3 = (ItemStack)dataInput.readObject();
            } catch (Throwable var8) {
               try {
                  dataInput.close();
               } catch (Throwable var7) {
                  var8.addSuppressed(var7);
               }

               throw var8;
            }

            dataInput.close();
         } catch (Throwable var9) {
            try {
               inputStream.close();
            } catch (Throwable var6) {
               var9.addSuppressed(var6);
            }

            throw var9;
         }

         inputStream.close();
         return var3;
      } catch (ClassNotFoundException | IOException var10) {
         throw new RuntimeException(var10);
      }
   }

   public static ItemStack decodeItemStack(String data) {
      return decodeItemStack(decode(data));
   }

   public static String encode(byte[] buf) {
      return Base64.getEncoder().encodeToString(buf);
   }

   public static byte[] decode(String src) {
      try {
         return Base64.getDecoder().decode(src);
      } catch (IllegalArgumentException var4) {
         try {
            return Base64Coder.decodeLines(src);
         } catch (Exception var3) {
            throw var4;
         }
      }
   }

   public static void name(ItemStack stack, Component component) {
      meta(stack, (meta) -> {
         meta.setDisplayNameComponent(ComponentUtil.base(component));
      });
   }

   public static void lore(ItemStack stack, Component... components) {
      ArrayList<BaseComponent[]> list = new ArrayList();
      Component[] var3 = components;
      int var4 = components.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         Component component = var3[var5];
         if (component != null) {
            list.add(ComponentUtil.base(component));
         }
      }

      meta(stack, (meta) -> {
         meta.setLoreComponents(list);
      });
   }

   public static void lore(ItemStack stack, Collection<Component> components) {
      ArrayList<BaseComponent[]> list = new ArrayList();
      Iterator var3 = components.iterator();

      while(var3.hasNext()) {
         Component component = (Component)var3.next();
         if (component != null) {
            list.add(ComponentUtil.base(component));
         }
      }

      meta(stack, (meta) -> {
         meta.setLoreComponents(list);
      });
   }

   public static <T extends ItemMeta> void meta(ItemStack stack, Consumer<T> metaConsumer) {
      try {
         ItemMeta meta = stack.getItemMeta();
         metaConsumer.accept(meta);
         stack.setItemMeta(meta);
      } catch (ClassCastException var3) {
         var3.printStackTrace();
      }

   }
}
