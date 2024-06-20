package com.ticxo.modelengine.api.utils.scheduling;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

public class BukkitPlatformScheduler implements PlatformScheduler {
   public PlatformTask scheduleRepeating(Plugin plugin, Runnable task, long delay, long period) {
      return new BukkitPlatformTask(Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period));
   }

   public PlatformTask scheduleRepeating(Plugin plugin, Entity entity, Runnable task, long delay, long period) {
      return this.scheduleRepeating(plugin, task, delay, period);
   }

   public PlatformTask scheduleRepeating(Plugin plugin, Location location, Runnable task, long delay, long period) {
      return this.scheduleRepeating(plugin, task, delay, period);
   }

   public PlatformTask scheduleRepeatingAsync(Plugin plugin, Runnable task, long delay, long period) {
      return new BukkitPlatformTask(Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delay, period));
   }
}
