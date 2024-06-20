package com.ticxo.modelengine.v1_19_R3.parser.vfx;

import com.ticxo.modelengine.api.entity.data.IEntityData;
import com.ticxo.modelengine.api.vfx.render.VFXDisplayRenderer;
import com.ticxo.modelengine.api.vfx.render.VFXRendererParser;
import com.ticxo.modelengine.v1_19_R3.entity.EntityUtils;
import com.ticxo.modelengine.v1_19_R3.network.utils.NetworkUtils;
import com.ticxo.modelengine.v1_19_R3.network.utils.Packets;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutMount;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.joml.Vector3f;

public class VFXDisplayParser implements VFXRendererParser<VFXDisplayRenderer> {
   public void sendToClients(VFXDisplayRenderer renderer) {
      IEntityData data = renderer.getVFX().getBase().getData();
      if (renderer.isRespawnRequired()) {
         this.spawn(data.getTracking().keySet(), renderer.getVFXModel());
         renderer.setRespawnRequired(false);
      } else {
         this.update(data.getTracking().keySet(), renderer.getVFXModel());
         this.spawn(data.getStartTracking(), renderer.getVFXModel());
         this.remove(data.getStopTracking(), renderer.getVFXModel());
      }

   }

   public void destroy(VFXDisplayRenderer renderer) {
      IEntityData data = renderer.getVFX().getBase().getData();
      HashSet<Player> inRange = new HashSet(data.getStartTracking());
      inRange.addAll(data.getTracking().keySet());
      inRange.addAll(data.getStopTracking());
      this.remove(inRange, renderer.getVFXModel());
   }

   public void spawn(Set<Player> targets, VFXDisplayRenderer.VFXModel vfx) {
      if (!targets.isEmpty()) {
         Packets set = new Packets();
         set.add(this.pivotSpawn(vfx));
         set.add((Packet)this.pivotData(vfx));
         set.add((Packet)this.vfxSpawn(vfx));
         set.add((Packet)this.vfxData(vfx, true));
         set.add((Packet)this.mount(vfx));
         NetworkUtils.sendBundled(targets, set);
      }
   }

   public void update(Set<Player> targets, VFXDisplayRenderer.VFXModel vfx) {
      if (!targets.isEmpty()) {
         Packets set = new Packets();
         set.add(this.teleport(vfx));
         set.add((Packet)this.vfxData(vfx, false));
         NetworkUtils.sendBundled(targets, set);
      }
   }

   public void remove(Set<Player> targets, VFXDisplayRenderer.VFXModel vfx) {
      if (!targets.isEmpty()) {
         PacketDataSerializer buf = NetworkUtils.createByteBuf();
         buf.d(2);
         buf.d(vfx.getPivotId());
         buf.d(vfx.getModelId());
         NetworkUtils.send((Set)targets, new PacketPlayOutEntityDestroy(buf));
      }
   }

   private Packets.PacketSupplier pivotSpawn(VFXDisplayRenderer.VFXModel vfx) {
      return NetworkUtils.createPivotSpawn(vfx.getPivotId(), vfx.getPivotUuid(), (Vector3f)vfx.getOrigin().get());
   }

   private PacketPlayOutEntityMetadata pivotData(VFXDisplayRenderer.VFXModel vfx) {
      PacketDataSerializer buf = NetworkUtils.createByteBuf();
      buf.d(vfx.getPivotId());
      EntityUtils.writeData(buf, 0, DataWatcherRegistry.a, (byte)32);
      EntityUtils.writeData(buf, 1, DataWatcherRegistry.b, Integer.MAX_VALUE);
      EntityUtils.writeData(buf, 8, DataWatcherRegistry.d, 0.0F);
      buf.writeByte(255);
      return new PacketPlayOutEntityMetadata(buf);
   }

   private PacketPlayOutSpawnEntity vfxSpawn(VFXDisplayRenderer.VFXModel vfx) {
      Vector3f location = (Vector3f)vfx.getOrigin().get();
      return new PacketPlayOutSpawnEntity(vfx.getModelId(), vfx.getModelUuid(), (double)location.x, (double)location.y, (double)location.z, 0.0F, 0.0F, EntityTypes.ae, 0, Vec3D.b, 0.0D);
   }

   private PacketPlayOutEntityMetadata vfxData(VFXDisplayRenderer.VFXModel vfx, boolean spawn) {
      if (!spawn && !vfx.isModelDirty()) {
         return null;
      } else {
         PacketDataSerializer buf = NetworkUtils.createByteBuf();
         buf.d(vfx.getModelId());
         if (spawn) {
            EntityUtils.writeData(buf, 0, DataWatcherRegistry.a, (byte)32);
            EntityUtils.writeData(buf, 1, DataWatcherRegistry.b, Integer.MAX_VALUE);
            EntityUtils.writeData(buf, 8, DataWatcherRegistry.b, 0);
            EntityUtils.writeData(buf, 9, DataWatcherRegistry.b, 1);
            EntityUtils.writeData(buf, 16, DataWatcherRegistry.d, 4096.0F);
         } else if (vfx.isModelDirty()) {
            EntityUtils.writeData(buf, 8, DataWatcherRegistry.b, 0);
         }

         vfx.getPosition().ifDirty((vector3f) -> {
            EntityUtils.writeData(buf, 10, DataWatcherRegistry.A, vector3f);
         }, spawn);
         vfx.getScale().ifDirty((vector3f) -> {
            EntityUtils.writeData(buf, 11, DataWatcherRegistry.A, vector3f);
         }, spawn);
         vfx.getLeftRotation().ifDirty((quaternionf) -> {
            EntityUtils.writeData(buf, 12, DataWatcherRegistry.B, quaternionf);
         }, spawn);
         vfx.getModel().ifDirty((itemStack) -> {
            EntityUtils.writeData(buf, 22, DataWatcherRegistry.h, CraftItemStack.asNMSCopy(itemStack));
         }, spawn);
         buf.writeByte(255);
         vfx.clearModelDirty();
         return new PacketPlayOutEntityMetadata(buf);
      }
   }

   private Packets.PacketSupplier teleport(VFXDisplayRenderer.VFXModel vfx) {
      return !vfx.getOrigin().isDirty() ? null : NetworkUtils.createPivotTeleport(vfx.getPivotId(), (Vector3f)vfx.getOrigin().get());
   }

   private PacketPlayOutMount mount(VFXDisplayRenderer.VFXModel vfx) {
      PacketDataSerializer buf = NetworkUtils.createByteBuf();
      buf.d(vfx.getPivotId());
      buf.d(1);
      buf.d(vfx.getModelId());
      return new PacketPlayOutMount(buf);
   }
}
