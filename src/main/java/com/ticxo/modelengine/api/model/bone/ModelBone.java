package com.ticxo.modelengine.api.model.bone;

import com.ticxo.modelengine.api.generator.blueprint.BlueprintBone;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehavior;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehaviorType;
import com.ticxo.modelengine.api.utils.OffsetMode;
import com.ticxo.modelengine.api.utils.StepFlag;
import com.ticxo.modelengine.api.utils.data.io.DataIO;
import com.ticxo.modelengine.api.utils.data.tracker.DataTracker;
import java.util.Map;
import java.util.Optional;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public interface ModelBone extends DataIO {
   String getUniqueBoneId();

   String getBoneId();

   String getCustomId();

   void setCustomId(String var1);

   ActiveModel getActiveModel();

   BlueprintBone getBlueprintBone();

   @Nullable
   ModelBone getParent();

   void setParent(@Nullable ModelBone var1);

   Map<String, ModelBone> getChildren();

   boolean isRenderer();

   void setRenderer(boolean var1);

   void tick();

   boolean isMarkedDestroy();

   void destroy();

   float getYaw();

   void setYaw(float var1);

   void setModelScale(int var1);

   void calculateGlobalTransform();

   Vector3f getCachedPosition();

   void setCachedPosition(Vector3f var1);

   Vector3f getGlobalPosition();

   void setGlobalPosition(Vector3f var1);

   Vector3f getTrueGlobalPosition();

   Vector3f getCachedLeftRotation();

   void setCachedLeftRotation(Vector3f var1);

   Quaternionf getGlobalLeftRotation();

   void setGlobalLeftRotation(Quaternionf var1);

   Quaternionf getTrueGlobalLeftRotation();

   Vector3f getCachedScale();

   void setCachedScale(Vector3f var1);

   Vector3f getGlobalScale();

   void setGlobalScale(Vector3f var1);

   Vector3f getTrueGlobalScale();

   Vector3f getCachedRightRotation();

   void setCachedRightRotation(Vector3f var1);

   Quaternionf getGlobalRightRotation();

   void setGlobalRightRotation(Quaternionf var1);

   Quaternionf getTrueGlobalRightRotation();

   boolean hasGlobalRotation();

   void setHasGlobalRotation(boolean var1);

   ItemStack getModel();

   void setModel(int var1);

   void setModel(ItemStack var1);

   DataTracker<ItemStack> getModelTracker();

   Color getDefaultTint();

   void setDefaultTint(Color var1);

   Color getDamageTint();

   void setDamageTint(Color var1);

   boolean isEnchanted();

   void setEnchanted(boolean var1);

   boolean isVisible();

   void setVisible(boolean var1);

   boolean isGlowing();

   void setGlowing(@Nullable Boolean var1);

   int getGlowColor();

   void setGlowColor(@Nullable Integer var1);

   int getBlockLight();

   void setBlockLight(int var1);

   int getSkyLight();

   void setSkyLight(int var1);

   default int getBrightness() {
      int blockLight = this.getBlockLight();
      int skyLight = this.getSkyLight();
      if (blockLight < 0 && skyLight < 0) {
         return -1;
      } else {
         blockLight = Math.max(blockLight, 0);
         skyLight = Math.max(skyLight, 0);
         return blockLight << 4 | skyLight << 20;
      }
   }

   boolean isEffectivelyInvisible();

   boolean shouldStep();

   void markStep(StepFlag var1);

   boolean pollModelScaleChanged();

   Location getLocation();

   Location getLocation(OffsetMode var1, Vector3f var2, boolean var3);

   void addBoneBehavior(BoneBehavior var1);

   boolean hasBoneBehavior(BoneBehaviorType<?> var1);

   <T extends BoneBehavior> Optional<T> getBoneBehavior(BoneBehaviorType<T> var1);

   <T extends BoneBehavior> Optional<T> removeBoneBehavior(BoneBehaviorType<T> var1);

   Map<BoneBehaviorType<?>, BoneBehavior> getImmutableBoneBehaviors();
}
