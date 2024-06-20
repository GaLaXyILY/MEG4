package com.ticxo.modelengine.v1_20_R4.parser.behavior;

import com.ticxo.modelengine.api.entity.BaseEntity;
import com.ticxo.modelengine.api.entity.data.IEntityData;
import com.ticxo.modelengine.api.model.bone.render.BehaviorRendererParser;
import com.ticxo.modelengine.api.model.bone.render.renderer.SubHitboxRenderer;
import com.ticxo.modelengine.v1_20_R4.entity.EntityUtils;
import com.ticxo.modelengine.v1_20_R4.network.utils.NetworkUtils;
import com.ticxo.modelengine.v1_20_R4.network.utils.Packets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
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
import org.bukkit.entity.Player;
import org.joml.Vector3f;

public class SubHitboxParser implements BehaviorRendererParser<SubHitboxRenderer> {
   public void sendToClients(SubHitboxRenderer renderer) {
      IEntityData data = renderer.getModelRenderer().getActiveModel().getModeledEntity().getBase().getData();
      this.update(data.getTracking().keySet(), renderer);
      this.spawn(data.getStartTracking(), renderer);
      this.remove(data.getStopTracking(), renderer);
   }

   public void destroy(SubHitboxRenderer renderer) {
      IEntityData data = renderer.getActiveModel().getModeledEntity().getBase().getData();
      HashSet<Player> inRange = new HashSet(data.getStartTracking());
      inRange.addAll(data.getTracking().keySet());
      inRange.addAll(data.getStopTracking());
      this.remove(inRange, renderer);
   }

   private void spawn(Set<Player> targets, SubHitboxRenderer renderer) {
      if (!targets.isEmpty()) {
         Packets set = new Packets();
         Iterator var4 = renderer.getRendered().values().iterator();

         SubHitboxRenderer.SubHitbox subHitbox;
         while(var4.hasNext()) {
            subHitbox = (SubHitboxRenderer.SubHitbox)var4.next();
            set.add(this.pivotSpawn(subHitbox));
            set.add((Packet)this.pivotData(subHitbox));
            set.add((Packet)this.hitboxSpawn(subHitbox));
            set.add((Packet)this.hitboxData(subHitbox, true));
            set.add((Packet)this.pivotMount(subHitbox));
         }

         var4 = renderer.getSpawnQueue().values().iterator();

         while(var4.hasNext()) {
            subHitbox = (SubHitboxRenderer.SubHitbox)var4.next();
            set.add(this.pivotSpawn(subHitbox));
            set.add((Packet)this.pivotData(subHitbox));
            set.add((Packet)this.hitboxSpawn(subHitbox));
            set.add((Packet)this.hitboxData(subHitbox, true));
            set.add((Packet)this.pivotMount(subHitbox));
         }

         BaseEntity<?> base = renderer.getActiveModel().getModeledEntity().getBase();
         NetworkUtils.sendBundled(targets, set, (player) -> {
            return player != base.getOriginal();
         });
      }
   }

   private void update(Set<Player> targets, SubHitboxRenderer renderer) {
      if (!targets.isEmpty()) {
         Packets set = new Packets();
         Iterator var4 = renderer.getRendered().values().iterator();

         SubHitboxRenderer.SubHitbox subHitbox;
         while(var4.hasNext()) {
            subHitbox = (SubHitboxRenderer.SubHitbox)var4.next();
            set.add((Packet)this.hitboxData(subHitbox, false));
            if (subHitbox.getPosition().isDirty()) {
               set.add(this.pivotMove(subHitbox));
               subHitbox.getPosition().clearDirty();
            }
         }

         var4 = renderer.getSpawnQueue().values().iterator();

         while(var4.hasNext()) {
            subHitbox = (SubHitboxRenderer.SubHitbox)var4.next();
            set.add(this.pivotSpawn(subHitbox));
            set.add((Packet)this.pivotData(subHitbox));
            set.add((Packet)this.hitboxSpawn(subHitbox));
            set.add((Packet)this.hitboxData(subHitbox, true));
            set.add((Packet)this.pivotMount(subHitbox));
         }

         Map<String, SubHitboxRenderer.SubHitbox> destroy = renderer.getDestroyQueue();
         if (!destroy.isEmpty()) {
            PacketDataSerializer buf = NetworkUtils.createByteBuf();
            buf.c(destroy.size() * 2);
            destroy.forEach((s, subHitboxx) -> {
               buf.c(subHitboxx.getPivotId());
               buf.c(subHitboxx.getHitboxId());
            });
            set.add((Packet)(new PacketPlayOutEntityDestroy(buf)));
         }

         NetworkUtils.sendBundled(targets, set);
      }
   }

   private void remove(Set<Player> targets, SubHitboxRenderer renderer) {
      if (!targets.isEmpty()) {
         PacketDataSerializer buf = NetworkUtils.createByteBuf();
         Collection<SubHitboxRenderer.SubHitbox> subHitboxes = renderer.getRendered().values();
         buf.c(subHitboxes.size() * 2);
         subHitboxes.forEach((subHitbox) -> {
            buf.c(subHitbox.getPivotId());
            buf.c(subHitbox.getHitboxId());
         });
         NetworkUtils.send((Set)targets, new PacketPlayOutEntityDestroy(buf));
      }
   }

   private Packets.PacketSupplier pivotSpawn(SubHitboxRenderer.SubHitbox renderer) {
      return NetworkUtils.createPivotSpawn(renderer.getPivotId(), renderer.getPivotUuid(), (Vector3f)renderer.getPosition().get());
   }

   private PacketPlayOutEntityMetadata pivotData(SubHitboxRenderer.SubHitbox renderer) {
      PacketDataSerializer buf = NetworkUtils.createByteBuf();
      buf.c(renderer.getPivotId());
      EntityUtils.writeData(buf, 0, DataWatcherRegistry.a, (byte)32);
      EntityUtils.writeData(buf, 1, DataWatcherRegistry.b, Integer.MAX_VALUE);
      EntityUtils.writeData(buf, 8, DataWatcherRegistry.d, 0.0F);
      buf.k(255);
      return new PacketPlayOutEntityMetadata(buf);
   }

   private Packets.PacketSupplier pivotMove(SubHitboxRenderer.SubHitbox renderer) {
      return NetworkUtils.createPivotTeleport(renderer.getPivotId(), (Vector3f)renderer.getPosition().get());
   }

   private PacketPlayOutMount pivotMount(SubHitboxRenderer.SubHitbox renderer) {
      PacketDataSerializer buf = NetworkUtils.createByteBuf();
      buf.c(renderer.getPivotId());
      buf.c(1);
      buf.c(renderer.getHitboxId());
      return new PacketPlayOutMount(buf);
   }

   private PacketPlayOutSpawnEntity hitboxSpawn(SubHitboxRenderer.SubHitbox renderer) {
      Vector3f pos = (Vector3f)renderer.getPosition().get();
      return new PacketPlayOutSpawnEntity(renderer.getHitboxId(), renderer.getHitboxUuid(), (double)pos.x, (double)pos.y, (double)pos.z, 0.0F, 0.0F, EntityTypes.ac, 0, Vec3D.b, 0.0D);
   }

   private PacketPlayOutEntityMetadata hitboxData(SubHitboxRenderer.SubHitbox renderer, boolean spawn) {
      if (!spawn && !renderer.isDirty()) {
         return null;
      } else {
         PacketDataSerializer buf = NetworkUtils.createByteBuf();
         buf.c(renderer.getHitboxId());
         if (spawn) {
            EntityUtils.writeData(buf, 1, DataWatcherRegistry.b, Integer.MAX_VALUE);
            EntityUtils.writeData(buf, 10, DataWatcherRegistry.k, false);
         }

         renderer.getWidth().ifDirty((width) -> {
            EntityUtils.writeData(buf, 8, DataWatcherRegistry.d, width);
         }, spawn);
         renderer.getHeight().ifDirty((height) -> {
            EntityUtils.writeData(buf, 9, DataWatcherRegistry.d, height);
         }, spawn);
         buf.k(255);
         renderer.clearDirty();
         return new PacketPlayOutEntityMetadata(buf);
      }
   }
}
