package com.ticxo.modelengine.v1_20_R4.entity;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.network.syncher.DataWatcherSerializer;
import net.minecraft.world.entity.Entity;
import org.bukkit.craftbukkit.v1_20_R4.entity.CraftEntity;

public class EntityUtils {
   public static Entity nms(org.bukkit.entity.Entity entity) {
      return ((CraftEntity)entity).getHandle();
   }

   public static <T> void writeData(PacketDataSerializer byteBuf, int id, DataWatcherSerializer<T> serializer, T val) {
      int serializedId = DataWatcherRegistry.b(serializer);
      byteBuf.k(id);
      byteBuf.c(serializedId);
      serializer.a(byteBuf, val);
   }

   public static void writeDataUnsafe(PacketDataSerializer byteBuf, int id, DataWatcherSerializer serializer, Object val) {
      int serializedId = DataWatcherRegistry.b(serializer);
      byteBuf.k(id);
      byteBuf.c(serializedId);
      serializer.a(byteBuf, val);
   }
}
