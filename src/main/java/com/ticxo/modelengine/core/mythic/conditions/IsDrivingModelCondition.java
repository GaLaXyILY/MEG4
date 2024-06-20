package com.ticxo.modelengine.core.mythic.conditions;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.model.bone.manager.BehaviorManager;
import com.ticxo.modelengine.api.model.bone.manager.MountData;
import com.ticxo.modelengine.api.model.bone.manager.MountManager;
import com.ticxo.modelengine.core.mythic.utils.MythicCondition;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.skills.conditions.IEntityCondition;

@MythicCondition(
   name = "drivingmodel"
)
public class IsDrivingModelCondition implements IEntityCondition {
   public boolean check(AbstractEntity abstractEntity) {
      ActiveModel model = ModelEngineAPI.getMountPairManager().getMountedPair(abstractEntity.getUniqueId());
      if (model == null) {
         return false;
      } else {
         ModeledEntity modeledEntity = model.getModeledEntity();
         if (modeledEntity == null) {
            return false;
         } else {
            BehaviorManager mountManager = ((MountData)modeledEntity.getMountData()).getMainMountManager();
            if (mountManager == null) {
               return false;
            } else {
               return ((MountManager)mountManager).getDriver() == abstractEntity.getBukkitEntity();
            }
         }
      }
   }
}
