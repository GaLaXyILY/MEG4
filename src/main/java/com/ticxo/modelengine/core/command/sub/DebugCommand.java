package com.ticxo.modelengine.core.command.sub;

import com.ticxo.modelengine.api.command.AbstractCommand;
import com.ticxo.modelengine.api.utils.config.DebugToggle;
import com.ticxo.modelengine.api.utils.logger.LogColor;
import com.ticxo.modelengine.core.command.MECommand;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class DebugCommand extends AbstractCommand {
   public DebugCommand(AbstractCommand parent) {
      super(parent);
   }

   public boolean onCommand(CommandSender sender, String[] args) {
      if (args.length == 0) {
         return false;
      } else {
         DebugToggle debugToggle = DebugToggle.get(args[0]);
         if (debugToggle == null) {
            MECommand.logSender(sender, ChatColor.RED + "[ModelEngine] Unknown debug: " + args[0] + ".", LogColor.RED + "Unknown debug: " + args[0] + ".");
            return false;
         } else {
            String var10001;
            LogColor var10002;
            if (args.length == 1) {
               ChatColor var5 = ChatColor.GREEN;
               var10001 = var5 + "[ModelEngine] " + debugToggle.name() + " is " + (DebugToggle.isDebugging(debugToggle) ? "enabled." : "disabled.");
               var10002 = LogColor.BRIGHT_GREEN;
               MECommand.logSender(sender, var10001, var10002 + debugToggle.name() + " is " + (DebugToggle.isDebugging(debugToggle) ? "enabled." : "disabled."));
               return true;
            } else {
               boolean flag = Boolean.parseBoolean(args[1]);
               DebugToggle.setDebug(debugToggle, flag);
               var10001 = ChatColor.GREEN + "[ModelEngine] Set " + debugToggle.name() + " to " + flag + ".";
               var10002 = LogColor.BRIGHT_GREEN;
               MECommand.logSender(sender, var10001, var10002 + "Set " + debugToggle.name() + " to " + flag + ".");
               return true;
            }
         }
      }
   }

   public List<String> onTabComplete(CommandSender sender, String[] args) {
      ArrayList<String> list = new ArrayList();
      switch(args.length) {
      case 1:
         DebugToggle[] var4 = DebugToggle.values();
         int var5 = var4.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            DebugToggle debug = var4[var6];
            list.add(debug.name());
         }

         return list;
      case 2:
         list.add("true");
         list.add("false");
      }

      return list;
   }

   public String getPermissionNode() {
      return "modelengine.command.debug";
   }

   public boolean isConsoleFriendly() {
      return true;
   }

   public String getName() {
      return "debug";
   }
}
