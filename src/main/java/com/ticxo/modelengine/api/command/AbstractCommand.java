package com.ticxo.modelengine.api.command;

import com.ticxo.modelengine.api.ModelEngineAPI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractCommand implements TabExecutor {
   protected final ModelEngineAPI plugin;
   private final Map<String, AbstractCommand> subCommands;
   private final Map<String, AbstractCommand> subCommandAliases;

   public AbstractCommand(AbstractCommand parent) {
      this(parent.getPlugin());
   }

   public AbstractCommand(ModelEngineAPI plugin) {
      this.subCommands = new HashMap();
      this.subCommandAliases = new HashMap();
      this.plugin = plugin;
   }

   public final void addSubCommands(AbstractCommand... commands) {
      AbstractCommand[] var2 = commands;
      int var3 = commands.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         AbstractCommand command = var2[var4];
         this.subCommands.put(command.getName(), command);
         String[] var6 = command.getAliases();
         int var7 = var6.length;

         for(int var8 = 0; var8 < var7; ++var8) {
            String alias = var6[var8];
            this.subCommandAliases.put(alias, command);
         }
      }

   }

   public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
      if (this.getPermissionNode() != null && !sender.hasPermission(this.getPermissionNode()) && !sender.hasPermission("modelengine.admin")) {
         sender.sendMessage(Component.text("You don't have permission to do this!").color(NamedTextColor.RED));
         return true;
      } else if (!this.isConsoleFriendly() && !(sender instanceof Player)) {
         sender.sendMessage(Component.text("Only players can do this!").color(NamedTextColor.RED));
         return true;
      } else {
         AbstractCommand sub;
         if (args.length > 0 && this.subCommands.get(args[0].toLowerCase()) != null) {
            sub = (AbstractCommand)this.subCommands.get(args[0].toLowerCase());
            return sub.onCommand(sender, cmd, label, (String[])Arrays.copyOfRange(args, 1, args.length));
         } else if (args.length > 0 && this.subCommandAliases.get(args[0].toLowerCase()) != null) {
            sub = (AbstractCommand)this.subCommandAliases.get(args[0].toLowerCase());
            return sub.onCommand(sender, cmd, label, (String[])Arrays.copyOfRange(args, 1, args.length));
         } else {
            return this.onCommand(sender, args);
         }
      }
   }

   public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
      if (this.getPermissionNode() != null && !sender.hasPermission(this.getPermissionNode())) {
         return null;
      } else {
         AbstractCommand sub;
         if (args.length > 1 && this.subCommands.get(args[0].toLowerCase()) != null) {
            sub = (AbstractCommand)this.subCommands.get(args[0].toLowerCase());
            return sub.onTabComplete(sender, cmd, label, (String[])Arrays.copyOfRange(args, 1, args.length));
         } else if (args.length > 1 && this.subCommandAliases.get(args[0].toLowerCase()) != null) {
            sub = (AbstractCommand)this.subCommandAliases.get(args[0].toLowerCase());
            return sub.onTabComplete(sender, cmd, label, (String[])Arrays.copyOfRange(args, 1, args.length));
         } else {
            List<String> result = this.onTabComplete(sender, args);
            if (result == null && args.length == 1) {
               result = new ArrayList();
               StringUtil.copyPartialMatches(args[0], this.subCommands.keySet(), (Collection)result);
            }

            return (List)result;
         }
      }
   }

   public abstract boolean onCommand(CommandSender var1, String[] var2);

   public abstract List<String> onTabComplete(CommandSender var1, String[] var2);

   public abstract String getPermissionNode();

   public abstract boolean isConsoleFriendly();

   public String[] getAliases() {
      return new String[0];
   }

   public abstract String getName();

   protected ModelEngineAPI getPlugin() {
      return this.plugin;
   }
}
