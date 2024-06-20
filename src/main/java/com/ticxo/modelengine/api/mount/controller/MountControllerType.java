package com.ticxo.modelengine.api.mount.controller;

import com.ticxo.modelengine.api.model.bone.type.Mount;
import java.util.function.BiFunction;
import org.bukkit.entity.Entity;

public class MountControllerType implements MountControllerSupplier {
   private final BiFunction<Entity, Mount, MountController> controllerConstructor;

   public MountController createController(Entity entity, Mount mount) {
      return (MountController)this.controllerConstructor.apply(entity, mount);
   }

   public MountControllerType(BiFunction<Entity, Mount, MountController> controllerConstructor) {
      this.controllerConstructor = controllerConstructor;
   }
}
