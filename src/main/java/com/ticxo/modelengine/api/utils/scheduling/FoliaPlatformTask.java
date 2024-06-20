package com.ticxo.modelengine.api.utils.scheduling;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

public class FoliaPlatformTask implements PlatformTask {
   protected ScheduledTask task;

   public FoliaPlatformTask(ScheduledTask task) {
      this.task = task;
   }

   public void cancel() {
      this.task.cancel();
   }
}
