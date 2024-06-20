package com.ticxo.modelengine.core.command.sub;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.command.AbstractCommand;
import com.ticxo.modelengine.api.generator.ModelGenerator;
import com.ticxo.modelengine.api.utils.config.ConfigManager;
import com.ticxo.modelengine.api.utils.logger.LogColor;
import com.ticxo.modelengine.api.utils.logger.TLogger;
import com.ticxo.modelengine.core.command.MECommand;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

public class ReloadCommand extends AbstractCommand {
   public ReloadCommand(AbstractCommand parent) {
      super(parent);
   }

   public boolean onCommand(CommandSender sender, String[] args) {
      if (args.length == 0) {
         this.reloadConfig(sender);
         this.reloadModels(sender);
         return true;
      } else {
         String var3 = args[0];
         byte var4 = -1;
         switch(var3.hashCode()) {
         case -1354792126:
            if (var3.equals("config")) {
               var4 = 1;
            }
            break;
         case -1068799382:
            if (var3.equals("models")) {
               var4 = 0;
            }
         }

         switch(var4) {
         case 0:
            this.reloadModels(sender);
            break;
         case 1:
            this.reloadConfig(sender);
            break;
         default:
            return false;
         }

         return true;
      }
   }

   private void reloadConfig(CommandSender sender) {
      ConfigManager config = ModelEngineAPI.getAPI().getConfigManager();
      config.reload();
      config.updateReferences();
      MECommand.logSender(sender, ChatColor.GREEN + "[ModelEngine] Config reloaded.", LogColor.BRIGHT_GREEN + "Config reloaded.");
   }

   private void reloadModels(CommandSender sender) {
      ModelGenerator generator = ModelEngineAPI.getAPI().getModelGenerator();
      generator.importModels(false);
      generator.queueTask(ModelGenerator.Phase.POST_IMPORT, () -> {
         String msg = ModelEngineAPI.getAPI().getModelRegistry().getKeys().size() + " models loaded.";
         if (sender instanceof Entity) {
            sender.sendMessage(ChatColor.GREEN + "[ModelEngine] " + msg);
         } else {
            TLogger.log();
            TLogger.log(LogColor.BRIGHT_GREEN + msg);
         }

      });
   }

   public List<String> onTabComplete(CommandSender sender, String[] args) {
      return List.of("models", "config");
   }

   public String getPermissionNode() {
      return "modelengine.command.reload";
   }

   public boolean isConsoleFriendly() {
      return true;
   }

   public String getName() {
      return "reload";
   }
}
