package com.ticxo.modelengine.core.listener;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.entity.BukkitPlayer;
import com.ticxo.modelengine.api.entity.data.IEntityData;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.core.menu.SelectionManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;

public class PlayerListener implements Listener {
   @EventHandler
   public void onJoin(PlayerJoinEvent event) {
      ModelEngineAPI.getNetworkHandler().injectChannel(event.getPlayer());
   }

   @EventHandler
   public void onQuit(PlayerQuitEvent event) {
      Player player = event.getPlayer();
      ModelEngineAPI.getNetworkHandler().ejectChannel(player);
      ModelEngineAPI.getMountPairManager().tryDismount(player);
      ModelEngineAPI.getInteractionTracker().removeDynamicHitbox(player.getUniqueId());
      ModelEngineAPI.getEntityHandler().setForcedInvisible(player, false);
      SelectionManager.removePlayer(event.getPlayer());
   }

   @EventHandler
   public void onPortal(PlayerPortalEvent event) {
      Player entity = event.getPlayer();
      if (ModelEngineAPI.getMountPairManager().get(entity.getUniqueId()) != null) {
         event.setCancelled(true);
      }

   }

   @EventHandler
   public void onTeleport(PlayerTeleportEvent event) {
      if (!(event instanceof PlayerPortalEvent)) {
         ModelEngineAPI.getMountPairManager().tryDismount(event.getPlayer());
      }
   }

   @EventHandler
   public void onDeath(PlayerDeathEvent event) {
      ModelEngineAPI.getMountPairManager().tryDismount(event.getPlayer());
   }

   @EventHandler
   public void onMove(PlayerMoveEvent event) {
      Vector dir = event.getTo().toVector().subtract(event.getFrom().toVector());
      if (!dir.isZero()) {
         Player player = event.getPlayer();
         ModeledEntity modeledEntity = ModelEngineAPI.getModeledEntity(player.getUniqueId());
         if (modeledEntity != null) {
            IEntityData var6 = modeledEntity.getBase().getData();
            if (var6 instanceof BukkitPlayer.BukkitPlayerData) {
               BukkitPlayer.BukkitPlayerData playerData = (BukkitPlayer.BukkitPlayerData)var6;
               if (dir.getX() != 0.0D || dir.getZ() != 0.0D) {
                  playerData.setWalkTick(3);
               }

               if (dir.getY() > 0.0D) {
                  playerData.setJumpTick(3);
               }

            }
         }
      }
   }
}
