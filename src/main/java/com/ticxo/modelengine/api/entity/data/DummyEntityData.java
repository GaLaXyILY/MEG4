package com.ticxo.modelengine.api.entity.data;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.entity.CullType;
import com.ticxo.modelengine.api.entity.Dummy;
import com.ticxo.modelengine.api.nms.impl.DummyTrackedEntity;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class DummyEntityData<T> extends AbstractEntityData {
   protected final Dummy<T> dummy;
   protected final DummyTrackedEntity tracked;
   protected final Set<Player> syncTracking = new HashSet();
   protected final Map<Player, CullType> asyncTracking = Maps.newConcurrentMap();
   protected final Queue<Player> startTrackingQueue = new ConcurrentLinkedQueue();
   protected final Set<Player> startTracking = new HashSet();
   protected final Queue<Player> stopTrackingQueue = new ConcurrentLinkedQueue();
   protected final Set<Player> stopTracking = new HashSet();
   protected Location location;

   public DummyEntityData(Dummy<T> dummy) {
      this.dummy = dummy;
      this.tracked = new DummyTrackedEntity();
      this.syncUpdate();
      this.asyncUpdate();
      ModelEngineAPI.getAPI().getDataTrackers().execute(dummy.getUUID(), (uuid, tracker) -> {
         tracker.putEntityData(uuid, this);
      });
   }

   public void asyncUpdate() {
      Player player;
      while(!this.startTrackingQueue.isEmpty()) {
         player = (Player)this.startTrackingQueue.poll();
         this.startTracking.add(player);
         this.asyncTracking.put(player, CullType.NO_CULL);
      }

      while(!this.stopTrackingQueue.isEmpty()) {
         player = (Player)this.stopTrackingQueue.poll();
         this.stopTracking.add(player);
         if (this.asyncTracking.get(player) != CullType.CULLED) {
            this.asyncTracking.remove(player);
         }
      }

   }

   public void syncUpdate() {
      if (this.dummy.isDetectingPlayers()) {
         this.tracked.detectPlayers(this.location);
      }

      Set<Player> updatedTracking = this.tracked.getTrackedPlayer((playerx) -> {
         return this.asyncTracking.get(playerx) != CullType.CULLED;
      });
      HashSet<Player> all = new HashSet(this.syncTracking);
      all.addAll(updatedTracking);
      Iterator var3 = all.iterator();

      while(var3.hasNext()) {
         Player player = (Player)var3.next();
         if (!this.syncTracking.contains(player)) {
            this.startTrackingQueue.add(player);
         } else if (!updatedTracking.contains(player)) {
            this.stopTrackingQueue.add(player);
         }
      }

      this.syncTracking.clear();
      this.syncTracking.addAll(updatedTracking);
   }

   public void cullUpdate() {
   }

   public void cleanup() {
      this.startTracking.clear();
      this.stopTracking.clear();
   }

   public void destroy() {
   }

   public boolean isDataValid() {
      return this.dummy.isAlive();
   }

   public List<Entity> getPassengers() {
      return List.of();
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

   public int getRenderRadius() {
      return this.tracked.getBaseRange();
   }

   public void setRenderRadius(int radius) {
      this.tracked.setBaseRange(radius);
   }

   public DummyTrackedEntity getTracked() {
      return this.tracked;
   }

   public Location getLocation() {
      return this.location;
   }

   public void setLocation(Location location) {
      this.location = location;
   }
}
