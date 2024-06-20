package com.ticxo.modelengine.v1_20_R4.entity.hitbox;

import com.ticxo.modelengine.api.utils.math.OrientedBoundingBox;
import java.util.Optional;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.Vec3D;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class OBB extends AxisAlignedBB {
   private final Quaternionf rotation;
   private final float yaw;
   private final OrientedBoundingBox bukkitOBB;

   public OBB(Vec3D cornerA, Vec3D cornerB, Quaternionf rotation, float yaw) {
      this(cornerA.c, cornerA.d, cornerA.e, cornerB.c, cornerB.d, cornerB.e, rotation, yaw);
   }

   public OBB(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, Quaternionf rotation, float yaw) {
      super(minX, minY, minZ, maxX, maxY, maxZ);
      this.rotation = rotation;
      this.yaw = yaw;
      this.bukkitOBB = new OrientedBoundingBox(this.f().j(), new Vector3f((float)this.b(), (float)this.c(), (float)this.d()), rotation, yaw);
   }

   public OBB makeOBBInstance(Vec3D position, Quaternionf rotation, float yaw) {
      return new OBB(position.b(this.a, this.b, this.c), position.b(this.d, this.e, this.f), rotation, yaw);
   }

   @NotNull
   public AxisAlignedBB c(double xInflate, double yInflate, double zInflate) {
      double minX = this.a - xInflate;
      double minY = this.b - yInflate;
      double minZ = this.c - zInflate;
      double maxX = this.d + xInflate;
      double maxY = this.e + yInflate;
      double maxZ = this.f + zInflate;
      return new OBB(minX, minY, minZ, maxX, maxY, maxZ, this.rotation, this.yaw);
   }

   public boolean c(@NotNull AxisAlignedBB aabb) {
      boolean var10000;
      if (aabb instanceof OBB) {
         OBB obb = (OBB)aabb;
         var10000 = this.intersects(obb);
      } else {
         var10000 = super.c(aabb);
      }

      return var10000;
   }

   public boolean intersects(OBB obb) {
      return this.bukkitOBB.intersects(obb.bukkitOBB);
   }

   public boolean a(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
      OrientedBoundingBox obbB = new OrientedBoundingBox((float)minX, (float)minY, (float)minZ, (float)maxX, (float)maxY, (float)maxZ);
      return this.bukkitOBB.intersects(obbB);
   }

   public Optional<Vec3D> b(Vec3D from, Vec3D to) {
      Quaternionf inverse = this.rotation.conjugate(new Quaternionf());
      Vec3D center = this.f();
      float yaw = this.yaw * 0.017453292F;
      Vec3D rFrom = (new Vec3D(from.d(center).j().rotateY(-yaw).rotate(inverse))).e(center);
      Vec3D rTo = (new Vec3D(to.d(center).j().rotateY(-yaw).rotate(inverse))).e(center);
      Optional<Vec3D> result = super.b(rFrom, rTo);
      if (result.isEmpty()) {
         return result;
      } else {
         Vec3D clip = (new Vec3D(((Vec3D)result.get()).d(center).j().rotate(this.rotation).rotateY(yaw))).e(center);
         return Optional.of(clip);
      }
   }

   public String toString() {
      Quaternionf var10000 = this.getRotation();
      return "OBB(rotation=" + var10000 + ", yaw=" + this.getYaw() + ", bukkitOBB=" + this.getBukkitOBB() + ")";
   }

   public Quaternionf getRotation() {
      return this.rotation;
   }

   public float getYaw() {
      return this.yaw;
   }

   public OrientedBoundingBox getBukkitOBB() {
      return this.bukkitOBB;
   }
}
