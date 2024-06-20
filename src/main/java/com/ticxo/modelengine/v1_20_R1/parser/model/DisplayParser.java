package com.ticxo.modelengine.v1_20_R1.parser.model;

import com.google.common.collect.ImmutableSet;
import com.ticxo.modelengine.api.entity.BaseEntity;
import com.ticxo.modelengine.api.entity.BukkitEntity;
import com.ticxo.modelengine.api.entity.CullType;
import com.ticxo.modelengine.api.entity.data.IEntityData;
import com.ticxo.modelengine.api.model.render.DisplayRenderer;
import com.ticxo.modelengine.api.model.render.ModelRendererParser;
import com.ticxo.modelengine.api.utils.data.tracker.CollectionDataTracker;
import com.ticxo.modelengine.v1_20_R1.entity.EntityUtils;
import com.ticxo.modelengine.v1_20_R1.network.utils.NetworkUtils;
import com.ticxo.modelengine.v1_20_R1.network.utils.Packets;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutMount;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class DisplayParser implements ModelRendererParser<DisplayRenderer> {
   private final Map<String, Set<Player>> players = new HashMap();
   private final Set<Runnable> cleanupQueue = new HashSet();

   public void sendToClients(DisplayRenderer renderer) {
      IEntityData data = renderer.getActiveModel().getModeledEntity().getBase().getData();
      Iterator var3 = data.getTracking().entrySet().iterator();

      while(var3.hasNext()) {
         Entry<Player, CullType> entry = (Entry)var3.next();
         switch((CullType)entry.getValue()) {
         case NO_CULL:
            if (renderer.pollFullUpdate((Player)entry.getKey())) {
               ((Set)this.players.computeIfAbsent("NO_CULL_FORCE", (s) -> {
                  return new HashSet();
               })).add((Player)entry.getKey());
            } else {
               ((Set)this.players.computeIfAbsent(((CullType)entry.getValue()).name(), (s) -> {
                  return new HashSet();
               })).add((Player)entry.getKey());
            }
            break;
         case MOVEMENT_ONLY:
            renderer.pushFullUpdate((Player)entry.getKey());
            ((Set)this.players.computeIfAbsent(((CullType)entry.getValue()).name(), (s) -> {
               return new HashSet();
            })).add((Player)entry.getKey());
            break;
         case CULLED:
            ((Set)this.players.computeIfAbsent(((CullType)entry.getValue()).name(), (s) -> {
               return new HashSet();
            })).add((Player)entry.getKey());
         }
      }

      if (renderer.pollFirstSpawn()) {
         HashSet<Player> set = new HashSet();
         set.addAll((Collection)this.players.getOrDefault("MOVEMENT_ONLY", ImmutableSet.of()));
         set.addAll((Collection)this.players.getOrDefault("NO_CULL", ImmutableSet.of()));
         set.addAll((Collection)this.players.getOrDefault("NO_CULL_FORCE", ImmutableSet.of()));
         this.spawn(set, renderer);
      } else {
         this.spawn(data.getStartTracking(), renderer);
         this.updateRealtime((Set)this.players.getOrDefault("MOVEMENT_ONLY", ImmutableSet.of()), renderer, true, false);
         this.updateRealtime((Set)this.players.getOrDefault("NO_CULL", ImmutableSet.of()), renderer, false, false);
         this.updateRealtime((Set)this.players.getOrDefault("NO_CULL_FORCE", ImmutableSet.of()), renderer, false, true);
         this.updateCulled((Set)this.players.getOrDefault("CULLED", ImmutableSet.of()), renderer);
         this.remove(data.getStopTracking(), renderer);
      }

      this.players.forEach((cullType, players) -> {
         players.clear();
      });
      renderer.getPivot().clearDirty();
      renderer.getHitbox().clearDirty();
      this.cleanupQueue.forEach(Runnable::run);
      this.cleanupQueue.clear();
   }

   public void destroy(DisplayRenderer renderer) {
      IEntityData data = renderer.getActiveModel().getModeledEntity().getBase().getData();
      HashSet<Player> inRange = new HashSet(data.getStartTracking());
      inRange.addAll(data.getTracking().keySet());
      inRange.addAll(data.getStopTracking());
      this.remove(inRange, renderer);
   }

   private void spawn(Set<Player> targets, DisplayRenderer renderer) {
      if (!targets.isEmpty()) {
         Packets set = new Packets();
         DisplayRenderer.Pivot pivot = renderer.getPivot();
         set.add(this.pivotSpawn(pivot));
         set.add((Packet)this.pivotData(pivot));
         Iterator var5 = renderer.getRendered().values().iterator();

         DisplayRenderer.Bone bone;
         while(var5.hasNext()) {
            bone = (DisplayRenderer.Bone)var5.next();
            set.add((Packet)this.displaySpawn(pivot, bone));
            set.add((Packet)this.displayData(bone, true, false));
         }

         var5 = renderer.getSpawnQueue().values().iterator();

         while(var5.hasNext()) {
            bone = (DisplayRenderer.Bone)var5.next();
            set.add((Packet)this.displaySpawn(pivot, bone));
            set.add((Packet)this.displayData(bone, true, false));
         }

         set.add((Packet)this.pivotMount(pivot));
         BaseEntity<?> base = renderer.getActiveModel().getModeledEntity().getBase();
         if (base instanceof BukkitEntity) {
            BukkitEntity bukkitEntity = (BukkitEntity)base;
            Entity var8 = bukkitEntity.getOriginal();
            if (var8 instanceof Player) {
               Player owner = (Player)var8;
               if (targets.contains(owner)) {
                  NetworkUtils.sendBundled(owner, set);
               }
            }
         }

         DisplayRenderer.Hitbox hitbox = renderer.getHitbox();
         set.add(this.hitboxSpawnPivot(hitbox));
         set.add((Packet)this.hitboxDataPivot(hitbox));
         set.add((Packet)this.hitboxSpawn(hitbox));
         set.add((Packet)this.hitboxData(hitbox, true));
         set.add((Packet)this.shadowSpawn(hitbox));
         set.add((Packet)this.shadowData(hitbox, true));
         set.add((Packet)this.hitboxMount(hitbox));
         NetworkUtils.sendBundled(targets, set, (player) -> {
            return !player.getUniqueId().equals(base.getUUID());
         });
      }
   }

   private void updateRealtime(Set<Player> targets, DisplayRenderer renderer, boolean movementOnly, boolean dynamicOnly) {
      if (!targets.isEmpty()) {
         Packets set = new Packets();
         HashSet<Integer> destroy = new HashSet();
         DisplayRenderer.Pivot pivot = renderer.getPivot();
         set.add(this.pivotTeleport(pivot));
         Iterator var8;
         DisplayRenderer.Bone bone;
         if (!movementOnly) {
            var8 = renderer.getRendered().values().iterator();

            while(var8.hasNext()) {
               bone = (DisplayRenderer.Bone)var8.next();
               set.add((Packet)this.displayData(bone, false, dynamicOnly));
            }
         } else {
            var8 = renderer.getRendered().values().iterator();

            while(var8.hasNext()) {
               bone = (DisplayRenderer.Bone)var8.next();
               set.add((Packet)this.displayVisibleData(bone));
            }
         }

         var8 = renderer.getSpawnQueue().values().iterator();

         while(var8.hasNext()) {
            bone = (DisplayRenderer.Bone)var8.next();
            set.add((Packet)this.displaySpawn(pivot, bone));
            set.add((Packet)this.displayData(bone, true, false));
         }

         renderer.getDestroyQueue().forEach((s, bonex) -> {
            destroy.add(bonex.getId());
         });
         if (pivot.getPassengers().isDirty()) {
            set.add((Packet)this.pivotMount(pivot));
         }

         DisplayRenderer.Hitbox hitbox = renderer.getHitbox();
         boolean updateHitboxMount = false;
         set.add(this.hitboxTeleport(hitbox));
         if (hitbox.getHitboxVisible().isDirty()) {
            if (hitbox.isHitboxVisible()) {
               set.add((Packet)this.hitboxSpawn(hitbox));
               set.add((Packet)this.hitboxData(hitbox, true));
               updateHitboxMount = true;
            } else {
               destroy.add(hitbox.getHitboxId());
            }
         } else {
            set.add((Packet)this.hitboxData(hitbox, false));
         }

         if (hitbox.getShadowVisible().isDirty()) {
            if (hitbox.isShadowVisible()) {
               set.add((Packet)this.shadowSpawn(hitbox));
               set.add((Packet)this.shadowData(hitbox, true));
               updateHitboxMount = true;
            } else {
               destroy.add(hitbox.getShadowId());
            }
         } else {
            set.add((Packet)this.shadowData(hitbox, false));
         }

         if (updateHitboxMount) {
            set.add((Packet)this.hitboxMount(hitbox));
         }

         PacketDataSerializer buf = NetworkUtils.createByteBuf();
         buf.d(destroy.size());
         Objects.requireNonNull(buf);
         destroy.forEach(buf::d);
         set.add((Packet)(new PacketPlayOutEntityDestroy(buf)));
         NetworkUtils.sendBundled(targets, set);
      }
   }

   private void updateCulled(Set<Player> targets, DisplayRenderer renderer) {
      if (!targets.isEmpty()) {
         Map<String, DisplayRenderer.Bone> destroy = renderer.getDestroyQueue();
         if (!destroy.isEmpty()) {
            PacketDataSerializer buf = NetworkUtils.createByteBuf();
            buf.d(destroy.size());
            destroy.forEach((s, item) -> {
               buf.d(item.getId());
            });
            NetworkUtils.send((Set)targets, new PacketPlayOutEntityDestroy(buf));
         }

      }
   }

   private void remove(Set<Player> targets, DisplayRenderer renderer) {
      if (!targets.isEmpty()) {
         PacketDataSerializer buf = NetworkUtils.createByteBuf();
         buf.d(4 + renderer.getRendered().size());
         buf.d(renderer.getPivot().getId());
         buf.d(renderer.getHitbox().getPivotId());
         buf.d(renderer.getHitbox().getHitboxId());
         buf.d(renderer.getHitbox().getShadowId());
         renderer.getRendered().forEach((s, bone) -> {
            buf.d(bone.getId());
         });
         NetworkUtils.send((Set)targets, new PacketPlayOutEntityDestroy(buf));
      }
   }

   private Packets.PacketSupplier pivotSpawn(DisplayRenderer.Pivot pivot) {
      return NetworkUtils.createPivotSpawn(pivot.getId(), pivot.getUuid(), (Vector3f)pivot.getPosition().get());
   }

   private PacketPlayOutEntityMetadata pivotData(DisplayRenderer.Pivot pivot) {
      PacketDataSerializer buf = NetworkUtils.createByteBuf();
      buf.d(pivot.getId());
      EntityUtils.writeData(buf, 0, DataWatcherRegistry.a, (byte)32);
      EntityUtils.writeData(buf, 1, DataWatcherRegistry.b, Integer.MAX_VALUE);
      EntityUtils.writeData(buf, 8, DataWatcherRegistry.d, 0.0F);
      buf.writeByte(255);
      return new PacketPlayOutEntityMetadata(buf);
   }

   private PacketPlayOutMount pivotMount(DisplayRenderer.Pivot pivot) {
      PacketDataSerializer buf = NetworkUtils.createByteBuf();
      buf.d(pivot.getId());
      buf.d(pivot.getPassengers().size());
      CollectionDataTracker var10000 = pivot.getPassengers();
      Objects.requireNonNull(buf);
      var10000.forEach(buf::d);
      return new PacketPlayOutMount(buf);
   }

   private Packets.PacketSupplier pivotTeleport(DisplayRenderer.Pivot pivot) {
      if (!pivot.getPosition().isDirty()) {
         return null;
      } else {
         this.cleanupQueue.add(() -> {
            pivot.getPosition().clearDirty();
         });
         return NetworkUtils.createPivotTeleport(pivot.getId(), (Vector3f)pivot.getPosition().get());
      }
   }

   private PacketPlayOutSpawnEntity displaySpawn(DisplayRenderer.Pivot pivot, DisplayRenderer.Bone bone) {
      Vector3f pos = (Vector3f)pivot.getPosition().get();
      return new PacketPlayOutSpawnEntity(bone.getId(), bone.getUuid(), (double)pos.x, (double)pos.y, (double)pos.z, 0.0F, 0.0F, EntityTypes.ae, 0, Vec3D.b, 0.0D);
   }

   private PacketPlayOutEntityMetadata displayData(DisplayRenderer.Bone bone, boolean force, boolean dynamicOnly) {
      if (!force && !dynamicOnly && !bone.isDirty()) {
         return null;
      } else {
         PacketDataSerializer buf = NetworkUtils.createByteBuf();
         buf.d(bone.getId());
         if (force) {
            EntityUtils.writeData(buf, 1, DataWatcherRegistry.b, Integer.MAX_VALUE);
            EntityUtils.writeData(buf, 8, DataWatcherRegistry.b, 0);
         } else if (bone.isTransformDirty() || dynamicOnly) {
            EntityUtils.writeData(buf, 8, DataWatcherRegistry.b, 0);
         }

         bone.getStep().ifDirty((flag) -> {
            EntityUtils.writeData(buf, 9, DataWatcherRegistry.b, flag ? 0 : 1);
         }, force || dynamicOnly);
         bone.getGlowing().ifDirty((flag) -> {
            EntityUtils.writeData(buf, 0, DataWatcherRegistry.a, (byte)(flag ? 96 : 32));
         }, force || dynamicOnly);
         bone.getGlowColor().ifDirty((color) -> {
            EntityUtils.writeData(buf, 21, DataWatcherRegistry.b, color);
         }, force || dynamicOnly);
         bone.getBrightness().ifDirty((val) -> {
            EntityUtils.writeData(buf, 15, DataWatcherRegistry.b, val);
         }, force || dynamicOnly);
         bone.getPosition().ifDirty((vector3f) -> {
            EntityUtils.writeData(buf, 10, DataWatcherRegistry.A, vector3f);
         }, force || dynamicOnly);
         bone.getScale().ifDirty((vector3f) -> {
            EntityUtils.writeData(buf, 11, DataWatcherRegistry.A, vector3f);
         }, force || dynamicOnly);
         bone.getLeftRotation().ifDirty((quaternionf) -> {
            EntityUtils.writeData(buf, 12, DataWatcherRegistry.B, quaternionf.rotateY(3.1415927F, new Quaternionf()));
         }, force || dynamicOnly);
         bone.getRightRotation().ifDirty((quaternionf) -> {
            EntityUtils.writeData(buf, 13, DataWatcherRegistry.B, quaternionf);
         }, force || dynamicOnly);
         bone.getVisibility().ifDirty((flag) -> {
            EntityUtils.writeData(buf, 16, DataWatcherRegistry.d, flag ? 4096.0F : 0.0F);
         }, force || dynamicOnly);
         bone.getModel().ifDirty((itemStack) -> {
            EntityUtils.writeData(buf, 22, DataWatcherRegistry.h, CraftItemStack.asNMSCopy(itemStack));
         }, force || dynamicOnly);
         bone.getDisplay().ifDirty((display) -> {
            EntityUtils.writeData(buf, 23, DataWatcherRegistry.a, display == null ? 0 : (byte)display.ordinal());
         }, force || dynamicOnly);
         buf.writeByte(255);
         Set var10000 = this.cleanupQueue;
         Objects.requireNonNull(bone);
         var10000.add(bone::clearDirty);
         return new PacketPlayOutEntityMetadata(buf);
      }
   }

   private PacketPlayOutEntityMetadata displayVisibleData(DisplayRenderer.Bone bone) {
      if (!bone.isRenderDirty()) {
         return null;
      } else {
         PacketDataSerializer buf = NetworkUtils.createByteBuf();
         buf.d(bone.getId());
         bone.getGlowing().ifDirty((flag) -> {
            EntityUtils.writeData(buf, 0, DataWatcherRegistry.a, (byte)(flag ? 96 : 32));
         });
         bone.getGlowColor().ifDirty((color) -> {
            EntityUtils.writeData(buf, 21, DataWatcherRegistry.b, color);
         });
         bone.getBrightness().ifDirty((val) -> {
            EntityUtils.writeData(buf, 15, DataWatcherRegistry.b, val);
         });
         bone.getVisibility().ifDirty((flag) -> {
            EntityUtils.writeData(buf, 16, DataWatcherRegistry.d, flag ? 4096.0F : 0.0F);
         });
         bone.getModel().ifDirty((itemStack) -> {
            EntityUtils.writeData(buf, 22, DataWatcherRegistry.h, CraftItemStack.asNMSCopy(itemStack));
         });
         bone.getDisplay().ifDirty((display) -> {
            EntityUtils.writeData(buf, 23, DataWatcherRegistry.a, display == null ? 0 : (byte)display.ordinal());
         });
         buf.writeByte(255);
         Set var10000 = this.cleanupQueue;
         Objects.requireNonNull(bone);
         var10000.add(bone::clearDirty);
         return new PacketPlayOutEntityMetadata(buf);
      }
   }

   private Packets.PacketSupplier hitboxSpawnPivot(DisplayRenderer.Hitbox hitbox) {
      return hitbox.isPivotVisible() ? NetworkUtils.createPivotSpawn(hitbox.getPivotId(), hitbox.getPivotUuid(), (Vector3f)hitbox.getPosition().get()) : null;
   }

   private PacketPlayOutEntityMetadata hitboxDataPivot(DisplayRenderer.Hitbox hitbox) {
      if (!hitbox.isPivotVisible()) {
         return null;
      } else {
         PacketDataSerializer buf = NetworkUtils.createByteBuf();
         buf.d(hitbox.getPivotId());
         EntityUtils.writeData(buf, 0, DataWatcherRegistry.a, (byte)32);
         EntityUtils.writeData(buf, 1, DataWatcherRegistry.b, Integer.MAX_VALUE);
         EntityUtils.writeData(buf, 8, DataWatcherRegistry.d, 0.0F);
         buf.writeByte(255);
         return new PacketPlayOutEntityMetadata(buf);
      }
   }

   private PacketPlayOutSpawnEntity hitboxSpawn(DisplayRenderer.Hitbox hitbox) {
      if (!hitbox.isHitboxVisible()) {
         return null;
      } else {
         Vector3f pos = (Vector3f)hitbox.getPosition().get();
         return new PacketPlayOutSpawnEntity(hitbox.getHitboxId(), hitbox.getHitboxUuid(), (double)pos.x, (double)pos.y, (double)pos.z, 0.0F, 0.0F, EntityTypes.ab, 0, Vec3D.b, 0.0D);
      }
   }

   private PacketPlayOutEntityMetadata hitboxData(DisplayRenderer.Hitbox hitbox, boolean spawn) {
      if (hitbox.isHitboxVisible() && (hitbox.isHitboxDirty() || spawn)) {
         PacketDataSerializer buf = NetworkUtils.createByteBuf();
         buf.d(hitbox.getHitboxId());
         if (spawn) {
            EntityUtils.writeData(buf, 1, DataWatcherRegistry.b, Integer.MAX_VALUE);
            EntityUtils.writeData(buf, 10, DataWatcherRegistry.k, false);
         }

         hitbox.getWidth().ifDirty((val) -> {
            EntityUtils.writeData(buf, 8, DataWatcherRegistry.d, val);
         }, spawn);
         hitbox.getHeight().ifDirty((val) -> {
            EntityUtils.writeData(buf, 9, DataWatcherRegistry.d, val);
         }, spawn);
         buf.writeByte(255);
         return new PacketPlayOutEntityMetadata(buf);
      } else {
         return null;
      }
   }

   private PacketPlayOutSpawnEntity shadowSpawn(DisplayRenderer.Hitbox hitbox) {
      if (!hitbox.isShadowVisible()) {
         return null;
      } else {
         Vector3f pos = (Vector3f)hitbox.getPosition().get();
         return new PacketPlayOutSpawnEntity(hitbox.getShadowId(), hitbox.getShadowUuid(), (double)pos.x, (double)pos.y, (double)pos.z, 0.0F, 0.0F, EntityTypes.ae, 0, Vec3D.b, 0.0D);
      }
   }

   private PacketPlayOutEntityMetadata shadowData(DisplayRenderer.Hitbox hitbox, boolean spawn) {
      if (hitbox.isShadowVisible() && (hitbox.getShadowRadius().isDirty() || spawn)) {
         PacketDataSerializer buf = NetworkUtils.createByteBuf();
         buf.d(hitbox.getShadowId());
         if (spawn) {
            EntityUtils.writeData(buf, 1, DataWatcherRegistry.b, Integer.MAX_VALUE);
         }

         hitbox.getShadowRadius().ifDirty((val) -> {
            EntityUtils.writeData(buf, 17, DataWatcherRegistry.d, val);
         }, spawn);
         buf.writeByte(255);
         return new PacketPlayOutEntityMetadata(buf);
      } else {
         return null;
      }
   }

   private Packets.PacketSupplier hitboxTeleport(DisplayRenderer.Hitbox hitbox) {
      if (!hitbox.isPivotVisible() && !hitbox.getPosition().isDirty()) {
         return null;
      } else {
         this.cleanupQueue.add(() -> {
            hitbox.getPosition().clearDirty();
         });
         return NetworkUtils.createPivotTeleport(hitbox.getPivotId(), (Vector3f)hitbox.getPosition().get());
      }
   }

   private PacketPlayOutMount hitboxMount(DisplayRenderer.Hitbox hitbox) {
      if (!hitbox.isPivotVisible()) {
         return null;
      } else {
         boolean hitboxVisible = hitbox.isHitboxVisible();
         boolean shadowVisible = hitbox.isShadowVisible();
         PacketDataSerializer buf = NetworkUtils.createByteBuf();
         buf.d(hitbox.getPivotId());
         buf.d(hitboxVisible && shadowVisible ? 2 : 1);
         if (hitboxVisible) {
            buf.d(hitbox.getHitboxId());
         }

         if (shadowVisible) {
            buf.d(hitbox.getShadowId());
         }

         return new PacketPlayOutMount(buf);
      }
   }
}
