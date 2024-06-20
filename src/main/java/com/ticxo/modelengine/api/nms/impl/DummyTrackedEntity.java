package com.ticxo.modelengine.api.nms.impl;

import com.google.common.collect.Sets;
import com.ticxo.modelengine.api.nms.entity.wrapper.TrackedEntity;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Predicate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class DummyTrackedEntity implements TrackedEntity {
   private final Set<Player> tracked = Sets.newConcurrentHashSet();
   private final Set<Player> forcedPairing = Sets.newConcurrentHashSet();
   private final Set<Player> forcedRemove = Sets.newConcurrentHashSet();
   private int baseRange = 64;
   private Predicate<Player> playerPredicate;

   public DummyTrackedEntity() {
      this.playerPredicate = DEFAULT_PREDICATE;
   }

   public void detectPlayers(Location location) {
      int r2 = this.baseRange * this.baseRange;
      this.tracked.clear();
      Iterator var3 = Bukkit.getOnlinePlayers().iterator();

      while(var3.hasNext()) {
         Player player = (Player)var3.next();

         try {
            if (player.getLocation().distanceSquared(location) <= (double)r2) {
               this.tracked.add(player);
            }
         } catch (IllegalArgumentException var6) {
         }
      }

   }

   public int getEffectiveRange() {
      return this.baseRange;
   }

   public Set<Player> getTrackedPlayer() {
      HashSet<Player> set = new HashSet(this.forcedPairing);
      Iterator var2 = this.tracked.iterator();

      while(var2.hasNext()) {
         Player player = (Player)var2.next();
         if (this.playerPredicate.test(player) && !this.forcedRemove.contains(player)) {
            set.add(player);
         }
      }

      return set;
   }

   public Set<Player> getTrackedPlayer(Predicate<Player> predicate) {
      HashSet<Player> set = new HashSet(this.forcedPairing);
      Iterator var3 = this.tracked.iterator();

      while(var3.hasNext()) {
         Player player = (Player)var3.next();
         if (predicate.test(player) && this.playerPredicate.test(player) && !this.forcedRemove.contains(player)) {
            set.add(player);
         }
      }

      return set;
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

   public Entity getEntity() {
      return null;
   }

   public void sendPairingData(Player player) {
   }

   public void broadcastSpawn() {
   }

   public void broadcastRemove() {
   }

   public int getBaseRange() {
      return this.baseRange;
   }

   public void setBaseRange(int baseRange) {
      this.baseRange = baseRange;
   }

   public Predicate<Player> getPlayerPredicate() {
      return this.playerPredicate;
   }

   public void setPlayerPredicate(Predicate<Player> playerPredicate) {
      this.playerPredicate = playerPredicate;
   }
}
