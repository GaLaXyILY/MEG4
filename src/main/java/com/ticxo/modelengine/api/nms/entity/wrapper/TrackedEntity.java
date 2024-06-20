package com.ticxo.modelengine.api.nms.entity.wrapper;

import java.util.Set;
import java.util.function.Predicate;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface TrackedEntity {
   Predicate<Player> DEFAULT_PREDICATE = (player) -> {
      return true;
   };

   Entity getEntity();

   int getBaseRange();

   void setBaseRange(int var1);

   int getEffectiveRange();

   Set<Player> getTrackedPlayer();

   Set<Player> getTrackedPlayer(Predicate<Player> var1);

   void sendPairingData(Player var1);

   void broadcastSpawn();

   void broadcastRemove();

   void addForcedPairing(Player var1);

   void removeForcedPairing(Player var1);

   void addForcedHidden(Player var1);

   void removeForcedHidden(Player var1);

   void setPlayerPredicate(@NotNull Predicate<Player> var1);

   @NotNull
   Predicate<Player> getPlayerPredicate();
}
