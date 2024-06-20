package com.ticxo.modelengine.core.listener;

import com.google.gson.JsonSyntaxException;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.ServerInfo;
import com.ticxo.modelengine.api.generator.ModelGenerator;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.utils.data.io.SavedData;
import com.ticxo.modelengine.core.data.DataUpdater;
import java.util.Iterator;
import java.util.List;
import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.event.world.EntitiesUnloadEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class WorldListener implements Listener {
   private final ModelGenerator generator = ModelEngineAPI.getAPI().getModelGenerator();

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   private void onEntityLoad(EntitiesLoadEvent event) {
      if (this.generator.isInitialized()) {
         this.loadEntities(event.getEntities());
      } else {
         this.generator.queueTask(ModelGenerator.Phase.POST_IMPORT, () -> {
            this.loadEntities(event.getEntities());
         });
      }

   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   private void onEntityUnload(EntitiesUnloadEvent event) {
      if (this.generator.isInitialized()) {
         this.unloadEntities(event.getEntities());
      } else {
         this.generator.queueTask(ModelGenerator.Phase.POST_IMPORT, () -> {
            this.unloadEntities(event.getEntities());
         });
      }

   }

   private void loadEntities(List<Entity> entities) {
      Iterator var2 = entities.iterator();

      while(var2.hasNext()) {
         Entity entity = (Entity)var2.next();
         if (!(entity instanceof Player) && ModelEngineAPI.getModeledEntity(entity.getUniqueId()) == null) {
            String jsonData = (String)entity.getPersistentDataContainer().get(SavedData.DATA_KEY, PersistentDataType.STRING);
            if (jsonData != null) {
               try {
                  SavedData data = DataUpdater.convertToSavedData(entity.getLocation(), jsonData);
                  if (DataUpdater.tryUpdate(data)) {
                     ModeledEntity model = ModelEngineAPI.createModeledEntity(entity);
                     model.load(data);
                  }
               } catch (JsonSyntaxException var7) {
                  var7.printStackTrace();
               }
            }
         }
      }

   }

   private void unloadEntities(List<Entity> entities) {
      Iterator var2 = entities.iterator();

      while(true) {
         Entity entity;
         do {
            do {
               if (!var2.hasNext()) {
                  return;
               }

               entity = (Entity)var2.next();
            } while(entity instanceof Player);
         } while(ServerInfo.HAS_CITIZENS && CitizensAPI.getNPCRegistry().isNPC(entity));

         PersistentDataContainer pdc = entity.getPersistentDataContainer();
         ModeledEntity model = ModelEngineAPI.getModeledEntity(entity.getUniqueId());
         if (model == null) {
            pdc.remove(SavedData.DATA_KEY);
         } else {
            if (model.shouldBeSaved()) {
               model.save().ifPresentOrElse((data) -> {
                  pdc.set(SavedData.DATA_KEY, PersistentDataType.STRING, data.toString());
               }, () -> {
                  pdc.remove(SavedData.DATA_KEY);
               });
            } else {
               entity.getPersistentDataContainer().remove(SavedData.DATA_KEY);
            }

            ModelEngineAPI.getAPI().getModelUpdaters().forceRemoveModeledEntity(model);
         }
      }
   }
}
