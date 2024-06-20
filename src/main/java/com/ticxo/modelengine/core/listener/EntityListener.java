package com.ticxo.modelengine.core.listener;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.model.bone.manager.BehaviorManager;
import com.ticxo.modelengine.api.model.bone.manager.MountManager;
import com.ticxo.modelengine.api.mount.controller.MountController;
import com.ticxo.modelengine.api.utils.data.ComponentUtil;
import com.ticxo.modelengine.core.menu.SelectionManager;
import com.ticxo.modelengine.core.menu.screen.NavigationScreen;
import it.unimi.dsi.fastutil.Pair;
import java.util.Iterator;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.projectiles.ProjectileSource;

public class EntityListener implements Listener {
   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onEntityDamage(EntityDamageEvent event) {
      if (!event.isCancelled()) {
         ModeledEntity model = ModelEngineAPI.getModeledEntity(event.getEntity());
         if (model != null) {
            model.markHurt();
         }

      }
   }

   @EventHandler
   public void onEntityAttacked(EntityDamageByEntityEvent event) {
      Entity target = event.getDamager();
      if (target instanceof Player) {
         Player player = (Player)target;
         if (SelectionManager.isSelecting(player)) {
            target = event.getEntity();
            event.setCancelled(true);
            SelectionManager.setSelecting(player, false);
            SelectionManager.setSelected(player, target);
            player.sendActionBar(ComponentUtil.base(Component.text("Selected " + target.getName() + ".")));
            (new NavigationScreen(player)).openScreen();
            return;
         }
      }

      Pair<ActiveModel, MountController> pair = ModelEngineAPI.getMountPairManager().get(event.getDamager().getUniqueId());
      if (pair != null) {
         Object base = ((ActiveModel)pair.left()).getModeledEntity().getBase().getOriginal();
         MountController controller = (MountController)pair.right();
         if (base.equals(event.getEntity()) && !controller.canDamageMount()) {
            event.setCancelled(true);
         }

      }
   }

   @EventHandler
   public void onEntityInteracted(PlayerInteractEntityEvent event) {
      Pair<ActiveModel, MountController> pair = ModelEngineAPI.getMountPairManager().get(event.getPlayer().getUniqueId());
      if (pair != null) {
         Object base = ((ActiveModel)pair.left()).getModeledEntity().getBase().getOriginal();
         MountController controller = (MountController)pair.right();
         if (base.equals(event.getRightClicked()) && !controller.canDamageMount()) {
            event.setCancelled(true);
         }

      }
   }

   @EventHandler
   public void onProjectileHit(ProjectileHitEvent event) {
      ProjectileSource var3 = event.getEntity().getShooter();
      if (var3 instanceof Entity) {
         Entity entity = (Entity)var3;
         if (event.getHitEntity() != null) {
            Pair<ActiveModel, MountController> pair = ModelEngineAPI.getMountPairManager().get(entity.getUniqueId());
            if (pair == null) {
               return;
            }

            Object base = ((ActiveModel)pair.left()).getModeledEntity().getBase().getOriginal();
            MountController controller = (MountController)pair.right();
            if (base.equals(event.getHitEntity()) && !controller.canDamageMount()) {
               event.setCancelled(true);
            }
         }
      }

   }

   @EventHandler
   public void onPortal(EntityPortalEvent event) {
      Entity entity = event.getEntity();
      if (ModelEngineAPI.getMountPairManager().get(entity.getUniqueId()) != null) {
         event.setCancelled(true);
      } else {
         ModeledEntity modeledEntity = ModelEngineAPI.getModeledEntity(entity);
         if (modeledEntity != null) {
            Iterator var4 = modeledEntity.getModels().values().iterator();

            Optional manager;
            do {
               if (!var4.hasNext()) {
                  return;
               }

               ActiveModel model = (ActiveModel)var4.next();
               manager = model.getMountManager();
            } while(manager.isEmpty() || !((MountManager)((BehaviorManager)manager.get())).hasPassengers());

            event.setCancelled(true);
         }
      }
   }

   @EventHandler
   public void onTeleport(EntityTeleportEvent event) {
      if (!(event instanceof EntityPortalEvent)) {
         ModelEngineAPI.getMountPairManager().tryDismount(event.getEntity());
      }
   }

   @EventHandler
   public void onDeath(EntityDeathEvent event) {
      ModelEngineAPI.getMountPairManager().tryDismount(event.getEntity());
   }
}
