package com.ticxo.modelengine.v1_20_R3.network;

import com.google.common.collect.Maps;
import com.ticxo.modelengine.api.nms.network.NetworkHandler;
import com.ticxo.modelengine.api.utils.ReflectionUtils;
import com.ticxo.modelengine.v1_20_R3.NMSFields;
import com.ticxo.modelengine.v1_20_R3.network.utils.Bundler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.SharedConstants;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketListenerPlayOut;
import net.minecraft.server.network.PlayerConnection;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class NetworkHandlerImpl implements NetworkHandler {
   public static NetworkHandlerImpl instance;
   private final Map<UUID, ChannelPipeline> pipelines = Maps.newConcurrentMap();
   private final Map<UUID, Bundler> bundles = Maps.newConcurrentMap();
   private boolean isBatching;

   public NetworkHandlerImpl() {
      if (instance != null) {
         throw new IllegalStateException("Network handler already initialized");
      } else {
         instance = this;
      }
   }

   public int getProtocolVersion() {
      return SharedConstants.c();
   }

   public Optional<ChannelPipeline> getPipeline(Player player) {
      return Optional.ofNullable((ChannelPipeline)this.pipelines.get(player.getUniqueId()));
   }

   public void removePipeline(Player player) {
      this.pipelines.remove(player.getUniqueId());
   }

   public void injectChannel(Player player) {
      ModelEngineChannelHandler handler = new ModelEngineChannelHandler(player);
      PlayerConnection listener = ((CraftPlayer)player).getHandle().c;
      NetworkManager connection = (NetworkManager)ReflectionUtils.get(listener, NMSFields.SERVER_COMMON_PACKET_LISTENER_IMPL_connection);
      ChannelPipeline pipeline = connection.n.pipeline();
      this.pipelines.put(player.getUniqueId(), pipeline);
      this.bundles.put(player.getUniqueId(), new Bundler());
      Iterator var6 = pipeline.toMap().keySet().iterator();

      while(var6.hasNext()) {
         String name = (String)var6.next();
         if (pipeline.get(name) instanceof NetworkManager) {
            pipeline.addBefore(name, "model_engine_packet_handler", handler);
            break;
         }
      }

   }

   public void ejectChannel(Player player) {
      PlayerConnection listener = ((CraftPlayer)player).getHandle().c;
      NetworkManager connection = (NetworkManager)ReflectionUtils.get(listener, NMSFields.SERVER_COMMON_PACKET_LISTENER_IMPL_connection);
      Channel channel = connection.n;
      channel.eventLoop().submit(() -> {
         channel.pipeline().remove("model_engine_packet_handler");
         return null;
      });
      this.removePipeline(player);
      this.bundles.remove(player.getUniqueId());
   }

   public void startBatch() {
      this.isBatching = true;
   }

   public void endBatch() {
      Iterator var1 = this.bundles.entrySet().iterator();

      while(var1.hasNext()) {
         Entry<UUID, Bundler> entry = (Entry)var1.next();
         ChannelPipeline pipeline = (ChannelPipeline)this.pipelines.get(entry.getKey());
         if (pipeline != null) {
            Bundler bundler = (Bundler)entry.getValue();
            Objects.requireNonNull(pipeline);
            bundler.bundle(pipeline::writeAndFlush);
            bundler.clear();
         }
      }

      this.isBatching = false;
   }

   public void appendPacket(UUID uuid, Packet<PacketListenerPlayOut> packet) {
      Bundler bundler = (Bundler)this.bundles.get(uuid);
      if (bundler != null) {
         bundler.appendPacket(packet);
      }

   }

   public void appendPackets(UUID uuid, Collection<Packet<PacketListenerPlayOut>> collection) {
      Bundler bundler = (Bundler)this.bundles.get(uuid);
      if (bundler != null) {
         bundler.appendPacket(collection);
      }

   }

   public boolean isBatching() {
      return this.isBatching;
   }
}
