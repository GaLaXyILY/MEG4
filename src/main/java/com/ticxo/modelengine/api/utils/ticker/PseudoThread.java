package com.ticxo.modelengine.api.utils.ticker;

import com.ticxo.modelengine.api.utils.logger.TLogger;
import com.ticxo.modelengine.api.utils.scheduling.PlatformScheduler;
import com.ticxo.modelengine.api.utils.scheduling.PlatformTask;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import org.bukkit.plugin.java.JavaPlugin;

public class PseudoThread {
   private final String name;
   private final PlatformScheduler scheduler;
   private final JavaPlugin plugin;
   private final boolean isAsync;
   private final int delay;
   private final int period;
   private final boolean canWait;
   private final boolean canMultiTick;
   private final Queue<Task> taskQueue = new ConcurrentLinkedQueue();
   private final List<Task> tasks = new ArrayList();
   private final List<Consumer<Integer>> overloadCallback = new ArrayList();
   private PlatformTask tickTask;
   private boolean locked;
   private int skipped;
   private long lastTick;

   public void start() {
      if (this.isAsync) {
         this.tickTask = this.scheduler.scheduleRepeatingAsync(this.plugin, this::tick, (long)this.delay, (long)this.period);
      } else {
         this.tickTask = this.scheduler.scheduleRepeating(this.plugin, this::tick, (long)this.delay, (long)this.period);
      }

   }

   public void end() {
      this.taskQueue.clear();
      this.tasks.clear();
      if (this.tickTask != null) {
         this.tickTask.cancel();
      }

   }

   public void queueTask(Task task) {
      this.taskQueue.add(task);
   }

   public void registerOverloadCallback(Consumer<Integer> callback) {
      this.overloadCallback.add(callback);
   }

   private void tick() {
      long currentTick = System.currentTimeMillis();
      if (this.canMultiTick || currentTick - this.lastTick >= 45L) {
         this.lastTick = currentTick;
         if (this.locked) {
            ++this.skipped;
         } else {
            this.locked = true;

            while(!this.taskQueue.isEmpty()) {
               this.tasks.add((Task)this.taskQueue.poll());
            }

            this.tasks.removeIf(Task::tick);
            long timings = System.currentTimeMillis() - currentTick;
            this.locked = false;
            if (this.skipped > 0) {
               this.overloadCallback.forEach((callback) -> {
                  callback.accept(this.skipped);
               });
               if (!this.canWait) {
                  TLogger.debug("The pseudo thread [" + this.name + "] has skipped " + this.skipped + (this.skipped == 1 ? " tick" : " ticks") + " (" + timings + "ms). Is it overloaded?");
               }

               this.skipped = 0;
            }

         }
      }
   }

   public PseudoThread(String name, PlatformScheduler scheduler, JavaPlugin plugin, boolean isAsync, int delay, int period, boolean canWait, boolean canMultiTick) {
      this.name = name;
      this.scheduler = scheduler;
      this.plugin = plugin;
      this.isAsync = isAsync;
      this.delay = delay;
      this.period = period;
      this.canWait = canWait;
      this.canMultiTick = canMultiTick;
   }

   public String getName() {
      return this.name;
   }
}
