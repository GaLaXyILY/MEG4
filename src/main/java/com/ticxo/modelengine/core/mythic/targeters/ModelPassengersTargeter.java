package com.ticxo.modelengine.core.mythic.targeters;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.model.bone.manager.MountManager;
import com.ticxo.modelengine.api.model.bone.type.Mount;
import com.ticxo.modelengine.core.mythic.MythicUtils;
import com.ticxo.modelengine.core.mythic.utils.MythicTargeter;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.SkillCaster;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import io.lumine.mythic.api.skills.targeters.IEntityTargeter;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.core.skills.placeholders.PlaceholderMeta;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import org.bukkit.entity.Entity;

@MythicTargeter(
   name = "modelpassengers",
   aliases = {}
)
public class ModelPassengersTargeter implements IEntityTargeter {
   private final PlaceholderString modelId;
   private final PlaceholderString pbone;

   public ModelPassengersTargeter(MythicLineConfig mlc) {
      this.modelId = mlc.getPlaceholderString(new String[]{"m", "mid", "model", "modelid"}, (String)null, new String[0]);
      this.pbone = mlc.getPlaceholderString(new String[]{"p", "pbone", "seat"}, (String)null, new String[0]);
   }

   public Collection<AbstractEntity> getEntities(SkillMetadata skillMetadata) {
      HashSet<AbstractEntity> targets = new HashSet();
      SkillCaster caster = skillMetadata.getCaster();
      ModeledEntity model = ModelEngineAPI.getModeledEntity(caster.getEntity().getUniqueId());
      if (model == null) {
         return targets;
      } else {
         String parts = MythicUtils.getOrNullLowercase(this.pbone, (PlaceholderMeta)skillMetadata);
         if (parts == null) {
            return targets;
         } else {
            String[] seats = parts.split(",");
            String modelId = MythicUtils.getOrNullLowercase(this.modelId, (PlaceholderMeta)skillMetadata);
            model.getModel(modelId).ifPresentOrElse((activeModel) -> {
               activeModel.getMountManager().ifPresent((mountManager) -> {
                  String[] var3 = seats;
                  int var4 = seats.length;

                  for(int var5 = 0; var5 < var4; ++var5) {
                     String seat = var3[var5];
                     ((MountManager)mountManager).getSeat(seat).ifPresent((mount) -> {
                        Iterator var2 = ((Mount)mount).getPassengers().iterator();

                        while(var2.hasNext()) {
                           Entity passenger = (Entity)var2.next();
                           targets.add(BukkitAdapter.adapt(passenger));
                        }

                     });
                  }

               });
            }, () -> {
               Iterator var3 = model.getModels().values().iterator();

               while(var3.hasNext()) {
                  ActiveModel activeModel = (ActiveModel)var3.next();
                  activeModel.getMountManager().ifPresent((mountManager) -> {
                     String[] var3 = seats;
                     int var4 = seats.length;

                     for(int var5 = 0; var5 < var4; ++var5) {
                        String seat = var3[var5];
                        ((MountManager)mountManager).getSeat(seat).ifPresent((mount) -> {
                           Iterator var2 = ((Mount)mount).getPassengers().iterator();

                           while(var2.hasNext()) {
                              Entity passenger = (Entity)var2.next();
                              targets.add(BukkitAdapter.adapt(passenger));
                           }

                        });
                     }

                  });
               }

            });
            return targets;
         }
      }
   }
}
