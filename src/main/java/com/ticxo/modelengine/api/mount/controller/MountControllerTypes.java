package com.ticxo.modelengine.api.mount.controller;

import com.ticxo.modelengine.api.mount.controller.impl.FlyingMountController;
import com.ticxo.modelengine.api.mount.controller.impl.WalkingMountController;

public class MountControllerTypes {
   public static final MountControllerType WALKING = new MountControllerType((entity, mount) -> {
      return new WalkingMountController(entity, mount, false);
   });
   public static final MountControllerType WALKING_FORCE = new MountControllerType((entity, mount) -> {
      return new WalkingMountController(entity, mount, true);
   });
   public static final MountControllerType FLYING = new MountControllerType((entity, mount) -> {
      return new FlyingMountController(entity, mount, false);
   });
   public static final MountControllerType FLYING_FORCE = new MountControllerType((entity, mount) -> {
      return new FlyingMountController(entity, mount, true);
   });
}
