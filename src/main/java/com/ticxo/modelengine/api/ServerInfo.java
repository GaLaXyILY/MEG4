package com.ticxo.modelengine.api;

import org.bukkit.Bukkit;

public class ServerInfo {
   public static final boolean IS_PAPER = classExists("com.destroystokyo.paper.VersionHistoryManager$VersionData");
   public static final boolean IS_FOLIA = classExists("io.papermc.paper.threadedregions.RegionizedServer");
   public static final String NMS_VERSION;
   public static final int VERSION_NUMBER;
   public static boolean HAS_VIAVERSION;
   public static boolean HAS_CITIZENS;

   private static boolean classExists(String path) {
      try {
         Class.forName(path);
         return true;
      } catch (ClassNotFoundException var2) {
         return false;
      }
   }

   static {
      String name = Bukkit.getServer().getClass().getPackage().getName();
      NMS_VERSION = name.substring(name.lastIndexOf(46) + 1);
      VERSION_NUMBER = Integer.parseInt(NMS_VERSION.split("_")[1]);
   }
}
