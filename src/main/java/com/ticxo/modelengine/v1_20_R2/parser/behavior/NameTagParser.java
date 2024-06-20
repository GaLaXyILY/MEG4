package com.ticxo.modelengine.v1_20_R2.parser.behavior;

import com.ticxo.modelengine.api.entity.data.IEntityData;
import com.ticxo.modelengine.api.model.bone.render.BehaviorRendererParser;
import com.ticxo.modelengine.api.model.bone.render.renderer.NameTagRenderer;
import com.ticxo.modelengine.v1_20_R2.entity.EntityUtils;
import com.ticxo.modelengine.v1_20_R2.network.utils.NetworkUtils;
import com.ticxo.modelengine.v1_20_R2.network.utils.Packets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.chat.IChatBaseComponent.ChatSerializer;
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

public class NameTagParser implements BehaviorRendererParser<NameTagRenderer> {
   public void sendToClients(NameTagRenderer renderer) {
      IEntityData data = renderer.getModelRenderer().getActiveModel().getModeledEntity().getBase().getData();
      this.update(data.getTracking().keySet(), renderer);
      this.spawn(data.getStartTracking(), renderer);
      this.remove(data.getStopTracking(), renderer);
   }

   public void destroy(NameTagRenderer renderer) {
      IEntityData data = renderer.getActiveModel().getModeledEntity().getBase().getData();
      HashSet<Player> inRange = new HashSet(data.getStartTracking());
      inRange.addAll(data.getTracking().keySet());
      inRange.addAll(data.getStopTracking());
      this.remove(inRange, renderer);
   }

   private void spawn(Set<Player> targets, NameTagRenderer renderer) {
      if (!targets.isEmpty()) {
         Packets set = new Packets();
         Iterator var4 = renderer.getRendered().values().iterator();

         NameTagRenderer.NameTag nameTag;
         while(var4.hasNext()) {
            nameTag = (NameTagRenderer.NameTag)var4.next();
            set.add(this.pivotSpawn(nameTag));
            set.add((Packet)this.pivotData(nameTag, true));
            set.add((Packet)this.tagSpawn(nameTag));
            set.add((Packet)this.tagData(nameTag, true));
            set.add((Packet)this.pivotMount(nameTag));
         }

         var4 = renderer.getSpawnQueue().values().iterator();

         while(var4.hasNext()) {
            nameTag = (NameTagRenderer.NameTag)var4.next();
            set.add(this.pivotSpawn(nameTag));
            set.add((Packet)this.pivotData(nameTag, true));
            set.add((Packet)this.tagSpawn(nameTag));
            set.add((Packet)this.tagData(nameTag, true));
            set.add((Packet)this.pivotMount(nameTag));
         }

         NetworkUtils.sendBundled(targets, set);
      }
   }

   private void update(Set<Player> targets, NameTagRenderer renderer) {
      if (!targets.isEmpty()) {
         Packets set = new Packets();
         Iterator var4 = renderer.getRendered().values().iterator();

         NameTagRenderer.NameTag nameTag;
         while(var4.hasNext()) {
            nameTag = (NameTagRenderer.NameTag)var4.next();
            set.add((Packet)this.tagData(nameTag, false));
            if (nameTag.getPosition().isDirty()) {
               set.add(this.pivotMove(nameTag));
               nameTag.getPosition().clearDirty();
            }
         }

         var4 = renderer.getSpawnQueue().values().iterator();

         while(var4.hasNext()) {
            nameTag = (NameTagRenderer.NameTag)var4.next();
            set.add(this.pivotSpawn(nameTag));
            set.add((Packet)this.pivotData(nameTag, true));
            set.add((Packet)this.tagSpawn(nameTag));
            set.add((Packet)this.tagData(nameTag, true));
            set.add((Packet)this.pivotMount(nameTag));
         }

         Map<String, NameTagRenderer.NameTag> destroy = renderer.getDestroyQueue();
         if (!destroy.isEmpty()) {
            PacketDataSerializer buf = NetworkUtils.createByteBuf();
            buf.c(destroy.size() * 2);
            destroy.forEach((s, mount) -> {
               buf.c(mount.getPivotId());
               buf.c(mount.getTagId());
            });
            set.add((Packet)(new PacketPlayOutEntityDestroy(buf)));
         }

         NetworkUtils.sendBundled(targets, set);
      }
   }

   private void remove(Set<Player> targets, NameTagRenderer renderer) {
      if (!targets.isEmpty()) {
         PacketDataSerializer buf = NetworkUtils.createByteBuf();
         Collection<NameTagRenderer.NameTag> nameTags = renderer.getRendered().values();
         buf.c(nameTags.size() * 2);
         nameTags.forEach((nameTag) -> {
            buf.c(nameTag.getPivotId());
            buf.c(nameTag.getTagId());
         });
         NetworkUtils.send((Set)targets, new PacketPlayOutEntityDestroy(buf));
      }
   }

   private Packets.PacketSupplier pivotSpawn(NameTagRenderer.NameTag renderer) {
      return NetworkUtils.createPivotSpawn(renderer.getPivotId(), renderer.getPivotUuid(), (Vector3f)renderer.getPosition().get());
   }

   private PacketPlayOutEntityMetadata pivotData(NameTagRenderer.NameTag renderer, boolean spawn) {
      if (!spawn && !renderer.isDirty()) {
         return null;
      } else {
         PacketDataSerializer buf = NetworkUtils.createByteBuf();
         buf.c(renderer.getPivotId());
         if (spawn) {
            EntityUtils.writeData(buf, 0, DataWatcherRegistry.a, (byte)32);
            EntityUtils.writeData(buf, 1, DataWatcherRegistry.b, Integer.MAX_VALUE);
            EntityUtils.writeData(buf, 8, DataWatcherRegistry.d, 0.0F);
         }

         buf.k(255);
         renderer.clearDirty();
         return new PacketPlayOutEntityMetadata(buf);
      }
   }

   private PacketPlayOutSpawnEntity tagSpawn(NameTagRenderer.NameTag renderer) {
      Vector3f pos = (Vector3f)renderer.getPosition().get();
      return new PacketPlayOutSpawnEntity(renderer.getTagId(), renderer.getTagUuid(), (double)pos.x, (double)pos.y, (double)pos.z, 0.0F, 0.0F, EntityTypes.d, 0, Vec3D.b, 0.0D);
   }

   private PacketPlayOutEntityMetadata tagData(NameTagRenderer.NameTag renderer, boolean spawn) {
      if (!spawn && !renderer.isDirty()) {
         return null;
      } else {
         PacketDataSerializer buf = NetworkUtils.createByteBuf();
         buf.c(renderer.getTagId());
         if (spawn) {
            EntityUtils.writeData(buf, 0, DataWatcherRegistry.a, (byte)32);
            EntityUtils.writeData(buf, 1, DataWatcherRegistry.b, Integer.MAX_VALUE);
            EntityUtils.writeData(buf, 15, DataWatcherRegistry.a, (byte)16);
         }

         renderer.getVisibility().ifDirty((flag) -> {
            EntityUtils.writeData(buf, 3, DataWatcherRegistry.k, flag);
         }, spawn);
         renderer.getJsonString().ifDirty((jsonString) -> {
            EntityUtils.writeData(buf, 2, DataWatcherRegistry.g, Optional.ofNullable(ChatSerializer.a(jsonString)));
         }, spawn);
         buf.k(255);
         renderer.clearDirty();
         return new PacketPlayOutEntityMetadata(buf);
      }
   }

   private Packets.PacketSupplier pivotMove(NameTagRenderer.NameTag renderer) {
      return NetworkUtils.createPivotTeleport(renderer.getPivotId(), (Vector3f)renderer.getPosition().get());
   }

   private PacketPlayOutMount pivotMount(NameTagRenderer.NameTag renderer) {
      PacketDataSerializer buf = NetworkUtils.createByteBuf();
      buf.c(renderer.getPivotId());
      buf.c(1);
      buf.c(renderer.getTagId());
      return new PacketPlayOutMount(buf);
   }
}
