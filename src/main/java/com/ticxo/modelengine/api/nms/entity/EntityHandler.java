package com.ticxo.modelengine.api.nms.entity;

import com.ticxo.modelengine.api.entity.BaseEntity;
import com.ticxo.modelengine.api.entity.Hitbox;
import com.ticxo.modelengine.api.interaction.DynamicHitbox;
import com.ticxo.modelengine.api.model.bone.ModelBone;
import com.ticxo.modelengine.api.model.bone.type.SubHitbox;
import com.ticxo.modelengine.api.nms.entity.wrapper.BodyRotationController;
import com.ticxo.modelengine.api.nms.entity.wrapper.LookController;
import com.ticxo.modelengine.api.nms.entity.wrapper.MoveController;
import com.ticxo.modelengine.api.nms.entity.wrapper.TrackedEntity;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.Nullable;

public interface EntityHandler {
   int ON_MIN_X = 1;
   int ON_MAX_X = 2;
   int ON_MIN_Y = 4;
   int ON_MAX_Y = 8;
   int ON_MIN_Z = 16;
   int ON_MAX_Z = 32;

   static Set<EntityHandler.Point> getPoints(byte face) {
      HashSet<EntityHandler.Point> set = new HashSet();
      if ((face & 1) == 1) {
         set.add(EntityHandler.Point.CORNER_A);
         set.add(EntityHandler.Point.CORNER_E);
         set.add(EntityHandler.Point.CORNER_C);
         set.add(EntityHandler.Point.CORNER_G);
      }

      if ((face & 4) == 4) {
         set.add(EntityHandler.Point.CORNER_A);
         set.add(EntityHandler.Point.CORNER_E);
         set.add(EntityHandler.Point.CORNER_B);
         set.add(EntityHandler.Point.CORNER_F);
      }

      if ((face & 16) == 16) {
         set.add(EntityHandler.Point.CORNER_A);
         set.add(EntityHandler.Point.CORNER_C);
         set.add(EntityHandler.Point.CORNER_B);
         set.add(EntityHandler.Point.CORNER_D);
      }

      if ((face & 2) == 2) {
         set.add(EntityHandler.Point.CORNER_B);
         set.add(EntityHandler.Point.CORNER_F);
         set.add(EntityHandler.Point.CORNER_D);
         set.add(EntityHandler.Point.CORNER_H);
      }

      if ((face & 8) == 8) {
         set.add(EntityHandler.Point.CORNER_C);
         set.add(EntityHandler.Point.CORNER_G);
         set.add(EntityHandler.Point.CORNER_D);
         set.add(EntityHandler.Point.CORNER_H);
      }

      if ((face & 32) == 32) {
         set.add(EntityHandler.Point.CORNER_E);
         set.add(EntityHandler.Point.CORNER_G);
         set.add(EntityHandler.Point.CORNER_F);
         set.add(EntityHandler.Point.CORNER_H);
      }

      return set;
   }

   void updateConfig();

   int getNextEntityId();

   void setHitbox(Entity var1, Hitbox var2);

   void setStepHeight(Entity var1, double var2);

   double getStepHeight(Entity var1);

   void setPosition(Entity var1, double var2, double var4, double var6);

   void movePassenger(Entity var1, double var2, double var4, double var6);

   void forceSpawn(BaseEntity<?> var1, Player var2);

   default void forceSpawn(Entity entity) {
      TrackedEntity tracked = this.wrapTrackedEntity(entity);
      if (tracked != null) {
         tracked.broadcastSpawn();
      }

   }

   void forceDespawn(BaseEntity<?> var1, Player var2);

   default void forceDespawn(Entity entity) {
      TrackedEntity tracked = this.wrapTrackedEntity(entity);
      if (tracked != null) {
         tracked.broadcastRemove();
      }

   }

   void setForcedInvisible(Player var1, boolean var2);

   boolean isForcedInvisible(Player var1);

   BodyRotationController wrapBodyRotationControl(Entity var1, Supplier<BodyRotationController> var2);

   MoveController wrapMoveController(Entity var1, Supplier<MoveController> var2);

   LookController wrapLookController(Entity var1, Supplier<LookController> var2);

   void wrapNavigation(Entity var1);

   HitboxEntity createHitbox(Location var1, ModelBone var2, SubHitbox var3);

   @Nullable
   HitboxEntity castHitbox(Entity var1);

   boolean hurt(Entity var1, Object var2, float var3);

   EntityHandler.InteractionResult interact(Entity var1, HumanEntity var2, EquipmentSlot var3);

   void spawnDynamicHitbox(DynamicHitbox var1);

   void updateDynamicHitbox(DynamicHitbox var1);

   void destroyDynamicHitbox(DynamicHitbox var1);

   void forceUseItem(Player var1, EquipmentSlot var2);

   float getYRot(Entity var1);

   float getYHeadRot(Entity var1);

   float getXHeadRot(Entity var1);

   float getYBodyRot(Entity var1);

   void setYRot(Entity var1, float var2);

   void setYHeadRot(Entity var1, float var2);

   void setXHeadRot(Entity var1, float var2);

   void setYBodyRot(Entity var1, float var2);

   boolean isWalking(Entity var1);

   boolean isStrafing(Entity var1);

   boolean isJumping(Entity var1);

   boolean isFlying(Entity var1);

   boolean isRemoved(Entity var1);

   int getGlowColor(Entity var1);

   void setDeathTick(Entity var1, int var2);

   TrackedEntity wrapTrackedEntity(Entity var1);

   boolean shouldCull(Player var1, Entity var2, @Nullable Hitbox var3);

   public static enum Point {
      CORNER_A(0.0F, 0.0F, 0.0F),
      CORNER_B(1.0F, 0.0F, 0.0F),
      CORNER_C(0.0F, 1.0F, 0.0F),
      CORNER_D(1.0F, 1.0F, 0.0F),
      CORNER_E(0.0F, 0.0F, 1.0F),
      CORNER_F(1.0F, 0.0F, 1.0F),
      CORNER_G(0.0F, 1.0F, 1.0F),
      CORNER_H(1.0F, 1.0F, 1.0F),
      FACE_NX(0.0F, 0.5F, 0.5F),
      FACE_PX(1.0F, 0.5F, 0.5F),
      FACE_NY(0.5F, 0.0F, 0.5F),
      FACE_PY(0.5F, 1.0F, 0.5F),
      FACE_NZ(0.5F, 0.5F, 0.0F),
      FACE_PZ(0.5F, 0.5F, 1.0F);

      public final float x;
      public final float y;
      public final float z;

      private Point(float x, float y, float z) {
         this.x = x;
         this.y = y;
         this.z = z;
      }

      // $FF: synthetic method
      private static EntityHandler.Point[] $values() {
         return new EntityHandler.Point[]{CORNER_A, CORNER_B, CORNER_C, CORNER_D, CORNER_E, CORNER_F, CORNER_G, CORNER_H, FACE_NX, FACE_PX, FACE_NY, FACE_PY, FACE_NZ, FACE_PZ};
      }
   }

   public static enum InteractionResult {
      SUCCESS,
      CONSUME,
      CONSUME_PARTIAL,
      PASS,
      FAIL;

      // $FF: synthetic method
      private static EntityHandler.InteractionResult[] $values() {
         return new EntityHandler.InteractionResult[]{SUCCESS, CONSUME, CONSUME_PARTIAL, PASS, FAIL};
      }
   }

   public static enum BoxRelToCam {
      INSIDE,
      POSITIVE,
      NEGATIVE;

      public static EntityHandler.BoxRelToCam from(int min, int max, int pos) {
         if (max > pos && min > pos) {
            return POSITIVE;
         } else {
            return min < pos && max < pos ? NEGATIVE : INSIDE;
         }
      }

      // $FF: synthetic method
      private static EntityHandler.BoxRelToCam[] $values() {
         return new EntityHandler.BoxRelToCam[]{INSIDE, POSITIVE, NEGATIVE};
      }
   }
}
