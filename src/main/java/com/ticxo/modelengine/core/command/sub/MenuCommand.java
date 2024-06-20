package com.ticxo.modelengine.core.command.sub;

import com.ticxo.modelengine.api.command.AbstractCommand;
import com.ticxo.modelengine.core.menu.screen.NavigationScreen;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MenuCommand extends AbstractCommand {
   public MenuCommand(AbstractCommand parent) {
      super(parent);
   }

   public boolean onCommand(CommandSender sender, String[] args) {
      if (sender instanceof Player) {
         Player player = (Player)sender;
         (new NavigationScreen(player)).openScreen();
      }

      return true;
   }

   public List<String> onTabComplete(CommandSender sender, String[] args) {
      return null;
   }

   public String getPermissionNode() {
      return "modelengine.command.menu";
   }

   public boolean isConsoleFriendly() {
      return false;
   }

   public String getName() {
      return "menu";
   }
}
