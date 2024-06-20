package com.ticxo.modelengine.core.mythic.conditions;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.bone.manager.BehaviorManager;
import com.ticxo.modelengine.api.model.bone.manager.MountManager;
import com.ticxo.modelengine.core.mythic.utils.MythicCondition;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.skills.conditions.IEntityCondition;
import java.util.Optional;

@MythicCondition(
   name = "ridingmodel"
)
public class IsRidingModelCondition implements IEntityCondition {
   public boolean check(AbstractEntity abstractEntity) {
      ActiveModel model = ModelEngineAPI.getMountPairManager().getMountedPair(abstractEntity.getUniqueId());
      if (model == null) {
         return false;
      } else {
         Optional maybeManager = model.getMountManager();
         if (maybeManager.isEmpty()) {
            return false;
         } else {
            BehaviorManager mountManager = (BehaviorManager)maybeManager.get();
            Optional mount = ((MountManager)mountManager).getMount(abstractEntity.getBukkitEntity());
            return mount.isPresent() && mount.get() != ((MountManager)mountManager).getDriverBone();
         }
      }
   }
}
