package com.ticxo.modelengine.core.model.bone.manager;

import com.ticxo.modelengine.api.model.bone.behavior.GlobalBehaviorData;
import com.ticxo.modelengine.api.model.bone.manager.BehaviorManager;
import com.ticxo.modelengine.api.model.bone.manager.MountData;
import com.ticxo.modelengine.api.model.bone.manager.MountManager;
import com.ticxo.modelengine.api.model.bone.type.Mount;

public class MountDataImpl implements GlobalBehaviorData, MountData {
   private MountManager mainMountManager;

   public <T extends BehaviorManager<? extends Mount> & MountManager> T getMainMountManager() {
      return (BehaviorManager)this.mainMountManager;
   }

   public <T extends BehaviorManager<? extends Mount> & MountManager> void setMainMountManager(T manager) {
      this.mainMountManager = (MountManager)manager;
   }
}
