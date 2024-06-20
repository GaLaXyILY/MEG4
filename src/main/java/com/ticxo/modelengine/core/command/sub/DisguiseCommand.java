package com.ticxo.modelengine.core.command.sub;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.command.AbstractCommand;
import com.ticxo.modelengine.api.entity.data.BukkitEntityData;
import com.ticxo.modelengine.api.entity.data.IEntityData;
import com.ticxo.modelengine.api.generator.blueprint.ModelBlueprint;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.model.bone.BoneBehaviorTypes;
import com.ticxo.modelengine.api.model.bone.type.PlayerLimb;
import com.ticxo.modelengine.core.command.MECommand;
import com.ticxo.modelengine.core.command.ModelOptionParser;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DisguiseCommand extends AbstractCommand {
   public DisguiseCommand(AbstractCommand parent) {
      super(parent);
   }

   public boolean onCommand(CommandSender sender, String[] args) {
      Player player = (Player)sender;
      if (args.length < 1) {
         return false;
      } else {
         ModelBlueprint blueprint = ModelEngineAPI.getBlueprint(args[0]);
         if (blueprint == null) {
            return false;
         } else {
            ModelOptionParser options = ModelOptionParser.parse(1, args);
            ModeledEntity modeledEntity = ModelEngineAPI.getOrCreateModeledEntity(player);
            modeledEntity.getBase().getBodyRotationController().setPlayerMode(true);
            modeledEntity.setBaseEntityVisible(false);
            IEntityData var8 = modeledEntity.getBase().getData();
            if (var8 instanceof BukkitEntityData) {
               BukkitEntityData data = (BukkitEntityData)var8;
               if (args.length < 2 || !options.hideSelfDisguise) {
                  data.getTracked().addForcedPairing(player);
               }
            }

            ModelEngineAPI.getEntityHandler().setForcedInvisible(player, true);
            if (modeledEntity.getModel(args[0]).isEmpty()) {
               ActiveModel activeModel = ModelEngineAPI.createActiveModel(blueprint);
               options.applyDisguiseOptions(activeModel);
               modeledEntity.addModel(activeModel, false).ifPresent(ActiveModel::destroy);
               activeModel.getBones().values().forEach((modelBone) -> {
                  modelBone.getBoneBehavior(BoneBehaviorTypes.PLAYER_LIMB).ifPresent((playerLimb) -> {
                     ((PlayerLimb)playerLimb).setTexture(player);
                  });
               });
            }

            return true;
         }
      }
   }

   public List<String> onTabComplete(CommandSender sender, String[] args) {
      List<String> list = new ArrayList();
      switch(args.length) {
      case 1:
         MECommand.getModelIdTabComplete(list, args[0]);
         break;
      default:
         list.addAll(ModelOptionParser.getTabCompletion(1, args));
      }

      return list;
   }

   public String getPermissionNode() {
      return "modelengine.command.disguise";
   }

   public boolean isConsoleFriendly() {
      return false;
   }

   public String getName() {
      return "disguise";
   }
}
