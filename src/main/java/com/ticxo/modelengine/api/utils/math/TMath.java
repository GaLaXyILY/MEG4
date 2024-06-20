package com.ticxo.modelengine.api.utils.math;

import java.util.UUID;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.joml.Math;
import org.joml.Quaterniond;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;

public class TMath {
   public static final float PI = 3.1415927F;
   public static final double TAU = 6.283185307179586D;
   public static final double PI_2 = 1.5707963267948966D;
   public static final double PI_4 = 0.7853981633974483D;
   public static final double EPSILON = 1.0E-5D;
   public static final float DEG2RAD = 0.017453292F;
   public static final float RAD2DEG = 57.29578F;
   protected static TMath.SlerpMode slerpMode;
   protected static double movementResolution;

   public static boolean isSimilar(float a, float b) {
      return (double)Math.abs(b - a) < 1.0E-5D;
   }

   public static double clamp(double value, double min, double max) {
      return Math.min(Math.max(value, min), max);
   }

   public static float clamp(float value, float min, float max) {
      return Math.min(Math.max(value, min), max);
   }

   public static int clamp(int value, int min, int max) {
      return Math.min(Math.max(value, min), max);
   }

   public static int absMax(float a, float b, float c) {
      float absA = Math.abs(a);
      float absB = Math.abs(b);
      float absC = Math.abs(c);
      if (absA > absB) {
         return absA > absC ? 0 : 2;
      } else {
         return absB > absC ? 1 : 2;
      }
   }

   public static byte setBit(byte dataItem, int bitOffset, boolean flag) {
      byte f = (byte)(1 << bitOffset);
      return (byte)(flag ? dataItem | f : dataItem & ~f);
   }

   public static boolean getBit(byte dataItem, int bitOffset) {
      return (dataItem & 1 << bitOffset) != 0;
   }

   public static int floor(double val) {
      return (int)Math.floor(val);
   }

   public static int ceil(double val) {
      return (int)Math.ceil(val);
   }

   public static double tryParse(String val, double def) {
      if (val == null) {
         return def;
      } else {
         try {
            return Double.parseDouble(val);
         } catch (NumberFormatException var4) {
            return def;
         }
      }
   }

   public static boolean isBoundingBoxWithinDistance(@NotNull Vector start, @NotNull Vector direction, BoundingBox boundingBox, double maxDistance) {
      double startX = start.getX();
      double startY = start.getY();
      double startZ = start.getZ();
      Vector dir = direction.clone();
      if (dir.getX() == 0.0D) {
         dir.setX(0);
      }

      if (dir.getY() == 0.0D) {
         dir.setY(0);
      }

      if (dir.getZ() == 0.0D) {
         dir.setZ(0);
      }

      dir.normalize();
      double dirX = dir.getX();
      double dirY = dir.getY();
      double dirZ = dir.getZ();
      double divX = 1.0D / dirX;
      double divY = 1.0D / dirY;
      double divZ = 1.0D / dirZ;
      double tMin;
      double tMax;
      if (dirX >= 0.0D) {
         tMin = (boundingBox.getMinX() - startX) * divX;
         tMax = (boundingBox.getMaxX() - startX) * divX;
      } else {
         tMin = (boundingBox.getMaxX() - startX) * divX;
         tMax = (boundingBox.getMinX() - startX) * divX;
      }

      double tyMin;
      double tyMax;
      if (dirY >= 0.0D) {
         tyMin = (boundingBox.getMinY() - startY) * divY;
         tyMax = (boundingBox.getMaxY() - startY) * divY;
      } else {
         tyMin = (boundingBox.getMaxY() - startY) * divY;
         tyMax = (boundingBox.getMinY() - startY) * divY;
      }

      if (!(tMin > tyMax) && !(tMax < tyMin)) {
         if (tyMin > tMin) {
            tMin = tyMin;
         }

         if (tyMax < tMax) {
            tMax = tyMax;
         }

         double tzMin;
         double tzMax;
         if (dirZ >= 0.0D) {
            tzMin = (boundingBox.getMinZ() - startZ) * divZ;
            tzMax = (boundingBox.getMaxZ() - startZ) * divZ;
         } else {
            tzMin = (boundingBox.getMaxZ() - startZ) * divZ;
            tzMax = (boundingBox.getMinZ() - startZ) * divZ;
         }

         if (!(tMin > tzMax) && !(tMax < tzMin)) {
            if (tzMin > tMin) {
               tMin = tzMin;
            }

            if (tzMax < tMax) {
               tMax = tzMax;
            }

            if (tMax < 0.0D) {
               return false;
            } else {
               return !(tMin > maxDistance);
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static double distanceSquaredToBoundingBox(Vector origin, BoundingBox box) {
      double x = Math.max(Math.abs(origin.getX() - box.getCenterX()) - box.getWidthX() * 0.5D, 0.0D);
      double y = Math.max(Math.abs(origin.getY() - box.getCenterY()) - box.getHeight() * 0.5D, 0.0D);
      double z = Math.max(Math.abs(origin.getZ() - box.getCenterZ()) - box.getWidthZ() * 0.5D, 0.0D);
      return x * x + y * y + z * z;
   }

   public static boolean isSimilar(Vector a, Vector b) {
      return Math.abs(b.getX() - a.getX()) < movementResolution && Math.abs(b.getY() - a.getY()) < movementResolution && Math.abs(b.getZ() - a.getZ()) < movementResolution;
   }

   public static EulerAngle makeAngle(double x, double y, double z) {
      return new EulerAngle(Math.toRadians(x), Math.toRadians(y), Math.toRadians(z));
   }

   public static EulerAngle add(EulerAngle a, EulerAngle b) {
      return a.add(b.getX(), b.getY(), b.getZ());
   }

   public static float wrapRadian(float r) {
      r = (float)((double)r % 6.283185307179586D);
      if ((double)r < -3.141592653589793D) {
         r = (float)((double)r + 6.283185307179586D);
      }

      if ((double)r > 3.141592653589793D) {
         r = (float)((double)r - 6.283185307179586D);
      }

      return r;
   }

   public static float wrapDegree(float r) {
      r %= 360.0F;
      if (r < -180.0F) {
         r += 360.0F;
      }

      if (r > 180.0F) {
         r -= 360.0F;
      }

      return r;
   }

   public static float radianDifference(float a, float b) {
      return wrapRadian(b - a);
   }

   public static float degreeDifference(float a, float b) {
      return wrapDegree(b - a);
   }

   public static float rotateIfNecessary(float current, float target, float negativeClamp, float positiveClamp) {
      float delta = degreeDifference(current, target);
      float clampedDelta = clamp(delta, negativeClamp, positiveClamp);
      return target - clampedDelta;
   }

   public static byte rotToByte(float rot) {
      return (byte)((int)(rot * 256.0F / 360.0F));
   }

   public static float byteToRot(byte rot) {
      return (float)rot / 256.0F * 360.0F;
   }

   public static double lerp(double a, double b, double t) {
      return (1.0D - t) * a + t * b;
   }

   public static double lerp(double a, double b, double aT, double bT) {
      return aT * a + bT * b;
   }

   public static float lerp(float a, float b, float aT, float bT) {
      return aT * a + bT * b;
   }

   public static double rotLerp(double a, double b, double t) {
      return a + (double)degreeDifference((float)a, (float)b) * t;
   }

   public static float rotLerp(float a, float b, double t) {
      return (float)((double)a + (double)degreeDifference(a, b) * t);
   }

   public static double smoothLerp(double a, double b, double c, double d, double t) {
      double t0 = 0.0D;
      double t1 = 1.0D;
      double t2 = 2.0D;
      double t3 = 3.0D;
      t = (t2 - t1) * t + t1;
      double a1 = lerp(a, b, (t1 - t) / (t1 - t0), (t - t0) / (t1 - t0));
      double a2 = lerp(b, c, (t2 - t) / (t2 - t1), (t - t1) / (t2 - t1));
      double a3 = lerp(c, d, (t3 - t) / (t3 - t2), (t - t2) / (t3 - t2));
      double b1 = lerp(a1, a2, (t2 - t) / (t2 - t0), (t - t0) / (t2 - t0));
      double b2 = lerp(a2, a3, (t3 - t) / (t3 - t1), (t - t1) / (t3 - t1));
      return lerp(b1, b2, (t2 - t) / (t2 - t1), (t - t1) / (t2 - t1));
   }

   public static Vector lerp(Vector a, Vector b, double t) {
      return new Vector(lerp(a.getX(), b.getX(), t), lerp(a.getY(), b.getY(), t), lerp(a.getZ(), b.getZ(), t));
   }

   public static Vector lerp(Vector a, Vector b, double aT, double bT) {
      return new Vector(lerp(a.getX(), b.getX(), aT, bT), lerp(a.getY(), b.getY(), aT, bT), lerp(a.getZ(), b.getZ(), aT, bT));
   }

   public static Vector3f lerp(Vector3f a, Vector3f b, double t) {
      return new Vector3f((float)lerp((double)a.x, (double)b.x, t), (float)lerp((double)a.y, (double)b.y, t), (float)lerp((double)a.z, (double)b.z, t));
   }

   public static Vector3f lerp(Vector3f a, Vector3f b, float aT, float bT) {
      return new Vector3f(lerp(a.x, b.x, aT, bT), lerp(a.y, b.y, aT, bT), lerp(a.z, b.z, aT, bT));
   }

   public static Vector smoothLerp(Vector a, Vector b, Vector c, Vector d, double t) {
      double t0 = 0.0D;
      double t1 = 1.0D;
      double t2 = 2.0D;
      double t3 = 3.0D;
      t = (t2 - t1) * t + t1;
      Vector a1 = lerp(a, b, (t1 - t) / (t1 - t0), (t - t0) / (t1 - t0));
      Vector a2 = lerp(b, c, (t2 - t) / (t2 - t1), (t - t1) / (t2 - t1));
      Vector a3 = lerp(c, d, (t3 - t) / (t3 - t2), (t - t2) / (t3 - t2));
      Vector b1 = lerp(a1, a2, (t2 - t) / (t2 - t0), (t - t0) / (t2 - t0));
      Vector b2 = lerp(a2, a3, (t3 - t) / (t3 - t1), (t - t1) / (t3 - t1));
      return lerp(b1, b2, (t2 - t) / (t2 - t1), (t - t1) / (t2 - t1));
   }

   public static Vector3f smoothLerp(Vector3f a, Vector3f b, Vector3f c, Vector3f d, float t) {
      float t0 = 0.0F;
      float t1 = 1.0F;
      float t2 = 2.0F;
      float t3 = 3.0F;
      t = (t2 - t1) * t + t1;
      Vector3f a1 = lerp(a, b, (t1 - t) / (t1 - t0), (t - t0) / (t1 - t0));
      Vector3f a2 = lerp(b, c, (t2 - t) / (t2 - t1), (t - t1) / (t2 - t1));
      Vector3f a3 = lerp(c, d, (t3 - t) / (t3 - t2), (t - t2) / (t3 - t2));
      Vector3f b1 = lerp(a1, a2, (t2 - t) / (t2 - t0), (t - t0) / (t2 - t0));
      Vector3f b2 = lerp(a2, a3, (t3 - t) / (t3 - t1), (t - t1) / (t3 - t1));
      return lerp(b1, b2, (t2 - t) / (t2 - t1), (t - t1) / (t2 - t1));
   }

   public static EulerAngle lerp(EulerAngle a, EulerAngle b, double t) {
      return new EulerAngle(lerp(a.getX(), b.getX(), t), lerp(a.getY(), b.getY(), t), lerp(a.getZ(), b.getZ(), t));
   }

   public static EulerAngle lerp(EulerAngle a, EulerAngle b, double aT, double bT) {
      return new EulerAngle(lerp(a.getX(), b.getX(), aT, bT), lerp(a.getY(), b.getY(), aT, bT), lerp(a.getZ(), b.getZ(), aT, bT));
   }

   public static EulerAngle smoothLerp(EulerAngle a, EulerAngle b, EulerAngle c, EulerAngle d, double t) {
      double t0 = 0.0D;
      double t1 = 1.0D;
      double t2 = 2.0D;
      double t3 = 3.0D;
      t = (t2 - t1) * t + t1;
      EulerAngle a1 = lerp(a, b, (t1 - t) / (t1 - t0), (t - t0) / (t1 - t0));
      EulerAngle a2 = lerp(b, c, (t2 - t) / (t2 - t1), (t - t1) / (t2 - t1));
      EulerAngle a3 = lerp(c, d, (t3 - t) / (t3 - t2), (t - t2) / (t3 - t2));
      EulerAngle b1 = lerp(a1, a2, (t2 - t) / (t2 - t0), (t - t0) / (t2 - t0));
      EulerAngle b2 = lerp(a2, a3, (t3 - t) / (t3 - t1), (t - t1) / (t3 - t1));
      return lerp(b1, b2, (t2 - t) / (t2 - t1), (t - t1) / (t2 - t1));
   }

   public static Vector3f slerp(Vector3f a, Vector3f b, double t) {
      if (a.equals(b)) {
         return a;
      } else {
         Quaternionf qA = (new Quaternionf()).rotationZYX(a.z, a.y, a.x);
         Quaternionf qB = (new Quaternionf()).rotationZYX(b.z, b.y, b.x);
         qA.slerp(qB, (float)t);
         return getEulerAnglesZYX(qA, new Vector3f());
      }
   }

   public static Vector3f getEulerAnglesZYX(Quaternionf quaternionf, Vector3f eulerAngles) {
      float w = quaternionf.w;
      float x = quaternionf.x;
      float y = quaternionf.y;
      float z = quaternionf.z;
      eulerAngles.x = Math.atan2(2.0F * (y * z + x * w), z * z - y * y - x * x + w * w);
      eulerAngles.y = Math.safeAsin(-2.0F * (x * z - w * y));
      eulerAngles.z = Math.atan2(x * y + w * z, 0.5F - y * y - z * z);
      return eulerAngles;
   }

   public static Vector3d getEulerAnglesZYX(Quaterniond quaterniond, Vector3d eulerAngles) {
      double w = quaterniond.w;
      double x = quaterniond.x;
      double y = quaterniond.y;
      double z = quaterniond.z;
      eulerAngles.x = Math.atan2(2.0D * (y * z + x * w), z * z - y * y - x * x + w * w);
      eulerAngles.y = Math.safeAsin(-2.0D * (x * z - w * y));
      eulerAngles.z = Math.atan2(x * y + w * z, 0.5D - y * y - z * z);
      return eulerAngles;
   }

   public static String toString(EulerAngle angle) {
      return String.format("[%s, %s, %s]", Math.toDegrees(angle.getX()), Math.toDegrees(angle.getY()), Math.toDegrees(angle.getZ()));
   }

   public static UUID parseUUID(String uuid) {
      try {
         return UUID.fromString(uuid);
      } catch (IllegalArgumentException var6) {
         if (uuid.length() != 32) {
            throw var6;
         } else {
            long first = Long.parseUnsignedLong(uuid.substring(0, 16), 16);
            long last = Long.parseUnsignedLong(uuid.substring(16), 16);
            return new UUID(first, last);
         }
      }
   }

   static {
      slerpMode = TMath.SlerpMode.SLERP;
      movementResolution = 0.001D;
   }

   public static enum SlerpMode {
      SLERP,
      ONLERP;

      public static TMath.SlerpMode get(String name) {
         try {
            return valueOf(name);
         } catch (IllegalArgumentException var2) {
            return SLERP;
         }
      }

      // $FF: synthetic method
      private static TMath.SlerpMode[] $values() {
         return new TMath.SlerpMode[]{SLERP, ONLERP};
      }
   }
}
