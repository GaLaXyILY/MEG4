package com.ticxo.modelengine.api.utils.scheduling;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

public interface PlatformScheduler {
   PlatformTask scheduleRepeating(Plugin var1, Runnable var2, long var3, long var5);

   PlatformTask scheduleRepeating(Plugin var1, Entity var2, Runnable var3, long var4, long var6);

   PlatformTask scheduleRepeating(Plugin var1, Location var2, Runnable var3, long var4, long var6);

   PlatformTask scheduleRepeatingAsync(Plugin var1, Runnable var2, long var3, long var5);
}
