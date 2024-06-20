package com.ticxo.modelengine.v1_20_R4.entity;

import com.google.common.collect.Sets;
import com.ticxo.modelengine.api.nms.entity.wrapper.TrackedEntity;
import com.ticxo.modelengine.api.utils.ReflectionUtils;
import com.ticxo.modelengine.v1_20_R4.NMSFields;
import com.ticxo.modelengine.v1_20_R4.NMSMethods;
import com.ticxo.modelengine.v1_20_R4.network.utils.NetworkUtils;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.PlayerChunkMap.EntityTracker;
import net.minecraft.server.network.ServerPlayerConnection;
import org.bukkit.craftbukkit.v1_20_R4.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TrackedEntityImpl implements TrackedEntity {
   private final Entity entity;
   private final Supplier<EntityTracker> trackedEntitySupplier;
   private final Set<Player> forcedPairing = Sets.newConcurrentHashSet();
   private final Set<Player> forcedRemove = Sets.newConcurrentHashSet();
   private Predicate<Player> playerPredicate;
   @NotNull
   private EntityTracker lastTrackedEntity;

   public TrackedEntityImpl(Entity entity, Supplier<EntityTracker> trackedEntitySupplier, @NotNull EntityTracker lastTrackedEntity) {
      this.playerPredicate = DEFAULT_PREDICATE;
      this.entity = entity;
      this.trackedEntitySupplier = trackedEntitySupplier;
      this.lastTrackedEntity = lastTrackedEntity;
   }

   public int getBaseRange() {
      Integer range = (Integer)ReflectionUtils.get(this.getTrackedEntity(), NMSFields.TRACKED_ENTITY_range);
      if (range == null) {
         throw new NullPointerException(String.format("Unable to retrieve base range of entity with UUID %s.", this.entity.getUniqueId()));
      } else {
         return range;
      }
   }

   public void setBaseRange(int range) {
      ReflectionUtils.set(this.getTrackedEntity(), NMSFields.TRACKED_ENTITY_range, range);
   }

   public int getEffectiveRange() {
      Integer range = (Integer)ReflectionUtils.call(this.getTrackedEntity(), NMSMethods.TRACKED_ENTITY_getEffectiveRange);
      if (range == null) {
         throw new NullPointerException(String.format("Unable to retrieve range of entity with UUID %s.", this.entity.getUniqueId()));
      } else {
         return range;
      }
   }

   public Set<Player> getTrackedPlayer() {
      HashSet<Player> set = new HashSet(this.forcedPairing);
      Iterator var2 = this.getTrackedEntity().f.iterator();

      while(var2.hasNext()) {
         ServerPlayerConnection connection = (ServerPlayerConnection)var2.next();
         Player player = connection.p().getBukkitEntity().getPlayer();
         if (this.playerPredicate.test(player) && !this.forcedRemove.contains(player)) {
            set.add(player);
         }
      }

      return set;
   }

   public Set<Player> getTrackedPlayer(Predicate<Player> predicate) {
      HashSet<Player> set = new HashSet(this.forcedPairing);
      Iterator var3 = this.getTrackedEntity().f.iterator();

      while(var3.hasNext()) {
         ServerPlayerConnection connection = (ServerPlayerConnection)var3.next();
         Player player = connection.p().getBukkitEntity().getPlayer();
         if (predicate.test(player) && this.playerPredicate.test(player) && !this.forcedRemove.contains(player)) {
            set.add(player);
         }
      }

      return set;
   }

   public void sendPairingData(Player player) {
      EntityPlayer nms = ((CraftPlayer)player).getHandle();
      this.getTrackedEntity().b.a(nms, (packet) -> {
         NetworkUtils.send(player, packet);
      });
   }

   public void broadcastSpawn() {
      Iterator var1 = this.getTrackedPlayer().iterator();

      while(var1.hasNext()) {
         Player player = (Player)var1.next();
         this.sendPairingData(player);
      }

   }

   public void broadcastRemove() {
      this.getTrackedEntity().a();
   }

   public void addForcedPairing(Player player) {
      this.forcedPairing.add(player);
      this.removeForcedHidden(player);
   }

   public void removeForcedPairing(Player player) {
      this.forcedPairing.remove(player);
   }

   public void addForcedHidden(Player player) {
      this.forcedRemove.add(player);
      this.removeForcedPairing(player);
   }

   public void removeForcedHidden(Player player) {
      this.forcedRemove.remove(player);
   }

   @NotNull
   private EntityTracker getTrackedEntity() {
      EntityTracker trackedEntity = (EntityTracker)this.trackedEntitySupplier.get();
      if (trackedEntity != null && this.lastTrackedEntity != trackedEntity) {
         this.syncInstance(trackedEntity);
         this.lastTrackedEntity = trackedEntity;
      }

      return this.lastTrackedEntity;
   }

   private void syncInstance(EntityTracker target) {
      Integer range = (Integer)ReflectionUtils.get(this.lastTrackedEntity, NMSFields.TRACKED_ENTITY_range);
      if (range != null) {
         ReflectionUtils.set(target, NMSFields.TRACKED_ENTITY_range, range);
      }

   }

   public Entity getEntity() {
      return this.entity;
   }

   public Predicate<Player> getPlayerPredicate() {
      return this.playerPredicate;
   }

   public void setPlayerPredicate(Predicate<Player> playerPredicate) {
      this.playerPredicate = playerPredicate;
   }
}
