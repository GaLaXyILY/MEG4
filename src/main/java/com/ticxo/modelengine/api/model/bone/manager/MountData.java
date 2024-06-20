package com.ticxo.modelengine.api.model.bone.manager;

import com.ticxo.modelengine.api.model.bone.type.Mount;

public interface MountData {
   <T extends BehaviorManager<? extends Mount> & MountManager> T getMainMountManager();

   <T extends BehaviorManager<? extends Mount> & MountManager> void setMainMountManager(T var1);
}
