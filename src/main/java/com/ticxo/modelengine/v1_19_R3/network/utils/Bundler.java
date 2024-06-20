package com.ticxo.modelengine.v1_19_R3.network.utils;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.nms.network.ProtectedPacket;
import com.ticxo.modelengine.api.utils.config.ConfigProperty;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.PacketListenerPlayOut;

public class Bundler {
   private static int BUNDLE_SIZE;
   private final List<List<Packet<PacketListenerPlayOut>>> bundles = new ArrayList();

   public void appendPacket(Packet<PacketListenerPlayOut> packet) {
      if (this.bundles.isEmpty()) {
         this.bundles.add(new ArrayList());
      }

      List<Packet<PacketListenerPlayOut>> bundleList = (List)this.bundles.get(this.bundles.size() - 1);
      if (((List)bundleList).size() == BUNDLE_SIZE) {
         bundleList = new ArrayList();
         this.bundles.add(bundleList);
      }

      ((List)bundleList).add(packet);
   }

   public void appendPacket(Collection<Packet<PacketListenerPlayOut>> packets) {
      if (this.bundles.isEmpty()) {
         this.bundles.add(new ArrayList());
      }

      List<Packet<PacketListenerPlayOut>> bundleList = (List)this.bundles.get(this.bundles.size() - 1);
      if (((List)bundleList).size() + packets.size() > BUNDLE_SIZE) {
         bundleList = new ArrayList();
         this.bundles.add(bundleList);
      }

      ((List)bundleList).addAll(packets);
   }

   public void clear() {
      this.bundles.clear();
   }

   public void bundle(Consumer<Object> send) {
      Iterator var2 = this.bundles.iterator();

      while(var2.hasNext()) {
         List<Packet<PacketListenerPlayOut>> list = (List)var2.next();
         ProtectedPacket wrapped = new ProtectedPacket(new ClientboundBundlePacket(list));
         send.accept(wrapped);
      }

   }

   static {
      ModelEngineAPI.getAPI().getConfigManager().registerReferenceUpdate(() -> {
         BUNDLE_SIZE = Math.min(ConfigProperty.BUNDLE_SIZE.getInt(), 4096);
      });
   }
}
