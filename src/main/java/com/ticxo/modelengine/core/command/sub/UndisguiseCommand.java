package com.ticxo.modelengine.core.command.sub;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.command.AbstractCommand;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.core.command.MECommand;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UndisguiseCommand extends AbstractCommand {
   public UndisguiseCommand(AbstractCommand parent) {
      super(parent);
   }

   public boolean onCommand(CommandSender sender, String[] args) {
      Player player = (Player)sender;
      ModeledEntity modeledEntity = ModelEngineAPI.getModeledEntity(player.getUniqueId());
      if (modeledEntity == null) {
         return true;
      } else {
         if (args.length == 0) {
            modeledEntity.markRemoved();
            ModelEngineAPI.getEntityHandler().setForcedInvisible(player, false);
            ModelEngineAPI.getEntityHandler().forceSpawn(player);
         } else {
            String[] var5 = args;
            int var6 = args.length;

            for(int var7 = 0; var7 < var6; ++var7) {
               String modelId = var5[var7];
               modeledEntity.removeModel(modelId).ifPresent(ActiveModel::destroy);
            }

            if (modeledEntity.getModels().isEmpty()) {
               modeledEntity.markRemoved();
               ModelEngineAPI.getEntityHandler().setForcedInvisible(player, false);
               ModelEngineAPI.getEntityHandler().forceSpawn(player);
            }
         }

         return true;
      }
   }

   public List<String> onTabComplete(CommandSender sender, String[] args) {
      List<String> list = new ArrayList();
      Player player = (Player)sender;
      ModeledEntity modeledEntity = ModelEngineAPI.getModeledEntity(player.getUniqueId());
      if (modeledEntity == null) {
         return list;
      } else {
         if (args.length > 0) {
            String arg = args[args.length - 1];
            MECommand.getModelIdTabComplete(list, arg, modeledEntity);
         }

         return list;
      }
   }

   public String getPermissionNode() {
      return "modelengine.command.undisguise";
   }

   public boolean isConsoleFriendly() {
      return false;
   }

   public String getName() {
      return "undisguise";
   }
}
