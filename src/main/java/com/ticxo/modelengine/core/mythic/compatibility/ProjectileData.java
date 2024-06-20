package com.ticxo.modelengine.core.mythic.compatibility;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.entity.CullType;
import com.ticxo.modelengine.api.entity.data.AbstractEntityData;
import io.lumine.mythic.api.skills.IParentSkill;
import io.lumine.mythic.bukkit.BukkitAdapter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class ProjectileData extends AbstractEntityData {
   private final ProjectileEntity<?> projectileEntity;
   private final AtomicBoolean isAlive = new AtomicBoolean(true);
   private final AtomicReference<Location> location = new AtomicReference();
   private final Set<Player> syncTracking = new HashSet();
   private final Map<Player, CullType> asyncTracking = Maps.newConcurrentMap();
   private final Queue<Player> startTrackingQueue = new ConcurrentLinkedQueue();
   private final Set<Player> startTracking = new HashSet();
   private final Queue<Player> stopTrackingQueue = new ConcurrentLinkedQueue();
   private final Set<Player> stopTracking = new HashSet();

   public ProjectileData(ProjectileEntity<?> projectileEntity) {
      this.projectileEntity = projectileEntity;
      this.syncUpdate();
      this.asyncUpdate();
      ModelEngineAPI.getAPI().getDataTrackers().execute(projectileEntity.getUUID(), (uuid, tracker) -> {
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
      Location location = BukkitAdapter.adapt(this.projectileEntity.getOriginal().getCurrentLocation());
      this.location.set(location);
      this.isAlive.set(!((IParentSkill)this.projectileEntity.getOriginal()).getCancelled());
      if (!this.isDataValid()) {
         this.projectileEntity.removeSelf();
      }

      HashSet<Player> updatedTracking = new HashSet();
      int radiusSqr = this.projectileEntity.getRenderRadius() * this.projectileEntity.getRenderRadius();
      Iterator var4 = Bukkit.getOnlinePlayers().iterator();

      while(var4.hasNext()) {
         Player player = (Player)var4.next();
         Location playerLocation = player.getLocation();
         if (this.getLocation().getWorld().equals(playerLocation.getWorld()) && playerLocation.distanceSquared(location) <= (double)radiusSqr) {
            updatedTracking.add(player);
         }
      }

      HashSet<Player> all = new HashSet(this.syncTracking);
      all.addAll(updatedTracking);
      Iterator var8 = all.iterator();

      while(var8.hasNext()) {
         Player player = (Player)var8.next();
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
      return this.isAlive.get();
   }

   public Location getLocation() {
      return (Location)this.location.get();
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
}
