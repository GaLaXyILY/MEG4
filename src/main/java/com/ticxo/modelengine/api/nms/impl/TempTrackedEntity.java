package com.ticxo.modelengine.api.nms.impl;

import com.ticxo.modelengine.api.nms.entity.wrapper.TrackedEntity;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class TempTrackedEntity implements TrackedEntity {
   private final Entity entity;
   private final Set<Player> forcePaired = new HashSet();
   private final Set<Player> forceHidden = new HashSet();
   private int baseRange = -1;
   private Predicate<Player> playerPredicate;

   public int getEffectiveRange() {
      return this.baseRange;
   }

   public Set<Player> getTrackedPlayer() {
      return new HashSet();
   }

   public Set<Player> getTrackedPlayer(Predicate<Player> predicate) {
      return new HashSet();
   }

   public void sendPairingData(Player player) {
   }

   public void broadcastSpawn() {
   }

   public void broadcastRemove() {
   }

   public void addForcedPairing(Player player) {
      this.forcePaired.add(player);
   }

   public void removeForcedPairing(Player player) {
      this.forcePaired.remove(player);
   }

   public void addForcedHidden(Player player) {
      this.forceHidden.add(player);
   }

   public void removeForcedHidden(Player player) {
      this.forceHidden.remove(player);
   }

   public TempTrackedEntity(Entity entity) {
      this.playerPredicate = DEFAULT_PREDICATE;
      this.entity = entity;
   }

   public Entity getEntity() {
      return this.entity;
   }

   public Set<Player> getForcePaired() {
      return this.forcePaired;
   }

   public Set<Player> getForceHidden() {
      return this.forceHidden;
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
