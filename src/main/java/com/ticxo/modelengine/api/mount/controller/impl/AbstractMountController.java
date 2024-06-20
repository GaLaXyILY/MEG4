package com.ticxo.modelengine.api.mount.controller.impl;

import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.bone.type.Mount;
import com.ticxo.modelengine.api.mount.controller.MountController;
import com.ticxo.modelengine.api.nms.entity.wrapper.LookController;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public abstract class AbstractMountController implements MountController {
   protected final Entity entity;
   protected final Mount mount;
   protected MountController.MountInput input;
   protected boolean canDamageMount;
   protected boolean canInteractMount;

   public boolean canDamageMount() {
      return this.canDamageMount;
   }

   public boolean canInteractMount() {
      return this.canInteractMount;
   }

   public void updateDirection(LookController controller, ActiveModel model) {
      Location location = this.getEntity().getLocation();
      controller.setHeadYaw(location.getYaw());
      controller.setPitch(location.getPitch() * 0.5F);
   }

   public AbstractMountController(Entity entity, Mount mount) {
      this.entity = entity;
      this.mount = mount;
   }

   public Entity getEntity() {
      return this.entity;
   }

   public Mount getMount() {
      return this.mount;
   }

   public MountController.MountInput getInput() {
      return this.input;
   }

   public boolean isCanDamageMount() {
      return this.canDamageMount;
   }

   public boolean isCanInteractMount() {
      return this.canInteractMount;
   }

   public void setInput(MountController.MountInput input) {
      this.input = input;
   }

   public void setCanDamageMount(boolean canDamageMount) {
      this.canDamageMount = canDamageMount;
   }

   public void setCanInteractMount(boolean canInteractMount) {
      this.canInteractMount = canInteractMount;
   }
}
