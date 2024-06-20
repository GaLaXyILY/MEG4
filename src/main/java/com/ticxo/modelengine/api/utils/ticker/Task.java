package com.ticxo.modelengine.api.utils.ticker;

import java.util.function.Consumer;

public class Task {
   private final Consumer<Task> task;
   private final int startDelay;
   private final int interval;
   private final boolean isRepeating;
   private int delay;
   private int tick;
   private int runCount;
   private boolean canceled;

   public boolean tick() {
      if (this.delay++ >= this.startDelay && this.tick-- <= 0) {
         this.tick = this.interval;
         this.task.accept(this);
         ++this.runCount;
         return this.canceled || !this.isRepeating;
      } else {
         return this.canceled;
      }
   }

   public void cancel() {
      this.canceled = true;
   }

   public Task(Consumer<Task> task, int startDelay, int interval, boolean isRepeating) {
      this.task = task;
      this.startDelay = startDelay;
      this.interval = interval;
      this.isRepeating = isRepeating;
   }

   public int getStartDelay() {
      return this.startDelay;
   }

   public int getInterval() {
      return this.interval;
   }

   public boolean isRepeating() {
      return this.isRepeating;
   }

   public int getDelay() {
      return this.delay;
   }

   public int getTick() {
      return this.tick;
   }

   public int getRunCount() {
      return this.runCount;
   }

   public boolean isCanceled() {
      return this.canceled;
   }
}
