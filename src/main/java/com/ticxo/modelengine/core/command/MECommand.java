package com.ticxo.modelengine.core.command;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.animation.property.IAnimationProperty;
import com.ticxo.modelengine.api.command.AbstractCommand;
import com.ticxo.modelengine.api.generator.blueprint.ModelBlueprint;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.utils.logger.TLogger;
import com.ticxo.modelengine.core.command.sub.DebugCommand;
import com.ticxo.modelengine.core.command.sub.DisguiseCommand;
import com.ticxo.modelengine.core.command.sub.MenuCommand;
import com.ticxo.modelengine.core.command.sub.PluginHealthCommand;
import com.ticxo.modelengine.core.command.sub.ReloadCommand;
import com.ticxo.modelengine.core.command.sub.SummonCommand;
import com.ticxo.modelengine.core.command.sub.UndisguiseCommand;
import java.util.Iterator;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

public class MECommand extends AbstractCommand {
   public MECommand(ModelEngineAPI plugin) {
      super(plugin);
      this.addSubCommands(new AbstractCommand[]{new ReloadCommand(this)});
      this.addSubCommands(new AbstractCommand[]{new SummonCommand(this)});
      this.addSubCommands(new AbstractCommand[]{new DisguiseCommand(this)});
      this.addSubCommands(new AbstractCommand[]{new UndisguiseCommand(this)});
      this.addSubCommands(new AbstractCommand[]{new DebugCommand(this)});
      this.addSubCommands(new AbstractCommand[]{new MenuCommand(this)});
      this.addSubCommands(new AbstractCommand[]{new PluginHealthCommand(this)});
   }

   public static void getModelIdTabComplete(List<String> list, String arg) {
      Iterator var2 = ModelEngineAPI.getAPI().getModelRegistry().getKeys().iterator();

      while(var2.hasNext()) {
         String id = (String)var2.next();
         if (id.startsWith(arg)) {
            list.add(id);
         }
      }

   }

   public static void getModelIdTabComplete(List<String> list, String arg, ModeledEntity modeledEntity) {
      Iterator var3 = modeledEntity.getModels().keySet().iterator();

      while(var3.hasNext()) {
         String id = (String)var3.next();
         if (id.startsWith(arg)) {
            list.add(id);
         }
      }

   }

   public static void getStateTabComplete(List<String> list, String arg, ActiveModel model) {
      Iterator var3 = model.getAnimationHandler().getAnimations().values().iterator();

      while(var3.hasNext()) {
         IAnimationProperty animation = (IAnimationProperty)var3.next();
         String name = animation.getName();
         if (name.startsWith(arg)) {
            list.add(name);
         }
      }

   }

   public static void getStateTabComplete(List<String> list, String arg, ModelBlueprint blueprint) {
      Iterator var3 = blueprint.getAnimations().keySet().iterator();

      while(var3.hasNext()) {
         String animation = (String)var3.next();
         if (animation.startsWith(arg)) {
            list.add(animation);
         }
      }

   }

   public static void logSender(CommandSender sender, String msg) {
      logSender(sender, msg, msg);
   }

   public static void logSender(CommandSender sender, String entityMsg, String consoleMsg) {
      if (sender instanceof Entity) {
         sender.sendMessage(entityMsg);
      } else {
         TLogger.log(consoleMsg);
      }

   }

   public boolean onCommand(CommandSender sender, String[] args) {
      return false;
   }

   public List<String> onTabComplete(CommandSender sender, String[] args) {
      return null;
   }

   public String getPermissionNode() {
      return "modelengine.command";
   }

   public boolean isConsoleFriendly() {
      return true;
   }

   public String getName() {
      return null;
   }
}
