package com.ticxo.modelengine.core.mythic.conditions;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.model.bone.manager.BehaviorManager;
import com.ticxo.modelengine.api.model.bone.manager.MountData;
import com.ticxo.modelengine.api.model.bone.manager.MountManager;
import com.ticxo.modelengine.core.mythic.utils.MythicCondition;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.skills.conditions.IEntityCondition;

@MythicCondition(
   name = "modelhasdriver",
   aliases = {"modeldriver"}
)
public class ModelHasDriverCondition implements IEntityCondition {
   public boolean check(AbstractEntity abstractEntity) {
      ModeledEntity model = ModelEngineAPI.getModeledEntity(abstractEntity.getUniqueId());
      if (model == null) {
         return false;
      } else {
         BehaviorManager mountManager = ((MountData)model.getMountData()).getMainMountManager();
         return mountManager == null ? false : ((MountManager)mountManager).isControlled();
      }
   }
}
