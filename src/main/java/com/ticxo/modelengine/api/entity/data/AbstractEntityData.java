package com.ticxo.modelengine.api.entity.data;

import com.ticxo.modelengine.api.entity.CullType;
import com.ticxo.modelengine.api.entity.Hitbox;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.bone.ModelBone;
import com.ticxo.modelengine.api.utils.config.ConfigProperty;
import com.ticxo.modelengine.api.utils.math.TMath;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractEntityData implements IEntityData {
   private static int CULL_INTERVAL;
   private static boolean CULL_VERTICAL;
   private static double CULL_VERTICAL_DISTANCE;
   private static CullType CULL_VERTICAL_TYPE;
   private static boolean CULL_BEHIND;
   private static double VIEW_ANGLE;
   private static double UPDATE_BEHIND_RADIUS_SQR;
   private static CullType CULL_BEHIND_TYPE;
   private static boolean CULL_BLOCKED;
   private static double FORCE_UNBLOCKED_RADIUS_SQR;
   private static CullType CULL_BLOCKED_TYPE;
   private final Set<ActiveModel> glowingModel = new HashSet();
   private final Set<ModelBone> glowingBone = new HashSet();
   private Hitbox cullHitbox;
   private Integer cullInterval;
   private Boolean verticalCull;
   private Double verticalCullDistance;
   private CullType verticalCullType;
   private Boolean backCull;
   private Double backCullAngle;
   private Double backCullIgnoreRadius;
   private CullType backCullType;
   private Boolean blockedCull;
   private Double blockedCullIgnoreRadius;
   private CullType blockedCullType;

   public static void updateConfig() {
      CULL_INTERVAL = Math.max(ConfigProperty.CULL_INTERVAL.getInt(), 1);
      CULL_VERTICAL = ConfigProperty.VERTICAL_CULL_ENABLE.getBoolean();
      CULL_VERTICAL_DISTANCE = ConfigProperty.VERTICAL_CULL_DISTANCE.getDouble();
      CULL_VERTICAL_TYPE = ConfigProperty.VERTICAL_CULL_TYPE.getCullType();
      CULL_BEHIND = ConfigProperty.BACKWARDS_CULL_ENABLED.getBoolean();
      VIEW_ANGLE = Math.cos(TMath.clamp(ConfigProperty.BACKWARDS_CULL_ANGLE.getDouble(), 0.0D, 360.0D) * 0.5D * 0.01745329238474369D);
      UPDATE_BEHIND_RADIUS_SQR = ConfigProperty.BACKWARDS_CULL_IGNORE_RADIUS.getDouble();
      UPDATE_BEHIND_RADIUS_SQR *= UPDATE_BEHIND_RADIUS_SQR;
      CULL_BEHIND_TYPE = ConfigProperty.BACKWARDS_CULL_TYPE.getCullType();
      CULL_BLOCKED = ConfigProperty.BLOCK_CULL_ENABLE.getBoolean();
      FORCE_UNBLOCKED_RADIUS_SQR = ConfigProperty.BLOCK_CULL_IGNORE_RADIUS.getDouble();
      FORCE_UNBLOCKED_RADIUS_SQR *= FORCE_UNBLOCKED_RADIUS_SQR;
      CULL_BLOCKED_TYPE = ConfigProperty.BLOCK_CULL_TYPE.getCullType();
   }

   public int cullInterval() {
      return this.cullInterval == null ? CULL_INTERVAL : this.cullInterval;
   }

   public boolean verticalCull() {
      return this.verticalCull == null ? CULL_VERTICAL : this.verticalCull;
   }

   public double verticalCullDistance() {
      return this.verticalCullDistance == null ? CULL_VERTICAL_DISTANCE : this.verticalCullDistance;
   }

   public CullType verticalCullType() {
      return this.verticalCullType == null ? CULL_VERTICAL_TYPE : this.verticalCullType;
   }

   public boolean backCull() {
      return this.backCull == null ? CULL_BEHIND : this.backCull;
   }

   public double backCullAngle() {
      return this.backCullAngle == null ? VIEW_ANGLE : this.backCullAngle;
   }

   public double backCullIgnoreRadius() {
      return this.backCullIgnoreRadius == null ? UPDATE_BEHIND_RADIUS_SQR : this.backCullIgnoreRadius * this.backCullIgnoreRadius;
   }

   public CullType backCullType() {
      return this.backCullType == null ? CULL_BEHIND_TYPE : this.backCullType;
   }

   public boolean blockedCull() {
      return this.blockedCull == null ? CULL_BLOCKED : this.blockedCull;
   }

   public double blockedCullIgnoreRadius() {
      return this.blockedCullIgnoreRadius == null ? FORCE_UNBLOCKED_RADIUS_SQR : this.blockedCullIgnoreRadius * this.blockedCullIgnoreRadius;
   }

   public CullType blockedCullType() {
      return this.blockedCullType == null ? CULL_BLOCKED_TYPE : this.blockedCullType;
   }

   public void markModelGlowing(ActiveModel model, boolean flag) {
      if (flag) {
         this.glowingModel.add(model);
      } else {
         this.glowingModel.remove(model);
      }

   }

   public void markBoneGlowing(ModelBone bone, boolean flag) {
      if (flag) {
         this.glowingBone.add(bone);
      } else {
         this.glowingBone.remove(bone);
      }

   }

   public boolean isModelGlowing() {
      return !this.glowingBone.isEmpty() || !this.glowingModel.isEmpty();
   }

   public Hitbox getCullHitbox() {
      return this.cullHitbox;
   }

   public void setCullHitbox(Hitbox cullHitbox) {
      this.cullHitbox = cullHitbox;
   }

   public Integer getCullInterval() {
      return this.cullInterval;
   }

   public void setCullInterval(Integer cullInterval) {
      this.cullInterval = cullInterval;
   }

   public Boolean getVerticalCull() {
      return this.verticalCull;
   }

   public void setVerticalCull(Boolean verticalCull) {
      this.verticalCull = verticalCull;
   }

   public Double getVerticalCullDistance() {
      return this.verticalCullDistance;
   }

   public void setVerticalCullDistance(Double verticalCullDistance) {
      this.verticalCullDistance = verticalCullDistance;
   }

   public CullType getVerticalCullType() {
      return this.verticalCullType;
   }

   public void setVerticalCullType(CullType verticalCullType) {
      this.verticalCullType = verticalCullType;
   }

   public Boolean getBackCull() {
      return this.backCull;
   }

   public void setBackCull(Boolean backCull) {
      this.backCull = backCull;
   }

   public Double getBackCullAngle() {
      return this.backCullAngle;
   }

   public void setBackCullAngle(Double backCullAngle) {
      this.backCullAngle = backCullAngle;
   }

   public Double getBackCullIgnoreRadius() {
      return this.backCullIgnoreRadius;
   }

   public void setBackCullIgnoreRadius(Double backCullIgnoreRadius) {
      this.backCullIgnoreRadius = backCullIgnoreRadius;
   }

   public CullType getBackCullType() {
      return this.backCullType;
   }

   public void setBackCullType(CullType backCullType) {
      this.backCullType = backCullType;
   }

   public Boolean getBlockedCull() {
      return this.blockedCull;
   }

   public void setBlockedCull(Boolean blockedCull) {
      this.blockedCull = blockedCull;
   }

   public Double getBlockedCullIgnoreRadius() {
      return this.blockedCullIgnoreRadius;
   }

   public void setBlockedCullIgnoreRadius(Double blockedCullIgnoreRadius) {
      this.blockedCullIgnoreRadius = blockedCullIgnoreRadius;
   }

   public CullType getBlockedCullType() {
      return this.blockedCullType;
   }

   public void setBlockedCullType(CullType blockedCullType) {
      this.blockedCullType = blockedCullType;
   }
}
