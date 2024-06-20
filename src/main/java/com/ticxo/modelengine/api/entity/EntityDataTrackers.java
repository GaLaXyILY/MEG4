package com.ticxo.modelengine.api.entity;

import com.google.common.collect.Maps;
import com.ticxo.modelengine.api.entity.data.IEntityData;
import com.ticxo.modelengine.api.utils.config.ConfigProperty;
import com.ticxo.modelengine.api.utils.logger.TLogger;
import com.ticxo.modelengine.api.utils.scheduling.PlatformScheduler;
import com.ticxo.modelengine.api.utils.ticker.AbstractLoadBalancer;
import com.ticxo.modelengine.api.utils.ticker.DualTicker;
import com.ticxo.modelengine.api.utils.ticker.LoadBalancer;
import com.ticxo.modelengine.api.utils.ticker.PseudoThread;
import com.ticxo.modelengine.api.utils.ticker.Task;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import org.bukkit.plugin.java.JavaPlugin;

public class EntityDataTrackers extends AbstractLoadBalancer<UUID, EntityDataTrackers.Tracker> {
   private final Map<EntityDataTrackers.Tracker, PseudoThread> threads = new HashMap();
   private final JavaPlugin plugin;
   private final PlatformScheduler scheduler;
   private final int maximumThreads;
   private int trackerCount = 0;
   private boolean started;

   public EntityDataTrackers(JavaPlugin plugin, PlatformScheduler scheduler) {
      super(ConfigProperty.CULLING_THREADS.getInt());
      this.plugin = plugin;
      this.scheduler = scheduler;
      this.maximumThreads = Math.max(ConfigProperty.MAX_CULLING_THREADS.getInt(), this.available.size());
      this.available.forEach(this::setup);
   }

   public EntityDataTrackers.Tracker supply() {
      return new EntityDataTrackers.Tracker();
   }

   public void start() {
      this.started = true;
      Iterator var1 = this.threads.entrySet().iterator();

      while(var1.hasNext()) {
         Entry<EntityDataTrackers.Tracker, PseudoThread> entry = (Entry)var1.next();
         ((PseudoThread)entry.getValue()).start();
      }

   }

   public void end() {
      Iterator var1 = this.threads.entrySet().iterator();

      while(var1.hasNext()) {
         Entry<EntityDataTrackers.Tracker, PseudoThread> entry = (Entry)var1.next();
         ((PseudoThread)entry.getValue()).end();
      }

   }

   private void setup(EntityDataTrackers.Tracker tracker) {
      tracker.setId("data_tracker_" + this.trackerCount);
      PseudoThread thread = new PseudoThread(tracker.getId(), this.scheduler, this.plugin, true, 0, 0, true, false);
      thread.registerOverloadCallback((skipped) -> {
         String var10000 = tracker.getId();
         TLogger.debug(var10000 + " is overloaded with " + tracker.getLoad() + " targets.");
         if (this.available.size() < this.maximumThreads) {
            this.growAndBalance(tracker, skipped);
         }

      });
      thread.queueTask(new Task((task) -> {
         tracker.asyncFetchEntityData();
      }, 0, 0, true));
      Objects.requireNonNull(tracker);
      DualTicker.queueRepeatingSyncTask((Runnable)(tracker::fetchEntityData), 0, 0);
      this.threads.put(tracker, thread);
      if (this.started) {
         thread.start();
      }

      ++this.trackerCount;
   }

   private void growAndBalance(EntityDataTrackers.Tracker request, int skipped) {
      TLogger.debug(request.id + " has skipped " + skipped + " ticks. Requested a new server.");
      EntityDataTrackers.Tracker handle = this.supply();
      int moved = request.dataTrackers.size() / 2;

      for(Iterator var5 = request.dataTrackers.entrySet().iterator(); var5.hasNext(); --moved) {
         Entry<UUID, IEntityData> entry = (Entry)var5.next();
         if (moved <= 0) {
            break;
         }

         handle.putEntityData((UUID)entry.getKey(), (IEntityData)entry.getValue());
         this.reference.put((UUID)entry.getKey(), handle);
      }

      Set var10000 = handle.dataTrackers.keySet();
      Map var10001 = request.dataTrackers;
      Objects.requireNonNull(var10001);
      var10000.forEach(var10001::remove);
      this.available.add(handle);
      this.setup(handle);
      TLogger.debug("- Created " + handle.id + ".");
   }

   public class Tracker implements LoadBalancer.Server {
      private final Map<UUID, IEntityData> dataTrackers = Maps.newConcurrentMap();
      private String id = "Unknown";
      private int lastSize = 0;
      private long timings;

      public void fetchEntityData() {
         Iterator var1 = this.dataTrackers.entrySet().iterator();

         while(var1.hasNext()) {
            Entry<UUID, IEntityData> entry = (Entry)var1.next();
            IEntityData data = (IEntityData)entry.getValue();
            if (!data.isDataValid()) {
               data.destroy();
               this.dataTrackers.remove(entry.getKey());
               EntityDataTrackers.this.unregister((UUID)entry.getKey());
            } else {
               data.syncUpdate();
            }
         }

      }

      public void asyncFetchEntityData() {
         long time = System.currentTimeMillis();
         Iterator var3 = this.dataTrackers.entrySet().iterator();

         while(var3.hasNext()) {
            Entry<UUID, IEntityData> entry = (Entry)var3.next();
            IEntityData data = (IEntityData)entry.getValue();
            data.cullUpdate();
         }

         this.timings = System.currentTimeMillis() - time;
         if (this.lastSize != this.dataTrackers.size()) {
            this.lastSize = this.dataTrackers.size();
            TLogger.debug(this.id + ": " + this.lastSize + " - " + this.timings);
         }

      }

      public void putEntityData(UUID uuid, IEntityData dataTracker) {
         this.dataTrackers.put(uuid, dataTracker);
      }

      public IEntityData getEntityData(UUID uuid) {
         return (IEntityData)this.dataTrackers.get(uuid);
      }

      public IEntityData removeEntityData(UUID uuid) {
         EntityDataTrackers.this.unregister(uuid);
         return (IEntityData)this.dataTrackers.remove(uuid);
      }

      public int getLoad() {
         return this.lastSize;
      }

      public Map<UUID, IEntityData> getDataTrackers() {
         return this.dataTrackers;
      }

      public String getId() {
         return this.id;
      }

      public void setId(String id) {
         this.id = id;
      }

      public long getTimings() {
         return this.timings;
      }
   }
}
