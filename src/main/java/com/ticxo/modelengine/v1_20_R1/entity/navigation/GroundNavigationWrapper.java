package com.ticxo.modelengine.v1_20_R1.entity.navigation;

import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.ai.navigation.Navigation;
import net.minecraft.world.level.pathfinder.PathEntity;
import net.minecraft.world.phys.Vec3D;
import org.jetbrains.annotations.Nullable;

public class GroundNavigationWrapper extends Navigation {
   public GroundNavigationWrapper(EntityInsentient mob, Navigation oldNav) {
      super(mob, mob.dI());
      this.a(oldNav.q());
      this.b(oldNav.f());
      this.c(oldNav.e());
   }

   public boolean a(@Nullable PathEntity newPath, double speed) {
      if (newPath != null && !newPath.a(this.c)) {
         int closestNode = newPath.f();
         double distanceSqr = this.getNodeDistanceSquared(newPath.a(this.a));

         for(int i = closestNode + 1; i < newPath.e(); ++i) {
            double temp = this.getNodeDistanceSquared(newPath.a(this.a, i));
            if (temp < distanceSqr) {
               distanceSqr = temp;
               closestNode = i;
            }
         }

         newPath.c(closestNode);
      }

      return super.a(newPath, speed);
   }

   private double getNodeDistanceSquared(Vec3D pos) {
      double x = pos.c - this.a.dn();
      double y = pos.d - this.a.dp();
      double z = pos.e - this.a.dt();
      return x * x + y * y + z * z;
   }

   protected void k() {
      Vec3D mobPosition = this.b();
      this.l = this.a.dd() > 0.75F ? this.a.dd() / 2.0F : 0.75F - this.a.dd() / 2.0F;
      Vec3D targetNodePosition = this.c.a(this.a);
      double distX = Math.abs(this.a.dn() - targetNodePosition.c);
      double distY = Math.abs(this.a.dp() - targetNodePosition.d);
      double distZ = Math.abs(this.a.dt() - targetNodePosition.e);
      boolean hasArrivedNode = distX < (double)this.l && distZ < (double)this.l && distY < 1.0D;
      boolean canAdvance = hasArrivedNode || this.b(this.c.h().l) && this.shouldTargetNextNodeInDirection(mobPosition);
      if (canAdvance) {
         this.c.a();
      }

      this.b(mobPosition);
   }

   private boolean shouldTargetNextNodeInDirection(Vec3D mobPosition) {
      if (this.c.f() + 1 >= this.c.e()) {
         return false;
      } else {
         Vec3D targetNodePosition = this.c.a(this.a);
         if (!mobPosition.a(targetNodePosition, 2.0D)) {
            return false;
         } else if (this.a(mobPosition, this.c.a(this.a))) {
            return true;
         } else {
            Vec3D nextNodePosition = this.c.a(this.a, this.c.f() + 1);
            Vec3D nextNodeDir = nextNodePosition.d(targetNodePosition);
            Vec3D mobDir = mobPosition.d(targetNodePosition);
            return nextNodeDir.b(mobDir) > 0.0D;
         }
      }
   }
}
