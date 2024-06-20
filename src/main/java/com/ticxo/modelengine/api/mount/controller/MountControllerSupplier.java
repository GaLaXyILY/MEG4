package com.ticxo.modelengine.api.mount.controller;

import com.ticxo.modelengine.api.model.bone.type.Mount;
import org.bukkit.entity.Entity;

@FunctionalInterface
public interface MountControllerSupplier {
   MountController createController(Entity var1, Mount var2);
}
