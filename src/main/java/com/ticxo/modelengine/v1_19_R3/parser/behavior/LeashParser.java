package com.ticxo.modelengine.v1_19_R3.parser.behavior;

import com.ticxo.modelengine.api.entity.data.IEntityData;
import com.ticxo.modelengine.api.model.bone.render.BehaviorRendererParser;
import com.ticxo.modelengine.api.model.bone.render.renderer.LeashRenderer;
import com.ticxo.modelengine.v1_19_R3.entity.EntityUtils;
import com.ticxo.modelengine.v1_19_R3.network.utils.NetworkUtils;
import com.ticxo.modelengine.v1_19_R3.network.utils.Packets;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutAttachEntity;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutEntityTeleport;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.entity.Player;
import org.joml.Vector3f;

public class LeashParser implements BehaviorRendererParser<LeashRenderer> {
   public void sendToClients(LeashRenderer renderer) {
      IEntityData data = renderer.getModelRenderer().getActiveModel().getModeledEntity().getBase().getData();
      this.update(data.getTracking().keySet(), renderer);
      this.spawn(data.getStartTracking(), renderer);
      HashSet<Player> stop = new HashSet(data.getStopTracking());
      stop.removeAll(data.getTracking().keySet());
      this.remove(stop, renderer);
   }

   public void destroy(LeashRenderer renderer) {
      IEntityData data = renderer.getActiveModel().getModeledEntity().getBase().getData();
      HashSet<Player> inRange = new HashSet(data.getStartTracking());
      inRange.addAll(data.getTracking().keySet());
      inRange.addAll(data.getStopTracking());
      this.remove(inRange, renderer);
   }

   private void spawn(Set<Player> targets, LeashRenderer renderer) {
      if (!targets.isEmpty()) {
         Packets set = new Packets();
         Iterator var4 = renderer.getRendered().values().iterator();

         LeashRenderer.Leash leash;
         while(var4.hasNext()) {
            leash = (LeashRenderer.Leash)var4.next();
            set.add((Packet)this.spawn(leash));
            set.add((Packet)this.link(leash, true));
            set.add((Packet)this.data(leash));
         }

         var4 = renderer.getSpawnQueue().values().iterator();

         while(var4.hasNext()) {
            leash = (LeashRenderer.Leash)var4.next();
            set.add((Packet)this.spawn(leash));
            set.add((Packet)this.link(leash, true));
            set.add((Packet)this.data(leash));
         }

         NetworkUtils.sendBundled(targets, set);
      }
   }

   private void update(Set<Player> targets, LeashRenderer renderer) {
      if (!targets.isEmpty()) {
         Packets set = new Packets();
         Iterator var4 = renderer.getRendered().values().iterator();

         LeashRenderer.Leash leash;
         while(var4.hasNext()) {
            leash = (LeashRenderer.Leash)var4.next();
            set.add((Packet)this.link(leash, false));
            set.add((Packet)this.move(leash));
         }

         var4 = renderer.getSpawnQueue().values().iterator();

         while(var4.hasNext()) {
            leash = (LeashRenderer.Leash)var4.next();
            set.add((Packet)this.spawn(leash));
            set.add((Packet)this.link(leash, true));
            set.add((Packet)this.data(leash));
         }

         Map<String, LeashRenderer.Leash> destroy = renderer.getDestroyQueue();
         if (!destroy.isEmpty()) {
            PacketDataSerializer buf = NetworkUtils.createByteBuf();
            buf.d(destroy.size());
            destroy.forEach((s, item) -> {
               buf.d(item.getId());
            });
            set.add((Packet)(new PacketPlayOutEntityDestroy(buf)));
         }

         NetworkUtils.sendBundled(targets, set);
      }
   }

   private void remove(Set<Player> targets, LeashRenderer renderer) {
      if (!targets.isEmpty()) {
         PacketDataSerializer buf = NetworkUtils.createByteBuf();
         Map<String, LeashRenderer.Leash> leashes = renderer.getRendered();
         buf.d(leashes.size());
         leashes.forEach((s, leash) -> {
            buf.d(leash.getId());
         });
         NetworkUtils.send((Set)targets, new PacketPlayOutEntityDestroy(buf));
      }
   }

   private PacketPlayOutSpawnEntity spawn(LeashRenderer.Leash renderer) {
      Vector3f pos = (Vector3f)renderer.getPosition().get();
      return new PacketPlayOutSpawnEntity(renderer.getId(), renderer.getUuid(), (double)pos.x, (double)pos.y, (double)pos.z, 0.0F, 0.0F, EntityTypes.aL, 0, Vec3D.b, 0.0D);
   }

   private PacketPlayOutEntityMetadata data(LeashRenderer.Leash renderer) {
      PacketDataSerializer buf = NetworkUtils.createByteBuf();
      buf.d(renderer.getId());
      EntityUtils.writeData(buf, 0, DataWatcherRegistry.a, (byte)32);
      EntityUtils.writeData(buf, 1, DataWatcherRegistry.b, Integer.MAX_VALUE);
      EntityUtils.writeData(buf, 16, DataWatcherRegistry.b, 0);
      buf.writeByte(255);
      return new PacketPlayOutEntityMetadata(buf);
   }

   private PacketPlayOutEntityTeleport move(LeashRenderer.Leash renderer) {
      if (!renderer.getPosition().isDirty()) {
         return null;
      } else {
         Vector3f pos = (Vector3f)renderer.getPosition().get();
         PacketDataSerializer buf = NetworkUtils.createByteBuf();
         buf.d(renderer.getId());
         buf.writeDouble((double)pos.x);
         buf.writeDouble((double)pos.y);
         buf.writeDouble((double)pos.z);
         buf.writeByte(0);
         buf.writeByte(0);
         buf.writeBoolean(false);
         renderer.getPosition().clearDirty();
         return new PacketPlayOutEntityTeleport(buf);
      }
   }

   private PacketPlayOutAttachEntity link(LeashRenderer.Leash renderer, boolean spawn) {
      if (!spawn && !renderer.getConnected().isDirty()) {
         return null;
      } else {
         PacketDataSerializer buf = NetworkUtils.createByteBuf();
         buf.writeInt(renderer.getId());
         buf.writeInt((Integer)renderer.getConnected().get());
         renderer.getConnected().clearDirty();
         return new PacketPlayOutAttachEntity(buf);
      }
   }
}
