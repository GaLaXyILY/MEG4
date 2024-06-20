package com.ticxo.modelengine.api.utils.scheduling;

import org.bukkit.scheduler.BukkitTask;

public class BukkitPlatformTask implements PlatformTask {
   protected BukkitTask task;

   public BukkitPlatformTask(BukkitTask task) {
      this.task = task;
   }

   public void cancel() {
      this.task.cancel();
   }
}
