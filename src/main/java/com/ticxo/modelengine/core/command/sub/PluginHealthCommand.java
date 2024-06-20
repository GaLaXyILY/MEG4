package com.ticxo.modelengine.core.command.sub;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.command.AbstractCommand;
import com.ticxo.modelengine.api.entity.EntityDataTrackers;
import com.ticxo.modelengine.api.model.ModelUpdaters;
import com.ticxo.modelengine.core.command.MECommand;
import java.util.Iterator;
import java.util.List;
import org.bukkit.command.CommandSender;

public class PluginHealthCommand extends AbstractCommand {
   public PluginHealthCommand(AbstractCommand parent) {
      super(parent);
   }

   public boolean onCommand(CommandSender sender, String[] args) {
      ModelUpdaters updaters = ModelEngineAPI.getAPI().getModelUpdaters();
      EntityDataTrackers trackers = ModelEngineAPI.getAPI().getDataTrackers();
      MECommand.logSender(sender, "Model Updaters:");
      Iterator var5 = updaters.getAvailable().iterator();

      while(var5.hasNext()) {
         ModelUpdaters.Updater updater = (ModelUpdaters.Updater)var5.next();
         MECommand.logSender(sender, String.format("- %s: %s (%sms)", updater.getId(), updater.getLoad(), updater.getTimings()));
      }

      MECommand.logSender(sender, "");
      MECommand.logSender(sender, "Entity Data Trackers:");
      var5 = trackers.getAvailable().iterator();

      while(var5.hasNext()) {
         EntityDataTrackers.Tracker tracker = (EntityDataTrackers.Tracker)var5.next();
         MECommand.logSender(sender, String.format("- %s: %s (%sms)", tracker.getId(), tracker.getLoad(), tracker.getTimings()));
      }

      return true;
   }

   public List<String> onTabComplete(CommandSender sender, String[] args) {
      return null;
   }

   public String getPermissionNode() {
      return "modelengine.command.health";
   }

   public boolean isConsoleFriendly() {
      return true;
   }

   public String getName() {
      return "health";
   }
}
