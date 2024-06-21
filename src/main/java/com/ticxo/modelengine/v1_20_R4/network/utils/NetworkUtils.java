/* Decompiler 1250ms, total 1656ms, lines 288 */
package com.ticxo.modelengine.v1_20_R4.network.utils;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.nms.network.ProtectedPacket;
import com.ticxo.modelengine.v1_20_R4.network.NetworkHandlerImpl;
import com.ticxo.modelengine.v1_20_R4.network.utils.Packets.PacketSupplier;
import io.netty.buffer.Unpooled;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.PacketListenerPlayOut;
import net.minecraft.network.protocol.game.PacketPlayOutEntityTeleport;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class NetworkUtils {
   public static PacketDataSerializer createByteBuf() {
      return new PacketDataSerializer(Unpooled.buffer());
   }

   public static PacketDataSerializer readData(Packet<?> packet) {
      PacketDataSerializer buf = createByteBuf();
      packet.a(buf);
      return buf;
   }

   public static PacketSupplier createPivotSpawn(int id, UUID uuid, Vector3f pos) {
      return (player) -> {
         return ModelEngineAPI.getPlayerProtocolVersion(player.getUniqueId()) >= 764 ? new PacketPlayOutSpawnEntity(id, uuid, (double)pos.x, (double)pos.y - 0.5D, (double)pos.z, 0.0F, 0.0F, EntityTypes.c, 0, Vec3D.b, 0.0D) : new PacketPlayOutSpawnEntity(id, uuid, (double)pos.x, (double)pos.y - 0.375D, (double)pos.z, 0.0F, 0.0F, EntityTypes.c, 0, Vec3D.b, 0.0D);
      };
   }

   public static PacketSupplier createPivotTeleport(int id, Vector3f pos) {
      PacketDataSerializer buf = createByteBuf();
      buf.c(id);
      buf.a((double)pos.x);
      buf.a((double)pos.y - 0.375D);
      buf.a((double)pos.z);
      buf.k(0);
      buf.k(0);
      buf.a(false);
      PacketPlayOutEntityTeleport tpHigher = new PacketPlayOutEntityTeleport(buf);
      buf = createByteBuf();
      buf.c(id);
      buf.a((double)pos.x);
      buf.a((double)pos.y - 0.5D);
      buf.a((double)pos.z);
      buf.k(0);
      buf.k(0);
      buf.a(false);
      PacketPlayOutEntityTeleport tpLower = new PacketPlayOutEntityTeleport(buf);
      return (player) -> {
         return ModelEngineAPI.getPlayerProtocolVersion(player.getUniqueId()) >= 764 ? tpLower : tpHigher;
      };
   }

   public static void send(Player target, @Nullable Packet<PacketListenerPlayOut> packet) {
      if (packet != null) {
         NetworkHandlerImpl handler = NetworkHandlerImpl.instance;
         if (handler.isBatching()) {
            handler.appendPacket(target.getUniqueId(), packet);
         } else {
            ProtectedPacket wrapped = new ProtectedPacket(packet);
            ModelEngineAPI.getNetworkHandler().getPipeline(target).ifPresent((pipeline) -> {
               pipeline.writeAndFlush(wrapped);
            });
         }

      }
   }

   public static void send(Set<Player> targets, @Nullable Packet<PacketListenerPlayOut> packet) {
      if (packet != null) {
         NetworkHandlerImpl handler = NetworkHandlerImpl.instance;
         if (handler.isBatching()) {
            Iterator var3 = targets.iterator();

            while(var3.hasNext()) {
               Player player = (Player)var3.next();
               handler.appendPacket(player.getUniqueId(), packet);
            }
         } else {
            ProtectedPacket wrapped = new ProtectedPacket(packet);
            Iterator var7 = targets.iterator();

            while(var7.hasNext()) {
               Player player = (Player)var7.next();
               ModelEngineAPI.getNetworkHandler().getPipeline(player).ifPresent((pipeline) -> {
                  pipeline.writeAndFlush(wrapped);
               });
            }
         }

      }
   }

   public static void send(Set<Player> targets, @Nullable Packet<PacketListenerPlayOut> packet, Predicate<Player> predicate) {
      if (packet != null) {
         NetworkHandlerImpl handler = NetworkHandlerImpl.instance;
         if (handler.isBatching()) {
            Iterator var4 = targets.iterator();

            while(var4.hasNext()) {
               Player player = (Player)var4.next();
               if (predicate.test(player)) {
                  handler.appendPacket(player.getUniqueId(), packet);
               }
            }
         } else {
            ProtectedPacket wrapped = new ProtectedPacket(packet);
            Iterator var8 = targets.iterator();

            while(var8.hasNext()) {
               Player player = (Player)var8.next();
               if (predicate.test(player)) {
                  ModelEngineAPI.getNetworkHandler().getPipeline(player).ifPresent((pipeline) -> {
                     pipeline.writeAndFlush(wrapped);
                  });
               }
            }
         }

      }
   }

   public static void sendRaw(Player target, @Nullable Packet<PacketListenerPlayOut> packet) {
      if (packet != null) {
         ModelEngineAPI.getNetworkHandler().getPipeline(target).ifPresent((pipeline) -> {
            pipeline.writeAndFlush(packet);
         });
      }
   }

   public static void sendRaw(Set<Player> targets, @Nullable Packet<PacketListenerPlayOut> packet) {
      if (packet != null) {
         Iterator var2 = targets.iterator();

         while(var2.hasNext()) {
            Player player = (Player)var2.next();
            ModelEngineAPI.getNetworkHandler().getPipeline(player).ifPresent((pipeline) -> {
               pipeline.writeAndFlush(packet);
            });
         }

      }
   }

   public static void sendRaw(Set<Player> targets, @Nullable Packet<PacketListenerPlayOut> packet, Predicate<Player> predicate) {
      if (packet != null) {
         Iterator var3 = targets.iterator();

         while(var3.hasNext()) {
            Player player = (Player)var3.next();
            if (predicate.test(player)) {
               ModelEngineAPI.getNetworkHandler().getPipeline(player).ifPresent((pipeline) -> {
                  pipeline.writeAndFlush(packet);
               });
            }
         }

      }
   }

   public static void sendBundled(Player target, Packets collection) {
      if (!collection.isEmpty()) {
         Collection<Packet<PacketListenerPlayOut>> packets = collection.compile(target);
         NetworkHandlerImpl handler = NetworkHandlerImpl.instance;
         if (handler.isBatching()) {
            handler.appendPackets(target.getUniqueId(), packets);
         } else {
            ProtectedPacket wrapped = new ProtectedPacket(new ClientboundBundlePacket(packets));
            handler.getPipeline(target).ifPresent((pipeline) -> {
               pipeline.writeAndFlush(wrapped);
            });
         }

      }
   }

   public static void sendBundled(Set<Player> targets, Packets collection) {
      if (!collection.isEmpty()) {
         NetworkHandlerImpl handler = NetworkHandlerImpl.instance;
         Iterator var3;
         Player player;
         if (handler.isBatching()) {
            var3 = targets.iterator();

            while(var3.hasNext()) {
               player = (Player)var3.next();
               handler.appendPackets(player.getUniqueId(), collection.compile(player));
            }
         } else {
            var3 = targets.iterator();

            while(var3.hasNext()) {
               player = (Player)var3.next();
               ModelEngineAPI.getNetworkHandler().getPipeline(player).ifPresent((pipeline) -> {
                  ProtectedPacket wrapped = new ProtectedPacket(new ClientboundBundlePacket(collection.compile(player)));
                  pipeline.writeAndFlush(wrapped);
               });
            }
         }

      }
   }

   public static void sendBundled(Set<Player> targets, Packets collection, Predicate<Player> predicate) {
      if (!collection.isEmpty()) {
         NetworkHandlerImpl handler = NetworkHandlerImpl.instance;
         Iterator var4;
         Player player;
         if (handler.isBatching()) {
            var4 = targets.iterator();

            while(var4.hasNext()) {
               player = (Player)var4.next();
               if (predicate.test(player)) {
                  handler.appendPackets(player.getUniqueId(), collection.compile(player));
               }
            }
         } else {
            var4 = targets.iterator();

            while(var4.hasNext()) {
               player = (Player)var4.next();
               if (predicate.test(player)) {
                  ModelEngineAPI.getNetworkHandler().getPipeline(player).ifPresent((pipeline) -> {
                     ProtectedPacket wrapped = new ProtectedPacket(new ClientboundBundlePacket(collection.compile(player)));
                     pipeline.writeAndFlush(wrapped);
                  });
               }
            }
         }

      }
   }

   public static void sendBundledRaw(Player target, Packets collection) {
      if (!collection.isEmpty()) {
         ClientboundBundlePacket packet = new ClientboundBundlePacket(collection.compile(target));
         ModelEngineAPI.getNetworkHandler().getPipeline(target).ifPresent((pipeline) -> {
            pipeline.writeAndFlush(packet);
         });
      }
   }

   public static void sendBundledRaw(Set<Player> targets, Packets collection) {
      if (!collection.isEmpty()) {
         Iterator var2 = targets.iterator();

         while(var2.hasNext()) {
            Player player = (Player)var2.next();
            ModelEngineAPI.getNetworkHandler().getPipeline(player).ifPresent((pipeline) -> {
               ClientboundBundlePacket packet = new ClientboundBundlePacket(collection.compile(player));
               pipeline.writeAndFlush(packet);
            });
         }

      }
   }

   public static void sendBundledRaw(Set<Player> targets, Packets collection, Predicate<Player> predicate) {
      if (!collection.isEmpty()) {
         Iterator var3 = targets.iterator();

         while(var3.hasNext()) {
            Player player = (Player)var3.next();
            if (predicate.test(player)) {
               ModelEngineAPI.getNetworkHandler().getPipeline(player).ifPresent((pipeline) -> {
                  ClientboundBundlePacket packet = new ClientboundBundlePacket(collection.compile(player));
                  pipeline.writeAndFlush(packet);
               });
            }
         }

      }
   }
}