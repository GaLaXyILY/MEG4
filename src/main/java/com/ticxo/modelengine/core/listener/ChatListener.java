package com.ticxo.modelengine.core.listener;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {
   private static final Map<UUID, Predicate<String>> FETCH_MAP = Maps.newConcurrentMap();

   @EventHandler
   public void onAsyncChat(AsyncPlayerChatEvent event) {
      UUID uuid = event.getPlayer().getUniqueId();
      Predicate<String> fetch = (Predicate)FETCH_MAP.get(uuid);
      if (fetch != null) {
         event.setCancelled(true);
         if (fetch.test(ChatColor.stripColor(event.getMessage()))) {
            FETCH_MAP.remove(uuid);
         }

      }
   }

   public static void fetch(Player player, Predicate<String> consumer) {
      FETCH_MAP.put(player.getUniqueId(), consumer);
   }
}
