package com.ticxo.modelengine.core.mythic.conditions;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehavior;
import com.ticxo.modelengine.api.model.bone.manager.BehaviorManager;
import com.ticxo.modelengine.api.model.bone.manager.MountManager;
import com.ticxo.modelengine.api.model.bone.type.Mount;
import com.ticxo.modelengine.core.mythic.MythicUtils;
import com.ticxo.modelengine.core.mythic.utils.MythicCondition;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.conditions.IEntityCondition;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import java.util.Iterator;
import java.util.Locale;
import java.util.Optional;

@MythicCondition(
   name = "modelhaspassengers",
   aliases = {"modelhaspassenger", "modelpassengers", "modelpassenger"}
)
public class ModelHasPassengerCondition implements IEntityCondition {
   private final PlaceholderString modelId;
   private final PlaceholderString pbone;
   private final String mode;

   public ModelHasPassengerCondition(MythicLineConfig mlc) {
      this.modelId = mlc.getPlaceholderString(new String[]{"m", "mid", "model", "modelid"}, (String)null, new String[0]);
      this.pbone = mlc.getPlaceholderString(new String[]{"p", "pbone", "seat"}, (String)null, new String[0]);
      this.mode = mlc.getString(new String[]{"mode"}, "AND", new String[0]).toUpperCase(Locale.ENGLISH);
   }

   public boolean check(AbstractEntity abstractEntity) {
      ModeledEntity modeledEntity = ModelEngineAPI.getModeledEntity(abstractEntity.getUniqueId());
      if (modeledEntity == null) {
         return false;
      } else {
         String modelId = MythicUtils.getOrNullLowercase(this.modelId, abstractEntity);
         if (modelId == null) {
            return this.noModelIdCheck(modeledEntity, abstractEntity);
         } else {
            Optional<ActiveModel> maybeModel = modeledEntity.getModel(modelId);
            if (maybeModel.isEmpty()) {
               return false;
            } else {
               ActiveModel activeModel = (ActiveModel)maybeModel.get();
               Optional maybeMountManager = activeModel.getMountManager();
               if (maybeMountManager.isEmpty()) {
                  return false;
               } else {
                  BehaviorManager mountManager = (BehaviorManager)maybeMountManager.get();
                  String pBone = MythicUtils.getOrNullLowercase(this.pbone, abstractEntity);
                  if (pBone != null && !pBone.isBlank()) {
                     String[] seats = pBone.split(",");
                     String var10 = this.mode;
                     byte var11 = -1;
                     switch(var10.hashCode()) {
                     case 2531:
                        if (var10.equals("OR")) {
                           var11 = 0;
                        }
                        break;
                     case 64951:
                        if (var10.equals("AND")) {
                           var11 = 1;
                        }
                     }

                     String[] var12;
                     int var13;
                     int var14;
                     String seatId;
                     Optional maybeSeat;
                     switch(var11) {
                     case 0:
                        var12 = seats;
                        var13 = seats.length;

                        for(var14 = 0; var14 < var13; ++var14) {
                           seatId = var12[var14];
                           maybeSeat = ((MountManager)mountManager).getSeat(seatId);
                           if (!maybeSeat.isEmpty() && ((MountManager)mountManager).getDriverBone() != maybeSeat.get() && !((Mount)((BoneBehavior)maybeSeat.get())).getPassengers().isEmpty()) {
                              return true;
                           }
                        }

                        return false;
                     case 1:
                        var12 = seats;
                        var13 = seats.length;

                        for(var14 = 0; var14 < var13; ++var14) {
                           seatId = var12[var14];
                           maybeSeat = ((MountManager)mountManager).getSeat(seatId);
                           if (maybeSeat.isEmpty()) {
                              return false;
                           }

                           if (((MountManager)mountManager).getDriverBone() != maybeSeat.get() && ((Mount)((BoneBehavior)maybeSeat.get())).getPassengers().isEmpty()) {
                              return false;
                           }
                        }

                        return true;
                     default:
                        return false;
                     }
                  } else {
                     return ((MountManager)mountManager).hasPassengers();
                  }
               }
            }
         }
      }
   }

   private boolean noModelIdCheck(ModeledEntity modeledEntity, AbstractEntity abstractEntity) {
      String pBone = MythicUtils.getOrNullLowercase(this.pbone, abstractEntity);
      if (pBone != null && !pBone.isBlank()) {
         String[] seats = pBone.split(",");
         String var17 = this.mode;
         byte var18 = -1;
         switch(var17.hashCode()) {
         case 2531:
            if (var17.equals("OR")) {
               var18 = 0;
            }
            break;
         case 64951:
            if (var17.equals("AND")) {
               var18 = 1;
            }
         }

         Iterator var7;
         ActiveModel activeModel;
         Optional maybeMountManager;
         BehaviorManager mountManager;
         String[] var11;
         int var12;
         int var13;
         String seatId;
         Optional maybeSeat;
         switch(var18) {
         case 0:
            var7 = modeledEntity.getModels().values().iterator();

            while(true) {
               do {
                  if (!var7.hasNext()) {
                     return false;
                  }

                  activeModel = (ActiveModel)var7.next();
                  maybeMountManager = activeModel.getMountManager();
               } while(maybeMountManager.isEmpty());

               mountManager = (BehaviorManager)maybeMountManager.get();
               var11 = seats;
               var12 = seats.length;

               for(var13 = 0; var13 < var12; ++var13) {
                  seatId = var11[var13];
                  maybeSeat = ((MountManager)mountManager).getSeat(seatId);
                  if (!maybeSeat.isEmpty() && ((MountManager)mountManager).getDriverBone() != maybeSeat.get() && !((Mount)((BoneBehavior)maybeSeat.get())).getPassengers().isEmpty()) {
                     return true;
                  }
               }
            }
         case 1:
            var7 = modeledEntity.getModels().values().iterator();

            while(var7.hasNext()) {
               activeModel = (ActiveModel)var7.next();
               maybeMountManager = activeModel.getMountManager();
               if (maybeMountManager.isEmpty()) {
                  return false;
               }

               mountManager = (BehaviorManager)maybeMountManager.get();
               var11 = seats;
               var12 = seats.length;

               for(var13 = 0; var13 < var12; ++var13) {
                  seatId = var11[var13];
                  maybeSeat = ((MountManager)mountManager).getSeat(seatId);
                  if (maybeSeat.isEmpty()) {
                     return false;
                  }

                  if (((MountManager)mountManager).getDriverBone() != maybeSeat.get() && ((Mount)((BoneBehavior)maybeSeat.get())).getPassengers().isEmpty()) {
                     return false;
                  }
               }
            }

            return true;
         default:
            return false;
         }
      } else {
         Iterator var4 = modeledEntity.getModels().values().iterator();

         Optional maybeMountManager;
         do {
            if (!var4.hasNext()) {
               return false;
            }

            ActiveModel activeModel = (ActiveModel)var4.next();
            maybeMountManager = activeModel.getMountManager();
         } while(maybeMountManager.isEmpty() || !((MountManager)((BehaviorManager)maybeMountManager.get())).hasPassengers());

         return true;
      }
   }
}
