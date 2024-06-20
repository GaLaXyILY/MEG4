package com.ticxo.modelengine.api.entity.data;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.entity.CullType;
import com.ticxo.modelengine.api.entity.Hitbox;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.mount.MountPairManager;
import com.ticxo.modelengine.api.nms.entity.EntityHandler;
import com.ticxo.modelengine.api.nms.entity.wrapper.TrackedEntity;
import com.ticxo.modelengine.api.nms.impl.TempTrackedEntity;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public class BukkitEntityData extends AbstractEntityData {
   protected final EntityHandler entityHandler = ModelEngineAPI.getEntityHandler();
   protected final Entity entity;
   protected final AtomicBoolean isEntityValid = new AtomicBoolean();
   protected final AtomicBoolean isDataValid = new AtomicBoolean();
   protected final AtomicBoolean isForcedAlive = new AtomicBoolean();
   protected final AtomicReference<Location> location = new AtomicReference();
   protected final AtomicReference<List<Entity>> passengers = new AtomicReference();
   protected final Set<Player> syncTracking = new HashSet();
   protected final Map<Player, CullType> asyncTracking = Maps.newConcurrentMap();
   protected final Queue<Queue<Player>> startTrackingQueue = new ConcurrentLinkedQueue();
   protected final Set<Player> startTracking = new HashSet();
   protected final Queue<Queue<Player>> stopTrackingQueue = new ConcurrentLinkedQueue();
   protected final Set<Player> stopTracking = new HashSet();
   protected TrackedEntity tracked;
   protected int lastCulled;
   protected int syncTick;
   protected boolean wasValid;

   public BukkitEntityData(Entity entity) {
      this.entity = entity;
      this.tracked = this.entityHandler.wrapTrackedEntity(entity);
      this.syncUpdate();
      this.asyncUpdate();
      ModelEngineAPI.getAPI().getDataTrackers().execute(entity.getUniqueId(), (uuid, tracker) -> {
         tracker.putEntityData(uuid, this);
      });
   }

   public void asyncUpdate() {
      while(!this.startTrackingQueue.isEmpty() || !this.stopTrackingQueue.isEmpty()) {
         Queue playerQueue;
         Player player;
         if (!this.startTrackingQueue.isEmpty()) {
            playerQueue = (Queue)this.startTrackingQueue.poll();

            while(!playerQueue.isEmpty()) {
               player = (Player)playerQueue.poll();
               this.startTracking.add(player);
               this.stopTracking.remove(player);
               this.asyncTracking.put(player, CullType.NO_CULL);
            }
         }

         if (!this.stopTrackingQueue.isEmpty()) {
            playerQueue = (Queue)this.stopTrackingQueue.poll();

            while(!playerQueue.isEmpty()) {
               player = (Player)playerQueue.poll();
               this.stopTracking.add(player);
               this.startTracking.remove(player);
               if (this.asyncTracking.get(player) != CullType.CULLED) {
                  this.asyncTracking.remove(player);
               }
            }
         }
      }

   }

   public void syncUpdate() {
      ++this.syncTick;
      boolean valid = this.entity.isValid();
      this.wasValid |= valid || this.syncTick > 20;
      valid |= !this.wasValid;
      this.isEntityValid.set(valid);
      this.isDataValid.set(valid || !this.entityHandler.isRemoved(this.entity));
      if (this.isForcedAlive()) {
         this.entityHandler.setDeathTick(this.entity, 0);
      } else if (!this.isEntityValid()) {
         this.entityHandler.setDeathTick(this.entity, 20);
      }

      this.location.set(this.entity.getLocation());
      this.passengers.set(this.entity.getPassengers());
      Set<Player> updatedTracking = this.getTracked().getTrackedPlayer((playerx) -> {
         return this.asyncTracking.get(playerx) != CullType.CULLED;
      });
      HashSet<Player> all = new HashSet(this.syncTracking);
      ConcurrentLinkedQueue<Player> startTrack = new ConcurrentLinkedQueue();
      ConcurrentLinkedQueue<Player> stopTrack = new ConcurrentLinkedQueue();
      all.addAll(updatedTracking);
      Iterator var6 = all.iterator();

      while(var6.hasNext()) {
         Player player = (Player)var6.next();
         if (!this.syncTracking.contains(player)) {
            startTrack.add(player);
         } else if (!updatedTracking.contains(player)) {
            stopTrack.add(player);
         }
      }

      this.syncTracking.clear();
      this.syncTracking.addAll(updatedTracking);
      this.startTrackingQueue.add(startTrack);
      this.stopTrackingQueue.add(stopTrack);
   }

   public void cullUpdate() {
      this.updateCulledPlayer();
   }

   public void cleanup() {
      this.startTracking.clear();
      this.stopTracking.clear();
   }

   public void destroy() {
   }

   private void updateCulledPlayer() {
      if (--this.lastCulled <= 0) {
         this.lastCulled = this.cullInterval();
         MountPairManager pairManager = ModelEngineAPI.getMountPairManager();
         Location location = this.getLocation();
         Iterator var3 = this.asyncTracking.keySet().iterator();

         while(true) {
            while(var3.hasNext()) {
               Player player = (Player)var3.next();
               ActiveModel mounted = pairManager.getMountedPair(player.getUniqueId());
               if (mounted != null && mounted.getModeledEntity().getBase().getData() == this) {
                  this.asyncTracking.put(player, CullType.NO_CULL);
               } else if (location.getWorld() != player.getWorld()) {
                  this.asyncTracking.put(player, CullType.CULLED);
               } else {
                  Location eyeLoc = player.getEyeLocation();
                  if (this.verticalCull()) {
                     Hitbox cullBox = this.getCullHitbox();
                     double distance;
                     if (cullBox == null) {
                        distance = Math.abs(eyeLoc.getY() - location.getY());
                     } else {
                        distance = Math.max(eyeLoc.getY() - location.getY() - cullBox.getHeight(), location.getY() - eyeLoc.getY());
                     }

                     if (distance > this.verticalCullDistance()) {
                        this.asyncTracking.put(player, this.verticalCullType());
                        continue;
                     }
                  }

                  Vector delta = location.clone().subtract(eyeLoc).toVector();
                  if (this.blockedCull() && !this.entity.isGlowing() && !this.isModelGlowing() && delta.lengthSquared() > this.blockedCullIgnoreRadius() && this.entityHandler.shouldCull(player, this.entity, this.getCullHitbox())) {
                     this.asyncTracking.put(player, this.blockedCullType());
                  } else {
                     if (this.backCull()) {
                        BoundingBox bb = this.getCullHitbox() == null ? this.entity.getBoundingBox() : this.getCullHitbox().createBoundingBox(location.toVector());
                        if (bb.contains(eyeLoc.toVector())) {
                           this.asyncTracking.put(player, CullType.NO_CULL);
                           continue;
                        }

                        if (delta.lengthSquared() > this.backCullIgnoreRadius() && eyeLoc.getDirection().dot(delta.normalize()) <= this.backCullAngle()) {
                           this.asyncTracking.put(player, this.backCullType());
                           continue;
                        }
                     }

                     this.asyncTracking.put(player, CullType.NO_CULL);
                  }
               }
            }

            return;
         }
      }
   }

   public boolean isDataValid() {
      return this.isDataValid.get();
   }

   public boolean isEntityValid() {
      return this.isEntityValid.get();
   }

   public boolean isForcedAlive() {
      return this.isForcedAlive.get();
   }

   public void setForcedAlive(boolean flag) {
      this.isForcedAlive.set(flag);
   }

   public Location getLocation() {
      return ((Location)this.location.get()).clone();
   }

   public List<Entity> getPassengers() {
      return (List)this.passengers.get();
   }

   public Set<Player> getStartTracking() {
      return ImmutableSet.copyOf(this.startTracking);
   }

   public Map<Player, CullType> getTracking() {
      return ImmutableMap.copyOf(this.asyncTracking);
   }

   public Set<Player> getStopTracking() {
      return ImmutableSet.copyOf(this.stopTracking);
   }

   public TrackedEntity getTracked() {
      TrackedEntity newTracked = this.tracked;
      if (newTracked instanceof TempTrackedEntity) {
         TempTrackedEntity tempTracked = (TempTrackedEntity)newTracked;
         newTracked = this.entityHandler.wrapTrackedEntity(this.entity);
         if (!(newTracked instanceof TempTrackedEntity)) {
            if (tempTracked.getBaseRange() != -1) {
               newTracked.setBaseRange(tempTracked.getBaseRange());
            }

            newTracked.setPlayerPredicate(tempTracked.getPlayerPredicate());
            Iterator var3 = tempTracked.getForcePaired().iterator();

            Player player;
            while(var3.hasNext()) {
               player = (Player)var3.next();
               newTracked.addForcedPairing(player);
            }

            var3 = tempTracked.getForceHidden().iterator();

            while(var3.hasNext()) {
               player = (Player)var3.next();
               newTracked.addForcedHidden(player);
            }

            this.tracked = newTracked;
         }
      }

      return this.tracked;
   }
}
