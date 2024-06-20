package com.ticxo.modelengine.core.menu;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class SelectionManager {
   private static final Set<Player> SELECTING = Sets.newConcurrentHashSet();
   private static final Map<Player, Entity> SELECTED = Maps.newConcurrentMap();

   public static void setSelecting(Player player, boolean flag) {
      if (flag) {
         SELECTING.add(player);
      } else {
         SELECTING.remove(player);
      }

   }

   public static boolean isSelecting(Player player) {
      return SELECTING.contains(player);
   }

   public static void setSelected(Player player, @Nullable Entity entity) {
      if (entity == null) {
         SELECTED.remove(player);
      } else {
         SELECTED.put(player, entity);
      }

   }

   public static Entity getSelected(Player player) {
      return (Entity)SELECTED.get(player);
   }

   public static void removePlayer(Player player) {
      SELECTING.remove(player);
      SELECTED.remove(player);
   }
}
