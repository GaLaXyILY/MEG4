/* Decompiler 695ms, total 1244ms, lines 221 */
package com.ticxo.modelengine.v1_19_R3.parser.behavior;

import com.ticxo.modelengine.api.entity.data.IEntityData;
import com.ticxo.modelengine.api.model.bone.render.BehaviorRenderer;
import com.ticxo.modelengine.api.model.bone.render.BehaviorRendererParser;
import com.ticxo.modelengine.api.model.bone.render.renderer.HeldItemRenderer;
import com.ticxo.modelengine.api.model.bone.render.renderer.HeldItemRenderer.Item;
import com.ticxo.modelengine.api.utils.data.tracker.CollectionDataTracker;
import com.ticxo.modelengine.v1_19_R3.entity.EntityUtils;
import com.ticxo.modelengine.v1_19_R3.network.utils.NetworkUtils;
import com.ticxo.modelengine.v1_19_R3.network.utils.Packets;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutEntityTeleport;
import net.minecraft.network.protocol.game.PacketPlayOutMount;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;

public class HeldItemParser implements BehaviorRendererParser<HeldItemRenderer> {
   public void sendToClients(HeldItemRenderer renderer) {
      IEntityData data = renderer.getModelRenderer().getActiveModel().getModeledEntity().getBase().getData();
      this.update(data.getTracking().keySet(), renderer);
      this.spawn(data.getStartTracking(), renderer);
      this.remove(data.getStopTracking(), renderer);
   }

   public void destroy(HeldItemRenderer renderer) {
      IEntityData data = renderer.getActiveModel().getModeledEntity().getBase().getData();
      HashSet<Player> inRange = new HashSet(data.getStartTracking());
      inRange.addAll(data.getTracking().keySet());
      inRange.addAll(data.getStopTracking());
      this.remove(inRange, renderer);
   }

   private void spawn(Set<Player> targets, HeldItemRenderer renderer) {
      if (!targets.isEmpty()) {
         Packets set = new Packets();
         Location location = renderer.getActiveModel().getModeledEntity().getBase().getLocation();
         set.add(this.pivotSpawn(location, renderer));
         set.add(this.pivotData(renderer));
         Iterator var5 = renderer.getRendered().values().iterator();

         Item item;
         while(var5.hasNext()) {
            item = (Item)var5.next();
            set.add(this.itemSpawn(location, item));
            set.add(this.itemData(item, true));
         }

         var5 = renderer.getSpawnQueue().values().iterator();

         while(var5.hasNext()) {
            item = (Item)var5.next();
            set.add(this.itemSpawn(location, item));
            set.add(this.itemData(item, true));
         }

         set.add(this.mount(renderer));
         NetworkUtils.sendBundled(targets, set);
      }
   }

   private void update(Set<Player> targets, HeldItemRenderer renderer) {
      if (!targets.isEmpty()) {
         Packets set = new Packets();
         set.add(this.teleport(renderer));
         Iterator var4 = renderer.getRendered().values().iterator();

         while(var4.hasNext()) {
            Item item = (Item)var4.next();
            set.add(this.itemData(item, false));
         }

         Location location = renderer.getActiveModel().getModeledEntity().getBase().getLocation();
         Iterator var8 = renderer.getSpawnQueue().values().iterator();

         while(var8.hasNext()) {
            Item item = (Item)var8.next();
            set.add(this.itemSpawn(location, item));
            set.add(this.itemData(item, true));
         }

         Map<String, Item> destroy = renderer.getDestroyQueue();
         if (!destroy.isEmpty()) {
            PacketDataSerializer buf = NetworkUtils.createByteBuf();
            buf.d(destroy.size());
            destroy.forEach((s, itemx) -> {
               buf.d(itemx.getId());
            });
            set.add(new PacketPlayOutEntityDestroy(buf));
         }

         if (renderer.getPassengers().isDirty()) {
            set.add(this.mount(renderer));
            renderer.getPassengers().clearDirty();
         }

         NetworkUtils.sendBundled(targets, set);
      }
   }

   private void remove(Set<Player> targets, HeldItemRenderer renderer) {
      if (!targets.isEmpty()) {
         PacketDataSerializer buf = NetworkUtils.createByteBuf();
         Map<String, Item> items = renderer.getRendered();
         buf.d(1 + items.size());
         buf.d(renderer.getId());
         items.forEach((s, item) -> {
            buf.d(item.getId());
         });
         NetworkUtils.send(targets, new PacketPlayOutEntityDestroy(buf));
      }
   }

   private PacketPlayOutSpawnEntity pivotSpawn(Location location, HeldItemRenderer renderer) {
      return new PacketPlayOutSpawnEntity(renderer.getId(), renderer.getUuid(), location.getX(), location.getY(), location.getZ(), 0.0F, 0.0F, EntityTypes.d, 0, Vec3D.b, 0.0D);
   }

   private PacketPlayOutEntityMetadata pivotData(HeldItemRenderer renderer) {
      PacketDataSerializer buf = NetworkUtils.createByteBuf();
      buf.d(renderer.getId());
      EntityUtils.writeData(buf, 0, DataWatcherRegistry.a, (byte)32);
      EntityUtils.writeData(buf, 1, DataWatcherRegistry.b, Integer.MAX_VALUE);
      EntityUtils.writeData(buf, 15, DataWatcherRegistry.a, (byte)16);
      buf.writeByte(255);
      return new PacketPlayOutEntityMetadata(buf);
   }

   private PacketPlayOutSpawnEntity itemSpawn(Location location, Item renderer) {
      return new PacketPlayOutSpawnEntity(renderer.getId(), renderer.getUuid(), location.getX(), location.getY(), location.getZ(), 0.0F, 0.0F, EntityTypes.ae, 0, Vec3D.b, 0.0D);
   }

   private PacketPlayOutEntityMetadata itemData(Item renderer, boolean spawn) {
      if (!spawn && !renderer.isDirty()) {
         return null;
      } else {
         PacketDataSerializer buf = NetworkUtils.createByteBuf();
         buf.d(renderer.getId());
         if (spawn) {
            EntityUtils.writeData(buf, 1, DataWatcherRegistry.b, Integer.MAX_VALUE);
            EntityUtils.writeData(buf, 8, DataWatcherRegistry.b, 0);
            EntityUtils.writeData(buf, 9, DataWatcherRegistry.b, 1);
            EntityUtils.writeData(buf, 16, DataWatcherRegistry.d, 4096.0F);
         } else if (renderer.isTransformDirty()) {
            EntityUtils.writeData(buf, 8, DataWatcherRegistry.b, 0);
         }

         renderer.getGlowing().ifDirty((flag) -> {
            EntityUtils.writeData(buf, 0, DataWatcherRegistry.a, (byte)(flag ? 96 : 32));
         }, spawn);
         renderer.getGlowColor().ifDirty((color) -> {
            EntityUtils.writeData(buf, 21, DataWatcherRegistry.b, color);
         }, spawn);
         renderer.getPosition().ifDirty((vector3f) -> {
            EntityUtils.writeData(buf, 10, DataWatcherRegistry.A, vector3f);
         }, spawn);
         renderer.getScale().ifDirty((vector3f) -> {
            EntityUtils.writeData(buf, 11, DataWatcherRegistry.A, vector3f);
         }, spawn);
         renderer.getRotation().ifDirty((quaternionf) -> {
            EntityUtils.writeData(buf, 12, DataWatcherRegistry.B, quaternionf);
         }, spawn);
         renderer.getModel().ifDirty((itemStack) -> {
            EntityUtils.writeData(buf, 22, DataWatcherRegistry.h, CraftItemStack.asNMSCopy(itemStack));
         }, spawn);
         renderer.getDisplay().ifDirty((display) -> {
            EntityUtils.writeData(buf, 23, DataWatcherRegistry.a, display == null ? 0 : (byte)display.ordinal());
         }, spawn);
         buf.writeByte(255);
         renderer.clearDirty();
         return new PacketPlayOutEntityMetadata(buf);
      }
   }

   private PacketPlayOutEntityTeleport teleport(HeldItemRenderer renderer) {
      Location location = renderer.getActiveModel().getModeledEntity().getBase().getLocation();
      PacketDataSerializer buf = NetworkUtils.createByteBuf();
      buf.d(renderer.getId());
      buf.writeDouble(location.getX());
      buf.writeDouble(location.getY());
      buf.writeDouble(location.getZ());
      buf.writeByte(0);
      buf.writeByte(0);
      buf.writeBoolean(false);
      return new PacketPlayOutEntityTeleport(buf);
   }

   private PacketPlayOutMount mount(HeldItemRenderer renderer) {
      PacketDataSerializer buf = NetworkUtils.createByteBuf();
      buf.d(renderer.getId());
      CollectionDataTracker<Integer> set = renderer.getPassengers();
      buf.d(set.size());
      Objects.requireNonNull(buf);
      set.forEach(buf::d);
      return new PacketPlayOutMount(buf);
   }

   // $FF: synthetic method
   // $FF: bridge method
   public void destroy(BehaviorRenderer var1) {
      this.destroy((HeldItemRenderer)var1);
   }

   // $FF: synthetic method
   // $FF: bridge method
   public void sendToClients(BehaviorRenderer var1) {
      this.sendToClients((HeldItemRenderer)var1);
   }
}