package com.ticxo.modelengine.v1_19_R3.parser.behavior;

import com.ticxo.modelengine.api.entity.data.IEntityData;
import com.ticxo.modelengine.api.model.bone.render.BehaviorRendererParser;
import com.ticxo.modelengine.api.model.bone.render.renderer.MountRenderer;
import com.ticxo.modelengine.api.utils.data.tracker.CollectionDataTracker;
import com.ticxo.modelengine.api.utils.math.TMath;
import com.ticxo.modelengine.v1_19_R3.entity.EntityUtils;
import com.ticxo.modelengine.v1_19_R3.network.utils.NetworkUtils;
import com.ticxo.modelengine.v1_19_R3.network.utils.Packets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutMount;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.network.protocol.game.PacketPlayOutEntity.PacketPlayOutEntityLook;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.entity.Player;
import org.joml.Vector3f;

public class MountParser implements BehaviorRendererParser<MountRenderer> {
   public void sendToClients(MountRenderer renderer) {
      IEntityData data = renderer.getModelRenderer().getActiveModel().getModeledEntity().getBase().getData();
      this.update(data.getTracking().keySet(), renderer);
      this.spawn(data.getStartTracking(), renderer);
      this.remove(data.getStopTracking(), renderer);
   }

   public void destroy(MountRenderer renderer) {
      IEntityData data = renderer.getActiveModel().getModeledEntity().getBase().getData();
      HashSet<Player> inRange = new HashSet(data.getStartTracking());
      inRange.addAll(data.getTracking().keySet());
      inRange.addAll(data.getStopTracking());
      this.remove(inRange, renderer);
   }

   private void spawn(Set<Player> targets, MountRenderer renderer) {
      if (!targets.isEmpty()) {
         Packets set = new Packets();
         Iterator var4 = renderer.getRendered().values().iterator();

         MountRenderer.Mount mount;
         while(var4.hasNext()) {
            mount = (MountRenderer.Mount)var4.next();
            set.add(this.pivotSpawn(mount));
            set.add((Packet)this.pivotData(mount));
            set.add((Packet)this.mountSpawn(mount));
            set.add((Packet)this.mountData(mount));
            set.add((Packet)this.pivotMount(mount));
            set.add((Packet)this.mount(mount));
         }

         var4 = renderer.getSpawnQueue().values().iterator();

         while(var4.hasNext()) {
            mount = (MountRenderer.Mount)var4.next();
            set.add(this.pivotSpawn(mount));
            set.add((Packet)this.pivotData(mount));
            set.add((Packet)this.mountSpawn(mount));
            set.add((Packet)this.mountData(mount));
            set.add((Packet)this.pivotMount(mount));
            set.add((Packet)this.mount(mount));
         }

         NetworkUtils.sendBundled(targets, set);
      }
   }

   private void update(Set<Player> targets, MountRenderer renderer) {
      if (!targets.isEmpty()) {
         Packets set = new Packets();
         Iterator var4 = renderer.getRendered().values().iterator();

         MountRenderer.Mount mount;
         while(var4.hasNext()) {
            mount = (MountRenderer.Mount)var4.next();
            if (mount.getPosition().isDirty()) {
               set.add(this.pivotMove(mount));
               mount.getPosition().clearDirty();
            }

            if (mount.getYaw().isDirty()) {
               set.add((Packet)this.mountRotate(mount));
               mount.getYaw().clearDirty();
            }

            if (mount.getPassengers().isDirty()) {
               set.add((Packet)this.mount(mount));
               mount.getPassengers().clearDirty();
            }
         }

         var4 = renderer.getSpawnQueue().values().iterator();

         while(var4.hasNext()) {
            mount = (MountRenderer.Mount)var4.next();
            set.add(this.pivotSpawn(mount));
            set.add((Packet)this.pivotData(mount));
            set.add((Packet)this.mountSpawn(mount));
            set.add((Packet)this.mountData(mount));
            set.add((Packet)this.pivotMount(mount));
            set.add((Packet)this.mount(mount));
         }

         Map<String, MountRenderer.Mount> destroy = renderer.getDestroyQueue();
         if (!destroy.isEmpty()) {
            PacketDataSerializer buf = NetworkUtils.createByteBuf();
            buf.d(destroy.size() * 2);
            destroy.forEach((s, mountx) -> {
               buf.d(mountx.getPivotId());
               buf.d(mountx.getMountId());
            });
            set.add((Packet)(new PacketPlayOutEntityDestroy(buf)));
         }

         NetworkUtils.sendBundled(targets, set);
      }
   }

   private void remove(Set<Player> targets, MountRenderer renderer) {
      if (!targets.isEmpty()) {
         PacketDataSerializer buf = NetworkUtils.createByteBuf();
         Collection<MountRenderer.Mount> mounts = renderer.getRendered().values();
         buf.d(mounts.size() * 2);
         mounts.forEach((mount) -> {
            buf.d(mount.getPivotId());
            buf.d(mount.getMountId());
         });
         NetworkUtils.send((Set)targets, new PacketPlayOutEntityDestroy(buf));
      }
   }

   private Packets.PacketSupplier pivotSpawn(MountRenderer.Mount renderer) {
      return NetworkUtils.createPivotSpawn(renderer.getPivotId(), renderer.getPivotUuid(), (Vector3f)renderer.getPosition().get());
   }

   private PacketPlayOutEntityMetadata pivotData(MountRenderer.Mount renderer) {
      PacketDataSerializer buf = NetworkUtils.createByteBuf();
      buf.d(renderer.getPivotId());
      EntityUtils.writeData(buf, 0, DataWatcherRegistry.a, (byte)32);
      EntityUtils.writeData(buf, 1, DataWatcherRegistry.b, Integer.MAX_VALUE);
      EntityUtils.writeData(buf, 8, DataWatcherRegistry.d, 0.0F);
      buf.writeByte(255);
      return new PacketPlayOutEntityMetadata(buf);
   }

   private PacketPlayOutSpawnEntity mountSpawn(MountRenderer.Mount renderer) {
      Vector3f pos = (Vector3f)renderer.getPosition().get();
      return new PacketPlayOutSpawnEntity(renderer.getMountId(), renderer.getMountUuid(), (double)pos.x, (double)pos.y, (double)pos.z, 0.0F, TMath.byteToRot((Byte)renderer.getYaw().get()), EntityTypes.d, 0, Vec3D.b, 0.0D);
   }

   private PacketPlayOutEntityMetadata mountData(MountRenderer.Mount renderer) {
      PacketDataSerializer buf = NetworkUtils.createByteBuf();
      buf.d(renderer.getMountId());
      EntityUtils.writeData(buf, 0, DataWatcherRegistry.a, (byte)32);
      EntityUtils.writeData(buf, 1, DataWatcherRegistry.b, Integer.MAX_VALUE);
      EntityUtils.writeData(buf, 15, DataWatcherRegistry.a, (byte)16);
      buf.writeByte(255);
      return new PacketPlayOutEntityMetadata(buf);
   }

   private PacketPlayOutMount pivotMount(MountRenderer.Mount renderer) {
      PacketDataSerializer buf = NetworkUtils.createByteBuf();
      buf.d(renderer.getPivotId());
      buf.d(1);
      buf.d(renderer.getMountId());
      return new PacketPlayOutMount(buf);
   }

   private PacketPlayOutMount mount(MountRenderer.Mount renderer) {
      CollectionDataTracker<Integer> ids = renderer.getPassengers();
      PacketDataSerializer buf = NetworkUtils.createByteBuf();
      buf.d(renderer.getMountId());
      buf.d(ids.size());
      Objects.requireNonNull(buf);
      ids.forEach(buf::d);
      return new PacketPlayOutMount(buf);
   }

   private Packets.PacketSupplier pivotMove(MountRenderer.Mount renderer) {
      return NetworkUtils.createPivotTeleport(renderer.getPivotId(), (Vector3f)renderer.getPosition().get());
   }

   private PacketPlayOutEntityLook mountRotate(MountRenderer.Mount renderer) {
      PacketDataSerializer rot = NetworkUtils.createByteBuf();
      rot.d(renderer.getMountId());
      rot.writeByte((Byte)renderer.getYaw().get());
      rot.writeByte(0);
      rot.writeBoolean(false);
      return PacketPlayOutEntityLook.b(rot);
   }
}
