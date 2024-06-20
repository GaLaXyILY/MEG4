package com.ticxo.modelengine.v1_20_R2.entity.controller;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.model.bone.BoneBehaviorTypes;
import com.ticxo.modelengine.api.model.bone.behavior.GlobalBehaviorData;
import com.ticxo.modelengine.api.model.bone.manager.BehaviorManager;
import com.ticxo.modelengine.api.model.bone.manager.MountData;
import com.ticxo.modelengine.api.model.bone.manager.MountManager;
import com.ticxo.modelengine.api.model.bone.type.Mount;
import com.ticxo.modelengine.api.mount.controller.MountController;
import com.ticxo.modelengine.api.nms.entity.wrapper.LookController;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.ai.control.ControllerLook;
import net.minecraft.world.phys.Vec3D;
import org.spigotmc.ActivationRange;

public class LookControlWrapper extends ControllerLook implements LookController {
   private final ControllerLook original;
   private ModeledEntity modeledEntity;

   public LookControlWrapper(EntityInsentient mob, ControllerLook control) {
      super(mob);
      this.original = control;
   }

   public void a(Vec3D var0) {
      this.original.a(var0);
   }

   public void a(Entity var0) {
      this.original.a(var0);
   }

   public void a(Entity var0, float var1, float var2) {
      this.original.a(var0, var1, var2);
   }

   public void a(double var0, double var2, double var4) {
      this.original.a(var0, var2, var4);
   }

   public void a(double var0, double var2, double var4, float var6, float var7) {
      this.original.a(var0, var2, var4, var6, var7);
   }

   public void a() {
      if (this.modeledEntity == null) {
         this.modeledEntity = ModelEngineAPI.getModeledEntity(this.a.cv());
      }

      BehaviorManager mountManager = this.getMainManager();
      if (mountManager != null && ((MountManager)mountManager).isControlled()) {
         this.controlledTick(mountManager);
      } else {
         this.defaultTick();
      }

   }

   protected <T extends BehaviorManager<? extends Mount> & MountManager> void controlledTick(T manager) {
      org.bukkit.entity.Entity driver = ((MountManager)manager).getDriver();
      if (driver != null) {
         MountController controller = ModelEngineAPI.getMountPairManager().getController(driver.getUniqueId());
         if (controller != null) {
            controller.updateDirection(this, manager.getActiveModel());
            return;
         }
      }

      this.defaultTick();
   }

   protected void defaultTick() {
      if (this.isActive()) {
         this.original.a();
      }

   }

   public boolean d() {
      return this.original.d();
   }

   public double e() {
      return this.original.e();
   }

   public double f() {
      return this.original.f();
   }

   public double g() {
      return this.original.g();
   }

   public void lookAt(double x, double y, double z) {
      double dX = x - this.a.dq();
      double dY = y - this.a.du();
      double dZ = z - this.a.dw();
      double hMagnitude = Math.sqrt(dX * dX + dZ * dZ);
      float pitch = (float)Math.toDegrees(Math.atan2(-dY, hMagnitude));
      float yaw = (float)Math.toDegrees(Math.atan2(-dX, dZ));
      this.setPitch(pitch);
      this.setHeadYaw(yaw);
   }

   public void setPitch(float pitch) {
      this.a.s(pitch);
   }

   public void setHeadYaw(float yaw) {
      this.a.r(yaw);
      this.a.aW = yaw;
   }

   public void setBodyYaw(float yaw) {
      this.a.aU = yaw;
   }

   private <T extends BehaviorManager<? extends Mount> & MountManager> T getMainManager() {
      if (this.modeledEntity == null) {
         return null;
      } else {
         GlobalBehaviorData data = this.modeledEntity.getGlobalBehaviorData(BoneBehaviorTypes.MOUNT);
         if (data instanceof MountData) {
            MountData mountData = (MountData)data;
            return mountData.getMainMountManager();
         } else {
            return null;
         }
      }
   }

   private boolean isActive() {
      return ActivationRange.checkIfActive(this.a);
   }
}
