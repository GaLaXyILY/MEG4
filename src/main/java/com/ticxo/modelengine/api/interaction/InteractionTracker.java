package com.ticxo.modelengine.api.interaction;

import com.google.common.collect.Maps;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.nms.entity.HitboxEntity;
import com.ticxo.modelengine.api.utils.math.OrientedBoundingBox;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.util.Consumer;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.joml.Vector3f;

public class InteractionTracker {
   private final Map<UUID, Integer> hitboxesUUID = Maps.newConcurrentMap();
   private final Map<Integer, HitboxEntity> hitboxes = Maps.newConcurrentMap();
   private final Map<UUID, DynamicHitbox> playerRelay = Maps.newConcurrentMap();
   private final Map<Integer, ActiveModel> modelRelay = Maps.newConcurrentMap();
   private final Map<Integer, Integer> entityRelay = Maps.newConcurrentMap();

   public void raytraceHitboxes() {
      Iterator var1 = Bukkit.getOnlinePlayers().iterator();

      while(true) {
         while(var1.hasNext()) {
            Player player = (Player)var1.next();
            if (player.getGameMode() == GameMode.SPECTATOR) {
               this.playerRelay.computeIfPresent(player.getUniqueId(), (uuid, dynamicHitboxx) -> {
                  dynamicHitboxx.destroy();
                  return null;
               });
            } else {
               Vector startVec = player.getEyeLocation().toVector();
               Vector3f start = startVec.toVector3f();
               Vector3f dir = player.getEyeLocation().getDirection().toVector3f();
               double traceDist = player.getGameMode() == GameMode.CREATIVE ? 5.0D : 3.0D;
               double dist = Double.MAX_VALUE;
               HitboxEntity closestHitbox = null;
               RayTraceResult closestResult = null;
               Iterator var12 = this.hitboxes.values().iterator();

               while(var12.hasNext()) {
                  HitboxEntity hitbox = (HitboxEntity)var12.next();
                  if (hitbox.getLocation().getWorld() == player.getWorld() && (Integer)this.entityRelay.getOrDefault(hitbox.getEntityId(), player.getEntityId() + 1) != player.getEntityId() && hitbox.getBone().getActiveModel().getModeledEntity().getBase().getEntityId() != player.getEntityId()) {
                     OrientedBoundingBox obb = hitbox.getOrientedBoundingBox();
                     if (obb != null) {
                        RayTraceResult result = obb.rayTrace(start, dir, traceDist, (Consumer)null);
                        if (result != null) {
                           double hitDistSqr = result.getHitPosition().distanceSquared(startVec);
                           if (!(dist < hitDistSqr)) {
                              closestHitbox = hitbox;
                              closestResult = result;
                           }
                        }
                     }
                  }
               }

               if (closestHitbox == null) {
                  this.playerRelay.computeIfPresent(player.getUniqueId(), (uuid, dynamicHitboxx) -> {
                     dynamicHitboxx.destroy();
                     return null;
                  });
               } else {
                  Vector hitPos = closestResult.getHitPosition();
                  DynamicHitbox dynamicHitbox = (DynamicHitbox)this.playerRelay.computeIfAbsent(player.getUniqueId(), (uuid) -> {
                     return new DynamicHitbox(player, hitPos);
                  });
                  dynamicHitbox.setTarget(closestHitbox.getEntityId());
                  dynamicHitbox.update(hitPos);
               }
            }
         }

         return;
      }
   }

   public void addHitbox(HitboxEntity hitbox) {
      this.hitboxesUUID.put(hitbox.getUniqueId(), hitbox.getEntityId());
      this.hitboxes.put(hitbox.getEntityId(), hitbox);
   }

   public void removeHitbox(UUID uuid) {
      Integer id = (Integer)this.hitboxesUUID.remove(uuid);
      if (id != null) {
         this.hitboxes.remove(id);
      }

   }

   public void removeHitbox(int id) {
      HitboxEntity hitbox = (HitboxEntity)this.hitboxes.remove(id);
      if (hitbox != null) {
         this.hitboxesUUID.remove(hitbox.getUniqueId());
      }

   }

   public HitboxEntity getHitbox(UUID uuid) {
      Integer id = (Integer)this.hitboxesUUID.get(uuid);
      return id == null ? null : this.getHitbox(id);
   }

   public HitboxEntity getHitbox(int id) {
      return (HitboxEntity)this.hitboxes.get(id);
   }

   public void removeDynamicHitbox(UUID uuid) {
      this.playerRelay.remove(uuid);
   }

   public DynamicHitbox getDynamicHitbox(UUID uuid) {
      return (DynamicHitbox)this.playerRelay.get(uuid);
   }

   public void setModelRelay(int id, ActiveModel activeModel) {
      this.modelRelay.put(id, activeModel);
   }

   public void removeModelRelay(int id) {
      ActiveModel var10000 = (ActiveModel)this.modelRelay.remove(id);
   }

   public ActiveModel getModelRelay(int id) {
      return (ActiveModel)this.modelRelay.get(id);
   }

   public void setEntityRelay(int id, Integer hitboxId) {
      this.entityRelay.put(id, hitboxId);
   }

   public void removeEntityRelay(int id) {
      this.entityRelay.remove(id);
   }

   public Integer getEntityRelay(int id) {
      return (Integer)this.entityRelay.get(id);
   }
}
