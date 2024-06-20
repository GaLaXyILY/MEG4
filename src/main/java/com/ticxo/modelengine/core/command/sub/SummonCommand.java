package com.ticxo.modelengine.core.command.sub;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.command.AbstractCommand;
import com.ticxo.modelengine.api.entity.BaseEntity;
import com.ticxo.modelengine.api.entity.BukkitEntity;
import com.ticxo.modelengine.api.generator.blueprint.ModelBlueprint;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.model.bone.BoneBehaviorTypes;
import com.ticxo.modelengine.api.model.bone.type.PlayerLimb;
import com.ticxo.modelengine.core.command.MECommand;
import com.ticxo.modelengine.core.command.ModelOptionParser;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class SummonCommand extends AbstractCommand {
   public SummonCommand(AbstractCommand parent) {
      super(parent);
   }

   public boolean onCommand(CommandSender sender, String[] args) {
      if (args.length < 1) {
         return false;
      } else {
         EntityType type = EntityType.PIG;
         if (args.length >= 2) {
            try {
               type = EntityType.valueOf(args[1].toUpperCase(Locale.ENGLISH));
            } catch (IllegalArgumentException var9) {
            }
         }

         Class<? extends Entity> clazz = type.getEntityClass();
         if (clazz == null) {
            return false;
         } else {
            ModelBlueprint blueprint = ModelEngineAPI.getBlueprint(args[0]);
            if (blueprint == null) {
               return false;
            } else {
               ModelOptionParser options = ModelOptionParser.parse(2, args);
               Player player = (Player)sender;
               Location location = player.getLocation();
               player.getWorld().spawn(location, clazz, (entity) -> {
                  BukkitEntity base = new BukkitEntity(entity);
                  base.getBodyRotationController().setYBodyRot(location.getYaw());
                  ModeledEntity modeledEntity = ModelEngineAPI.createModeledEntity((BaseEntity)base);
                  modeledEntity.setBaseEntityVisible(false);
                  ActiveModel activeModel = ModelEngineAPI.createActiveModel(blueprint);
                  activeModel.setAutoRendererInitialization(false);
                  options.applyDisguiseOptions(activeModel);
                  modeledEntity.addModel(activeModel, true).ifPresent(ActiveModel::destroy);
                  activeModel.getBones().values().forEach((modelBone) -> {
                     modelBone.getBoneBehavior(BoneBehaviorTypes.PLAYER_LIMB).ifPresent((playerLimb) -> {
                        ((PlayerLimb)playerLimb).setTexture(player);
                     });
                  });
                  activeModel.initializeRenderer();
               });
               return true;
            }
         }
      }
   }

   public List<String> onTabComplete(CommandSender sender, String[] args) {
      List<String> list = new ArrayList();
      switch(args.length) {
      case 1:
         MECommand.getModelIdTabComplete(list, args[0]);
         break;
      case 2:
         String arg = args[1];
         EntityType[] var5 = EntityType.values();
         int var6 = var5.length;

         for(int var7 = 0; var7 < var6; ++var7) {
            EntityType type = var5[var7];
            String name = type.name();
            if (name.startsWith(arg.toUpperCase(Locale.ENGLISH))) {
               list.add(name);
            }
         }

         return list;
      default:
         list.addAll(ModelOptionParser.getTabCompletion(args.length > 1 ? 2 : 1, args));
      }

      return list;
   }

   public String getPermissionNode() {
      return "modelengine.command.summon";
   }

   public boolean isConsoleFriendly() {
      return false;
   }

   public String getName() {
      return "summon";
   }
}
