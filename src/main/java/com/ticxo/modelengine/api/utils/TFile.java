package com.ticxo.modelengine.api.utils;

import com.google.common.io.ByteStreams;
import com.ticxo.modelengine.api.utils.data.ResourceLocation;
import com.ticxo.modelengine.api.utils.logger.TLogger;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;
import javax.imageio.ImageIO;
import org.bukkit.plugin.java.JavaPlugin;

public class TFile {
   public static final String SEP = System.getProperty("file.separator");

   public static void copyResource(JavaPlugin plugin, File file, String path) {
      if (!file.exists()) {
         try {
            OutputStream writer = new FileOutputStream(file);
            InputStream reader = plugin.getResource(path);
            if (reader != null) {
               ByteStreams.copy(reader, writer);
            }

            writer.close();
         } catch (IOException var5) {
            var5.printStackTrace();
         }
      }

   }

   public static void copyResource(JavaPlugin plugin, File origin, String resourceOrigin, String path) {
      copyResource(plugin, createFile(origin, path), resourceOrigin + "/" + path);
   }

   public static String createPath(String... path) {
      if (path.length == 0) {
         return "";
      } else {
         StringBuilder builder = new StringBuilder(path[0]);

         for(int i = 1; i < path.length; ++i) {
            builder.append(SEP).append(path[i]);
         }

         return builder.toString();
      }
   }

   public static File createDirectory(File parent, String... path) {
      String compactPath = createPath(path);
      File file = new File(parent, compactPath);
      if (!file.exists() && !file.mkdirs()) {
         TLogger.log("Failed to create directory: " + compactPath);
      }

      return file;
   }

   public static File createFile(File parent, String... path) {
      String compactPath = createPath(path);
      File file = new File(parent, compactPath);
      if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
         TLogger.log("Failed to create file: " + compactPath);
      }

      return file;
   }

   public static File createFileOrEmpty(File parent, String... path) {
      String compactPath = createPath(path);
      File file = new File(parent, compactPath);

      try {
         if (!file.getParentFile().exists() && !file.getParentFile().mkdirs() || !file.exists() && !file.createNewFile()) {
            TLogger.log("Failed to create file: " + compactPath);
         }
      } catch (IOException var5) {
         var5.printStackTrace();
      }

      return file;
   }

   public static File createFile(File parent, String scope, ResourceLocation location, String extension) {
      String var10002 = parent.getPath() + SEP + location.getNamespace() + SEP + scope;
      String var10003 = location.getPath();
      File file = new File(var10002, var10003 + "." + extension);
      if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
         TLogger.log("Failed to create file.");
      }

      return file;
   }

   public static void recreateFile(File file) {
      try {
         if (!file.getParentFile().exists() && !file.getParentFile().mkdirs() || !file.exists() && !file.createNewFile()) {
            TLogger.log("Failed to create file: " + file.getName());
         }
      } catch (IOException var2) {
         var2.printStackTrace();
      }

   }

   public static BufferedImage toImage(String data) {
      String[] d = data.split(",");
      return d.length > 1 ? rawToImage(d[1]) : rawToImage(data);
   }

   public static BufferedImage rawToImage(String data) {
      try {
         byte[] imageBytes = Base64.getDecoder().decode(data);
         return ImageIO.read(new ByteArrayInputStream(imageBytes));
      } catch (IOException var2) {
         var2.printStackTrace();
         return null;
      }
   }

   public static String removeExtension(String path) {
      int id = path.lastIndexOf(".");
      return id == -1 ? path : path.substring(0, id);
   }

   public static boolean isExtension(String path, String extensions) {
      int id = path.lastIndexOf(".");
      return id == -1 ? false : path.substring(id + 1).equalsIgnoreCase(extensions);
   }
}
