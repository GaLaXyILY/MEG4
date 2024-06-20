package com.ticxo.modelengine.core.citizens;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.animation.property.IAnimationProperty;
import com.ticxo.modelengine.api.command.AbstractCommand;
import com.ticxo.modelengine.api.generator.blueprint.ModelBlueprint;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.core.command.MECommand;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class CitizensCommand extends AbstractCommand {
   public CitizensCommand(AbstractCommand parent) {
      super(parent);
      this.addSubCommands(new AbstractCommand[]{new CitizensCommand.ModelCommand(this)});
      this.addSubCommands(new AbstractCommand[]{new CitizensCommand.StateCommand(this)});
   }

   public boolean onCommand(CommandSender sender, String[] args) {
      return false;
   }

   public List<String> onTabComplete(CommandSender sender, String[] args) {
      return null;
   }

   public String getPermissionNode() {
      return "modelengine.command.npc";
   }

   public boolean isConsoleFriendly() {
      return true;
   }

   public String getName() {
      return "npc";
   }

   private static void getNPCIdTabComplete(List<String> list) {
      CitizensAPI.getNPCRegistries().forEach((npcs) -> {
         npcs.sorted().forEach((npc) -> {
            if (npc.isSpawned() && npc.hasTrait(ModelTrait.class)) {
               String var10001 = npcs.getName();
               list.add(var10001 + ":" + npc.getId() + ":[" + npc.getName().replace(" ", "-") + "]");
            }

         });
      });
   }

   private static String tryGetOrDefault(String[] args, int index, String def) {
      return args.length <= index ? def : args[index];
   }

   private static NPC getNPC(String id) {
      String[] val = id.split(":");
      if (val.length < 2) {
         throw new IllegalArgumentException("NPC ID must be formatted as <registry>:<NPC ID>");
      } else {
         NPCRegistry reg = CitizensAPI.getNamedNPCRegistry(val[0]);
         if (reg == null) {
            throw new IllegalArgumentException("Unknown NPC registry: " + val[0]);
         } else {
            NPC npc = reg.getById(Integer.parseInt(val[1]));
            if (npc == null) {
               throw new IllegalArgumentException("Unknown NPC ID: " + val[1]);
            } else {
               return npc;
            }
         }
      }
   }

   private static class ModelCommand extends AbstractCommand {
      public ModelCommand(AbstractCommand parent) {
         super(parent);
      }

      public boolean onCommand(CommandSender sender, String[] args) {
         if (args.length < 1) {
            return false;
         } else {
            NPC npc = CitizensCommand.getNPC(args[0]);
            ModelTrait trait = (ModelTrait)npc.getTraitNullable(ModelTrait.class);
            if (trait == null) {
               return false;
            } else if (!npc.isSpawned()) {
               sender.sendMessage(ChatColor.RED + "Please spawn the NPC before editing.");
               return true;
            } else {
               ModeledEntity modeledEntity = trait.getOrCreateModeledEntity();
               if (modeledEntity == null) {
                  sender.sendMessage(ChatColor.RED + "An error occurred while retrieving the model of this NPC.");
                  return true;
               } else if (args.length >= 2) {
                  if (args.length >= 3) {
                     String modelId = args[2];
                     String var13 = args[1];
                     byte var14 = -1;
                     switch(var13.hashCode()) {
                     case -934610812:
                        if (var13.equals("remove")) {
                           var14 = 1;
                        }
                        break;
                     case 96417:
                        if (var13.equals("add")) {
                           var14 = 0;
                        }
                     }

                     switch(var14) {
                     case 0:
                        boolean showBase = Boolean.parseBoolean(CitizensCommand.tryGetOrDefault(args, 3, "false"));
                        boolean overrideHitbox = Boolean.parseBoolean(CitizensCommand.tryGetOrDefault(args, 4, "true"));
                        ActiveModel model = ModelEngineAPI.createActiveModel(modelId);
                        if (model == null) {
                           return false;
                        }

                        modeledEntity.setBaseEntityVisible(showBase);
                        modeledEntity.addModel(model, overrideHitbox).ifPresent(ActiveModel::destroy);
                        sender.sendMessage("Added model " + modelId + " to " + npc.getName());
                        break;
                     case 1:
                        modeledEntity.removeModel(modelId).ifPresent(ActiveModel::destroy);
                        if (modeledEntity.getModels().isEmpty()) {
                           modeledEntity.setBaseEntityVisible(true);
                        }

                        sender.sendMessage("Removed model " + modelId + " from " + npc.getName());
                     }
                  }

                  return true;
               } else if (modeledEntity.getModels().isEmpty()) {
                  sender.sendMessage("This NPC has no models.");
                  return true;
               } else {
                  StringBuilder builder = new StringBuilder("Models: ");
                  Iterator var7 = modeledEntity.getModels().keySet().iterator();

                  while(var7.hasNext()) {
                     String active = (String)var7.next();
                     builder.append(active).append(", ");
                  }

                  builder.delete(builder.length() - 2, builder.length());
                  sender.sendMessage(builder.toString());
                  return true;
               }
            }
         }
      }

      public List<String> onTabComplete(CommandSender sender, String[] args) {
         List<String> list = new ArrayList();
         String arg = args[args.length - 1];
         switch(args.length) {
         case 1:
            CitizensCommand.getNPCIdTabComplete(list);
            break;
         case 2:
            if ("add".startsWith(arg)) {
               list.add("add");
            }

            if ("remove".startsWith(arg)) {
               list.add("remove");
            }
            break;
         case 3:
            String var5 = args[1];
            byte var6 = -1;
            switch(var5.hashCode()) {
            case -934610812:
               if (var5.equals("remove")) {
                  var6 = 1;
               }
               break;
            case 96417:
               if (var5.equals("add")) {
                  var6 = 0;
               }
            }

            switch(var6) {
            case 0:
               MECommand.getModelIdTabComplete(list, arg);
               return list;
            case 1:
               ModelTrait trait = (ModelTrait)CitizensCommand.getNPC(args[0]).getTraitNullable(ModelTrait.class);
               if (trait != null) {
                  MECommand.getModelIdTabComplete(list, arg, trait.getModeledEntity());
               }

               return list;
            default:
               return list;
            }
         case 4:
            if ("add".equals(args[1])) {
               list.add("[showBaseEntity]");
               if ("true".startsWith(arg)) {
                  list.add("true");
               }

               if ("false".startsWith(arg)) {
                  list.add("false");
               }
            }
            break;
         case 5:
            if ("add".equals(args[1])) {
               list.add("[overrideHitbox]");
               if ("true".startsWith(arg)) {
                  list.add("true");
               }

               if ("false".startsWith(arg)) {
                  list.add("false");
               }
            }
         }

         return list;
      }

      public String getPermissionNode() {
         return "modelengine.command.npc.model";
      }

      public boolean isConsoleFriendly() {
         return true;
      }

      public String getName() {
         return "model";
      }
   }

   private static class StateCommand extends AbstractCommand {
      public StateCommand(AbstractCommand parent) {
         super(parent);
      }

      public boolean onCommand(CommandSender sender, String[] args) {
         if (args.length < 1) {
            return false;
         } else {
            NPC npc = CitizensCommand.getNPC(args[0]);
            ModelTrait trait = (ModelTrait)npc.getTraitNullable(ModelTrait.class);
            if (trait == null) {
               return false;
            } else if (!npc.isSpawned()) {
               sender.sendMessage(ChatColor.RED + "Please spawn the NPC before editing.");
               return true;
            } else {
               ModeledEntity modeledEntity = trait.getOrCreateModeledEntity();
               if (modeledEntity == null) {
                  sender.sendMessage(ChatColor.RED + "An error occurred while retrieving the model of this NPC.");
                  return true;
               } else {
                  ActiveModel activeModel = (ActiveModel)modeledEntity.getModel(args[1]).orElse((Object)null);
                  if (activeModel == null) {
                     return false;
                  } else if (args.length >= 3) {
                     if (args.length < 4) {
                        return false;
                     } else {
                        String stateId = args[3];
                        String var15 = args[2];
                        byte var16 = -1;
                        switch(var15.hashCode()) {
                        case -934610812:
                           if (var15.equals("remove")) {
                              var16 = 1;
                           }
                           break;
                        case 96417:
                           if (var15.equals("add")) {
                              var16 = 0;
                           }
                        }

                        switch(var16) {
                        case 0:
                           if (!activeModel.getBlueprint().getAnimations().containsKey(stateId)) {
                              return false;
                           }

                           int lerpIn = Integer.parseInt(CitizensCommand.tryGetOrDefault(args, 4, "0"));
                           int lerpOut = Integer.parseInt(CitizensCommand.tryGetOrDefault(args, 5, "0"));
                           double speed = Double.parseDouble(CitizensCommand.tryGetOrDefault(args, 6, "1"));
                           activeModel.getAnimationHandler().playAnimation(stateId, (double)lerpIn / 20.0D, (double)lerpOut / 20.0D, speed, true);
                           sender.sendMessage("Added state " + stateId + " to " + npc.getName());
                           break;
                        case 1:
                           boolean ignoreLerp = Boolean.parseBoolean(CitizensCommand.tryGetOrDefault(args, 4, "false"));
                           if (ignoreLerp) {
                              activeModel.getAnimationHandler().forceStopAnimation(stateId);
                           } else {
                              activeModel.getAnimationHandler().forceStopAnimation(stateId);
                           }

                           sender.sendMessage("Removed state " + stateId + " from " + npc.getName());
                        }

                        return false;
                     }
                  } else {
                     StringBuilder builder = new StringBuilder();
                     builder.append(args[1]).append(": ");
                     Iterator var8 = activeModel.getAnimationHandler().getAnimations().values().iterator();

                     while(var8.hasNext()) {
                        IAnimationProperty animation = (IAnimationProperty)var8.next();
                        builder.append(animation.getName()).append(", ");
                     }

                     builder.delete(builder.length() - 2, builder.length());
                     sender.sendMessage(builder.toString());
                     return true;
                  }
               }
            }
         }
      }

      public List<String> onTabComplete(CommandSender sender, String[] args) {
         List<String> list = new ArrayList();
         String arg = args[args.length - 1];
         ModelTrait trait;
         switch(args.length) {
         case 1:
            CitizensCommand.getNPCIdTabComplete(list);
            break;
         case 2:
            trait = (ModelTrait)CitizensCommand.getNPC(args[0]).getTraitNullable(ModelTrait.class);
            if (trait != null) {
               MECommand.getModelIdTabComplete(list, arg, trait.getModeledEntity());
            }
            break;
         case 3:
            if ("add".startsWith(arg)) {
               list.add("add");
            }

            if ("remove".startsWith(arg)) {
               list.add("remove");
            }
            break;
         case 4:
            trait = (ModelTrait)CitizensCommand.getNPC(args[0]).getTraitNullable(ModelTrait.class);
            if (trait == null) {
               return list;
            }

            ModeledEntity model = trait.getModeledEntity();
            ActiveModel activeModel = (ActiveModel)model.getModel(args[1]).orElse((Object)null);
            if (activeModel == null) {
               return list;
            }

            String var8 = args[2];
            byte var9 = -1;
            switch(var8.hashCode()) {
            case -934610812:
               if (var8.equals("remove")) {
                  var9 = 1;
               }
               break;
            case 96417:
               if (var8.equals("add")) {
                  var9 = 0;
               }
            }

            switch(var9) {
            case 0:
               MECommand.getStateTabComplete(list, arg, (ModelBlueprint)activeModel.getBlueprint());
               return list;
            case 1:
               MECommand.getStateTabComplete(list, arg, (ActiveModel)activeModel);
               return list;
            default:
               return list;
            }
         case 5:
            String var5 = args[2];
            byte var6 = -1;
            switch(var5.hashCode()) {
            case -934610812:
               if (var5.equals("remove")) {
                  var6 = 1;
               }
               break;
            case 96417:
               if (var5.equals("add")) {
                  var6 = 0;
               }
            }

            switch(var6) {
            case 0:
               list.add("[lerpin]");
               return list;
            case 1:
               list.add("[ignoreLerp]");
               if ("true".startsWith(arg)) {
                  list.add("true");
               }

               if ("false".startsWith(arg)) {
                  list.add("false");
               }

               return list;
            default:
               return list;
            }
         case 6:
            if ("add".equals(args[2])) {
               list.add("[lerpout]");
            }
            break;
         case 7:
            if ("add".equals(args[2])) {
               list.add("[speed]");
            }
         }

         return list;
      }

      public String getPermissionNode() {
         return "modelengine.command.npc.state";
      }

      public boolean isConsoleFriendly() {
         return true;
      }

      public String getName() {
         return "state";
      }
   }
}
