package com.ticxo.modelengine.core.citizens;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.entity.BaseEntity;
import com.ticxo.modelengine.api.entity.BukkitEntity;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.utils.data.io.SavedData;
import com.ticxo.modelengine.core.data.DataUpdater;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.DataKey;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

@TraitName("meg_model")
public class ModelTrait extends Trait {
   private String modelData;

   public ModelTrait() {
      super("meg_model");
   }

   public void load(DataKey key) {
      this.modelData = key.getString("model_data");
   }

   public void save(DataKey key) {
      ModeledEntity modeledEntity = this.getModeledEntity();
      if (modeledEntity != null) {
         modeledEntity.save().ifPresent((data) -> {
            this.modelData = data.toString();
         });
      } else {
         this.modelData = null;
      }

      key.setString("model_data", this.modelData);
   }

   public void onDespawn() {
      ModeledEntity modeledEntity = this.getModeledEntity();
      if (modeledEntity != null) {
         modeledEntity.save().ifPresent((data) -> {
            this.modelData = data.toString();
         });
      } else {
         this.modelData = null;
      }

   }

   public void onSpawn() {
      if (this.modelData != null) {
         Entity entity = this.getNPC().getEntity();
         Location location = entity.getLocation();
         SavedData data = DataUpdater.convertToSavedData(location, this.modelData);
         if (DataUpdater.tryUpdate(data)) {
            BukkitEntity base = new BukkitEntity(entity);
            base.getBodyRotationController().setYBodyRot(location.getYaw());
            ModeledEntity modeledEntity = ModelEngineAPI.createModeledEntity((BaseEntity)base);
            modeledEntity.setSaved(false);
            modeledEntity.load(data);
         }
      }
   }

   public ModeledEntity getModeledEntity() {
      return this.npc.getEntity() != null ? ModelEngineAPI.getModeledEntity(this.npc.getEntity()) : null;
   }

   public ModeledEntity getOrCreateModeledEntity() {
      return this.npc.getEntity() != null ? ModelEngineAPI.getOrCreateModeledEntity(this.npc.getEntity()) : null;
   }
}
