package com.ticxo.modelengine.v1_20_R3.entity.controller;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehavior;
import com.ticxo.modelengine.api.model.bone.behavior.GlobalBehaviorData;
import com.ticxo.modelengine.api.model.bone.manager.BehaviorManager;
import com.ticxo.modelengine.api.model.bone.manager.MountData;
import com.ticxo.modelengine.api.model.bone.manager.MountManager;
import com.ticxo.modelengine.api.model.bone.type.Mount;
import com.ticxo.modelengine.api.mount.controller.MountController;
import com.ticxo.modelengine.api.nms.entity.wrapper.MoveController;
import com.ticxo.modelengine.api.utils.ReflectionUtils;
import com.ticxo.modelengine.v1_20_R3.NMSFields;
import java.util.Iterator;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.entity.ai.control.ControllerMove;
import net.minecraft.world.phys.Vec3D;
import org.apache.logging.log4j.util.TriConsumer;
import org.bukkit.craftbukkit.v1_20_R3.util.CraftVector;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class MoveControlWrapper extends ControllerMove implements MoveController {
   protected final ControllerMove original;
   protected final Queue<Runnable> runnables = new ConcurrentLinkedQueue();
   protected boolean isOnGround;

   public MoveControlWrapper(EntityInsentient mob, ControllerMove control) {
      super(mob);
      this.original = control;
   }

   public boolean b() {
      return this.original.b();
   }

   public double c() {
      return this.original.c();
   }

   public void a(double x, double y, double z, double speed) {
      this.original.a(x, y, z, speed);
   }

   public void a(float forward, float sideways) {
      this.original.a(forward, sideways);
   }

   public void a() {
      this.isOnGround = this.d.aC();
      ModeledEntity modeledEntity = ModelEngineAPI.getModeledEntity(this.d.cw());
      if (modeledEntity == null) {
         this.defaultTick();
      } else {
         GlobalBehaviorData mountData = modeledEntity.getMountData();
         BehaviorManager mainMountManager = mountData == null ? null : ((MountData)mountData).getMainMountManager();
         if (mainMountManager != null && ((MountManager)mainMountManager).isControlled()) {
            this.d.c(true);
            this.disableWaterJumping();
            this.driverTick(mainMountManager);
         } else {
            this.defaultTick();
         }

         this.passengerTick(modeledEntity, mainMountManager);

         while(!this.runnables.isEmpty()) {
            ((Runnable)this.runnables.poll()).run();
         }

      }
   }

   public double d() {
      return this.original.d();
   }

   public double e() {
      return this.original.e();
   }

   public double f() {
      return this.original.f();
   }

   protected <T extends BehaviorManager<? extends Mount> & MountManager> void driverTick(T manager) {
      this.d.A(0.0F);
      this.d.C(0.0F);
      this.updateRider(((MountManager)manager).getDriver(), manager.getActiveModel(), ((MountManager)manager).getDriverBone(), MountController::updateDriverMovement);
   }

   protected <T extends BehaviorManager<? extends Mount> & MountManager> void passengerTick(ModeledEntity modeledEntity, T manager) {
      Iterator var3 = modeledEntity.getModels().values().iterator();

      while(var3.hasNext()) {
         ActiveModel activeModel = (ActiveModel)var3.next();
         activeModel.getMountManager().ifPresent((mountManager) -> {
            Iterator var4 = ((MountManager)mountManager).getSeats().values().iterator();

            while(var4.hasNext()) {
               BoneBehavior seat = (BoneBehavior)var4.next();
               Iterator var6 = ((Mount)seat).getPassengers().iterator();

               while(var6.hasNext()) {
                  Entity passenger = (Entity)var6.next();
                  this.updateRider(passenger, activeModel, (Mount)seat, MountController::updatePassengerMovement);
               }
            }

            if (mountManager != manager && ((MountManager)mountManager).isControlled()) {
               this.updateRider(((MountManager)mountManager).getDriver(), activeModel, ((MountManager)mountManager).getDriverBone(), MountController::updatePassengerMovement);
            }

         });
      }

   }

   private void updateRider(Entity entity, ActiveModel activeModel, Mount mountBone, TriConsumer<MountController, MoveController, ActiveModel> updateMethod) {
      MountController controller = this.getController(entity.getUniqueId());
      if (controller != null) {
         if (controller.getInput() == null) {
            controller.setInput(new MountController.MountInput());
         }

         updateMethod.accept(controller, this, activeModel);
      }
   }

   protected void defaultTick() {
      this.original.a();
   }

   private void disableWaterJumping() {
      if (this.d.aZ()) {
         ReflectionUtils.set(this.d, NMSFields.LIVING_ENTITY_noJumpDelay, 1);
      }

   }

   public void move(float side, float up, float front, float speedModifier) {
      float speed = this.getSpeed();
      this.d.w(speed * speedModifier);
      this.d.A(front);
      this.d.B(up);
      this.d.C(side);
   }

   public void globalMove(float x, float y, float z, float speedModifier) {
      float speed = this.getSpeed();
      this.d.w(speed * speedModifier);
      Vec3D vec = (new Vec3D((double)x, (double)y, (double)z)).b(-this.d.dC() * 0.017453292F);
      this.d.C((float)vec.c);
      this.d.B((float)vec.d);
      this.d.A((float)vec.e);
   }

   public void jump() {
      this.d.M().a();
   }

   public void setVelocity(double x, double y, double z) {
      this.d.o(x, y, z);
   }

   public void addVelocity(double x, double y, double z) {
      this.d.g(this.d.dp().b(x, y, z));
   }

   public void nullifyFallDistance() {
      this.d.n();
   }

   public boolean isOnGround() {
      return this.isOnGround;
   }

   public boolean isInWater() {
      return this.d.aZ();
   }

   public float getSpeed() {
      return (float)this.d.b(GenericAttributes.m);
   }

   public Vector getVelocity() {
      return CraftVector.toBukkit(this.d.dp());
   }

   public void queuePostTick(Runnable runnable) {
      this.runnables.add(runnable);
   }

   private MountController getController(UUID uuid) {
      return ModelEngineAPI.getMountPairManager().getController(uuid);
   }
}
