package com.ticxo.modelengine.v1_20_R2.network.utils;

import com.ticxo.modelengine.api.utils.data.NullableHashSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketListenerPlayOut;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Packets extends LinkedHashSet<Packets.PacketSupplier> {
   public Packets(int initialCapacity, float loadFactor) {
      super(initialCapacity, loadFactor);
   }

   public Packets(int initialCapacity) {
      super(initialCapacity);
   }

   public Packets() {
   }

   public Packets(@NotNull Collection<? extends Packets.PacketSupplier> c) {
      super(c);
   }

   public boolean add(Packets.PacketSupplier supplier) {
      return supplier == null ? false : super.add(supplier);
   }

   public boolean add(Packet<PacketListenerPlayOut> packet) {
      return this.add((player) -> {
         return packet;
      });
   }

   public Collection<Packet<PacketListenerPlayOut>> compile(Player player) {
      NullableHashSet<Packet<PacketListenerPlayOut>> set = new NullableHashSet();
      Iterator var3 = this.iterator();

      while(var3.hasNext()) {
         Packets.PacketSupplier supplier = (Packets.PacketSupplier)var3.next();
         set.add(supplier.supply(player));
      }

      return set;
   }

   @FunctionalInterface
   public interface PacketSupplier {
      Packet<PacketListenerPlayOut> supply(Player var1);
   }
}
