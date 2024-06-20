package com.ticxo.modelengine.core.model.bone;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.ticxo.modelengine.api.entity.data.IEntityData;
import com.ticxo.modelengine.api.generator.BaseItemEnum;
import com.ticxo.modelengine.api.generator.blueprint.BlueprintBone;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.model.bone.ModelBone;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehavior;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehaviorType;
import com.ticxo.modelengine.api.model.bone.behavior.ProceduralType;
import com.ticxo.modelengine.api.utils.OffsetMode;
import com.ticxo.modelengine.api.utils.StepFlag;
import com.ticxo.modelengine.api.utils.config.ConfigProperty;
import com.ticxo.modelengine.api.utils.data.io.SavedData;
import com.ticxo.modelengine.api.utils.data.tracker.DataTracker;
import com.ticxo.modelengine.api.utils.data.tracker.UpdateDataTracker;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class ModelBoneImpl implements ModelBone {
   @NotNull
   private final ActiveModel activeModel;
   @NotNull
   private final BlueprintBone blueprintBone;
   private final Map<String, ModelBone> children = Maps.newConcurrentMap();
   private final Map<BoneBehaviorType<?>, BoneBehavior> boneBehaviors = new LinkedHashMap();
   private final DataTracker<ItemStack> modelTracker;
   private final DataTracker<Vector3f> modelScale;
   private final Set<ProceduralType> proceduralTypes;
   private final AtomicReference<Vector3f> trueGlobalPosition;
   private final AtomicReference<Quaternionf> trueGlobalLeftRotation;
   private final AtomicReference<Vector3f> trueGlobalScale;
   private final AtomicReference<Quaternionf> trueGlobalRightRotation;
   private Vector3f cachedPosition;
   private Vector3f cachedLeftRotation;
   private Vector3f cachedScale;
   private Vector3f cachedRightRotation;
   private Vector3f globalPosition;
   private Quaternionf globalLeftRotation;
   private Vector3f globalScale;
   private Quaternionf globalRightRotation;
   private final DataTracker<Byte> trackedStepFlags;
   private byte stepFlags;
   private String customId;
   @Nullable
   private ModelBone parent;
   private boolean isRenderer;
   private float yaw;
   private boolean hasGlobalRotation;
   private Color lastColor;
   private Color defaultTint;
   private Color damageTint;
   private boolean enchanted;
   private Boolean glowing;
   private Integer glowColor;
   private int blockLight;
   private int skyLight;
   private boolean visible;
   private boolean markedDestroy;

   public ModelBoneImpl(@NotNull ActiveModel activeModel, @NotNull BlueprintBone blueprintBone) {
      this.modelTracker = new DataTracker(new ItemStack(Material.AIR));
      this.modelScale = new UpdateDataTracker(new Vector3f(), Vector3f::set);
      this.proceduralTypes = new HashSet();
      this.trueGlobalPosition = new AtomicReference(new Vector3f());
      this.trueGlobalLeftRotation = new AtomicReference(new Quaternionf());
      this.trueGlobalScale = new AtomicReference(new Vector3f());
      this.trueGlobalRightRotation = new AtomicReference(new Quaternionf());
      this.cachedPosition = new Vector3f();
      this.cachedLeftRotation = new Vector3f();
      this.cachedScale = new Vector3f();
      this.cachedRightRotation = new Vector3f();
      this.globalPosition = new Vector3f();
      this.globalLeftRotation = new Quaternionf();
      this.globalScale = new Vector3f();
      this.globalRightRotation = new Quaternionf();
      this.trackedStepFlags = new DataTracker();
      this.customId = null;
      this.blockLight = -1;
      this.skyLight = -1;
      this.visible = true;
      this.activeModel = activeModel;
      this.blueprintBone = blueprintBone;
      this.globalPosition.set(blueprintBone.getRotatedGlobalPosition());
      this.globalLeftRotation.set(blueprintBone.getGlobalQuaternion());
      this.modelScale.set((new Vector3f(blueprintBone.getModelScale())).mul((float)blueprintBone.getScale()));
      this.globalScale.set((Vector3fc)this.modelScale.get());
      this.isRenderer = blueprintBone.isRenderer();
      if (this.isRenderer) {
         this.setModel(blueprintBone.getDataId());
      } else {
         BlueprintBone dupeTarget = blueprintBone.getDupeTarget();
         if (dupeTarget != null && dupeTarget.isRenderer()) {
            this.setModel(dupeTarget.getDataId());
            this.isRenderer = true;
         }
      }

      this.setVisible(blueprintBone.isRenderByDefault());
   }

   public String getUniqueBoneId() {
      return this.getCustomId() == null ? this.getBoneId() : this.getCustomId();
   }

   public String getBoneId() {
      return this.blueprintBone.getName();
   }

   public void setParent(@Nullable ModelBone bone) {
      if (this.parent != null) {
         this.parent.getChildren().remove(this.getUniqueBoneId());
      }

      this.parent = bone;
      if (this.parent != null) {
         this.parent.getChildren().put(this.getUniqueBoneId(), this);
      }

      this.forBehaviors((boneBehavior) -> {
         boneBehavior.onParentSwap(this.parent);
      });
   }

   public void tick() {
      this.stepFlags = 0;
      this.trackedStepFlags.clearDirty();
      this.tryTintModel();
      this.cachedPosition.set(0.0F, 0.0F, 0.0F);
      this.cachedLeftRotation.set(0.0F, 0.0F, 0.0F);
      this.cachedScale.set(1.0F, 1.0F, 1.0F);
      this.cachedRightRotation.set(0.0F, 0.0F, 0.0F);
      this.forBehaviors(BoneBehavior::preAnimation);
      if (this.proceduralTypes.contains(ProceduralType.ANIMATION)) {
         this.forBehaviors(BoneBehavior::onAnimation);
      } else {
         this.activeModel.getAnimationHandler().updateBone(this);
      }

      this.forBehaviors(BoneBehavior::postAnimation);
      this.forBehaviors(BoneBehavior::preGlobalCalculation);
      if (this.proceduralTypes.contains(ProceduralType.TRANSFORM)) {
         this.forBehaviors(BoneBehavior::onGlobalCalculation);
      } else {
         this.calculateGlobalTransform();
      }

      this.forBehaviors(BoneBehavior::postGlobalCalculation);
      this.yaw = this.activeModel.getModeledEntity().getYBodyRot();
      if (!this.children.isEmpty()) {
         this.forBehaviors(BoneBehavior::preChildCalculation);
         this.children.values().forEach(ModelBone::tick);
         this.forBehaviors(BoneBehavior::postChildCalculation);
      }

      this.globalScale.mul((Vector3fc)this.modelScale.get()).mul(this.activeModel.getScale());
      this.globalPosition.mul(this.activeModel.getScale());
      this.forBehaviors(BoneBehavior::onFinalize);
      this.trackedStepFlags.set(this.stepFlags);
      this.trueGlobalPosition.set(this.globalPosition);
      this.trueGlobalLeftRotation.set(this.globalLeftRotation);
      this.trueGlobalScale.set(this.globalScale);
      this.trueGlobalRightRotation.set(this.globalRightRotation);
   }

   public void destroy() {
      this.markedDestroy = true;
      this.boneBehaviors.values().forEach(BoneBehavior::onRemove);
      this.children.values().forEach(ModelBone::destroy);
      this.children.clear();
      this.getData().markBoneGlowing(this, false);
      if (this.parent != null && !this.parent.isMarkedDestroy()) {
         this.parent.getChildren().remove(this.getUniqueBoneId());
      }

      this.activeModel.removeBone(this.getUniqueBoneId());
   }

   public void setModelScale(int scale) {
      this.modelScale.set(new Vector3f((float)scale));
   }

   public void calculateGlobalTransform() {
      this.cachedPosition.add(this.blueprintBone.getLocalPosition());
      this.cachedLeftRotation.add(this.blueprintBone.getLocalRotation());
      Quaternionf localQuaternion = (new Quaternionf()).rotationZYX(this.cachedLeftRotation.z, this.cachedLeftRotation.y, this.cachedLeftRotation.x);
      if (this.parent != null) {
         Vector3f parentPosition = this.parent.getGlobalPosition();
         Quaternionf parentRotation = this.parent.getGlobalLeftRotation();
         Vector3f parentScale = this.parent.getGlobalScale();
         parentPosition.add(this.cachedPosition.mul(parentScale).rotate(parentRotation), this.globalPosition);
         if (!this.hasGlobalRotation()) {
            parentRotation.mul(localQuaternion, this.globalLeftRotation);
         } else {
            this.globalLeftRotation.set(localQuaternion);
         }

         parentScale.mul(this.cachedScale, this.globalScale);
      } else {
         this.globalPosition.set(this.cachedPosition);
         this.globalLeftRotation.set(localQuaternion);
         this.globalScale.set(this.cachedScale);
      }

   }

   public Vector3f getTrueGlobalPosition() {
      return (Vector3f)this.trueGlobalPosition.get();
   }

   public Quaternionf getTrueGlobalLeftRotation() {
      return (Quaternionf)this.trueGlobalLeftRotation.get();
   }

   public Vector3f getTrueGlobalScale() {
      return (Vector3f)this.trueGlobalScale.get();
   }

   public Quaternionf getTrueGlobalRightRotation() {
      return (Quaternionf)this.trueGlobalRightRotation.get();
   }

   public boolean hasGlobalRotation() {
      return this.hasGlobalRotation;
   }

   public ItemStack getModel() {
      return (ItemStack)this.modelTracker.get();
   }

   public void setModel(int data) {
      BaseItemEnum base = ConfigProperty.ITEM_MODEL.getBaseItem();
      ItemStack model = (ItemStack)this.modelTracker.get();
      model.setType(base.getMaterial());
      ItemMeta meta = model.getItemMeta();
      base.color(meta, this.getColor());
      meta.setCustomModelData(data);
      model.setItemMeta(meta);
      this.modelTracker.set(model);
      this.modelTracker.markDirty();
   }

   public void setModel(ItemStack stack) {
      this.modelTracker.set(stack);
   }

   public Color getDefaultTint() {
      return this.defaultTint == null ? this.activeModel.getDefaultTint() : this.defaultTint;
   }

   public Color getDamageTint() {
      return this.damageTint == null ? this.activeModel.getDamageTint() : this.damageTint;
   }

   public void setEnchanted(boolean flag) {
      if (this.isEnchanted() != flag) {
         this.enchanted = flag;
         ItemStack model = (ItemStack)this.modelTracker.get();
         if (flag) {
            model.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);
         } else {
            model.removeEnchantment(Enchantment.VANISHING_CURSE);
         }

         this.modelTracker.markDirty();
      }
   }

   public boolean isGlowing() {
      return this.glowing == null ? this.activeModel.isGlowing() : this.glowing;
   }

   public void setGlowing(@Nullable Boolean flag) {
      this.glowing = flag;
      this.getData().markBoneGlowing(this, this.glowing != null && this.glowing);
   }

   public int getGlowColor() {
      return this.glowColor == null ? this.activeModel.getGlowColor() : this.glowColor;
   }

   public int getBlockLight() {
      return this.blockLight == -1 ? this.activeModel.getBlockLight() : this.blockLight;
   }

   public int getSkyLight() {
      return this.skyLight == -1 ? this.activeModel.getSkyLight() : this.skyLight;
   }

   public boolean isEffectivelyInvisible() {
      return !this.isRenderer || (double)((Vector3f)this.modelScale.get()).lengthSquared() < 1.0E-5D || (double)this.globalScale.lengthSquared() < 1.0E-5D;
   }

   public boolean shouldStep() {
      return (Byte)this.trackedStepFlags.get() == 0 && this.trackedStepFlags.isDirty();
   }

   public void markStep(StepFlag flag) {
      this.stepFlags = flag.setStep(this.stepFlags, true);
   }

   public boolean pollModelScaleChanged() {
      if (this.modelScale.isDirty()) {
         this.modelScale.clearDirty();
         return true;
      } else {
         return false;
      }
   }

   public Location getLocation() {
      return this.getLocation(OffsetMode.LOCAL, new Vector3f(), true);
   }

   public Location getLocation(OffsetMode mode, Vector3f offset, boolean scale) {
      ModeledEntity modeledEntity = this.getActiveModel().getModeledEntity();
      float angle = (180.0F - modeledEntity.getYBodyRot()) * 0.017453292F;
      Vector3f bonePosition = this.getTrueGlobalPosition();
      Quaternionf boneRotation = this.getTrueGlobalLeftRotation();
      if (scale) {
         offset.mul(this.activeModel.getScale());
      }

      Vector3f var10000;
      switch(mode) {
      case LOCAL:
         var10000 = bonePosition.add(offset.rotate(boneRotation), new Vector3f()).rotateY(angle);
         break;
      case MODEL:
         var10000 = bonePosition.add(offset, new Vector3f()).rotateY(angle);
         break;
      case GLOBAL:
         var10000 = bonePosition.rotateY(angle, new Vector3f()).add(offset);
         break;
      default:
         throw new IncompatibleClassChangeError();
      }

      bonePosition = var10000;
      return modeledEntity.getBase().getLocation().clone().add((double)bonePosition.x, (double)bonePosition.y, (double)bonePosition.z);
   }

   public void addBoneBehavior(BoneBehavior boneBehavior) {
      this.boneBehaviors.put(boneBehavior.getType(), boneBehavior);
      boneBehavior.onApply();
      this.proceduralTypes.addAll(boneBehavior.getType().getProceduralTypes());
   }

   public boolean hasBoneBehavior(BoneBehaviorType<?> type) {
      return this.boneBehaviors.containsKey(type);
   }

   public <T extends BoneBehavior> Optional<T> getBoneBehavior(BoneBehaviorType<T> type) {
      BoneBehavior behavior = (BoneBehavior)this.boneBehaviors.get(type);
      return Optional.ofNullable(behavior);
   }

   public <T extends BoneBehavior> Optional<T> removeBoneBehavior(BoneBehaviorType<T> type) {
      BoneBehavior behavior = (BoneBehavior)this.boneBehaviors.remove(type);
      if (behavior != null) {
         behavior.onRemove();
      }

      return Optional.ofNullable(behavior);
   }

   public Map<BoneBehaviorType<?>, BoneBehavior> getImmutableBoneBehaviors() {
      return ImmutableMap.copyOf(this.boneBehaviors);
   }

   private void forBehaviors(Consumer<BoneBehavior> consumer) {
      this.boneBehaviors.values().forEach(consumer);
   }

   private void tryTintModel() {
      ItemStack model = (ItemStack)this.modelTracker.get();
      BaseItemEnum base = BaseItemEnum.fromMaterial(model.getType());
      if (base != null) {
         Color color = this.getColor();
         if (color != this.lastColor) {
            this.lastColor = color;
            ItemMeta meta = model.getItemMeta();
            base.color(meta, color);
            model.setItemMeta(meta);
            this.modelTracker.markDirty();
         }
      }
   }

   private Color getColor() {
      return this.activeModel.isMarkedHurt() ? this.getDamageTint() : this.getDefaultTint();
   }

   public void save(SavedData data) {
      if (this.defaultTint != null) {
         data.putInt("default_tint", this.defaultTint.asRGB());
      }

      if (this.damageTint != null) {
         data.putInt("damage_tint", this.damageTint.asRGB());
      }

      if (this.blueprintBone.isRenderByDefault() != this.isVisible()) {
         data.putBoolean("visible", this.isVisible());
      }

      if (this.isEnchanted()) {
         data.putBoolean("enchant", true);
      }

      if (this.glowing != null) {
         data.putBoolean("glowing", this.glowing);
      }

      if (this.glowColor != null) {
         data.putInt("glow_color", this.glowColor);
      }

      if (this.blockLight > 0) {
         data.putInt("block_light", this.blockLight);
      }

      if (this.skyLight > 0) {
         data.putInt("sky_light", this.skyLight);
      }

      this.forBehaviors((behavior) -> {
         behavior.save().ifPresent((data1) -> {
            data.putData(behavior.getType().getId(), data1);
         });
      });
   }

   public void load(SavedData data) {
      Integer defaultTint = data.getInt("default_tint");
      if (defaultTint != null) {
         this.setDefaultTint(Color.fromRGB(defaultTint));
      }

      Integer damageTint = data.getInt("damage_tint");
      if (damageTint != null) {
         this.setDamageTint(Color.fromRGB(damageTint));
      }

      Boolean visible = data.getBoolean("visible");
      if (visible != null) {
         this.setVisible(visible);
      }

      if (data.getBoolean("enchant", false)) {
         this.setEnchanted(true);
      }

      this.setGlowing(data.getBoolean("glowing"));
      this.setGlowColor(data.getInt("glow_color"));
      this.setBlockLight(data.getInt("block_light", -1));
      this.setSkyLight(data.getInt("sky_light", -1));
      this.forBehaviors((behavior) -> {
         Optional var10000 = data.getData(behavior.getType().getId());
         Objects.requireNonNull(behavior);
         var10000.ifPresent(behavior::load);
      });
   }

   private IEntityData getData() {
      return this.activeModel.getModeledEntity().getBase().getData();
   }

   @NotNull
   public ActiveModel getActiveModel() {
      return this.activeModel;
   }

   @NotNull
   public BlueprintBone getBlueprintBone() {
      return this.blueprintBone;
   }

   public Map<String, ModelBone> getChildren() {
      return this.children;
   }

   public DataTracker<ItemStack> getModelTracker() {
      return this.modelTracker;
   }

   public DataTracker<Vector3f> getModelScale() {
      return this.modelScale;
   }

   public Set<ProceduralType> getProceduralTypes() {
      return this.proceduralTypes;
   }

   public Vector3f getCachedPosition() {
      return this.cachedPosition;
   }

   public Vector3f getCachedLeftRotation() {
      return this.cachedLeftRotation;
   }

   public Vector3f getCachedScale() {
      return this.cachedScale;
   }

   public Vector3f getCachedRightRotation() {
      return this.cachedRightRotation;
   }

   public Vector3f getGlobalPosition() {
      return this.globalPosition;
   }

   public Quaternionf getGlobalLeftRotation() {
      return this.globalLeftRotation;
   }

   public Vector3f getGlobalScale() {
      return this.globalScale;
   }

   public Quaternionf getGlobalRightRotation() {
      return this.globalRightRotation;
   }

   public DataTracker<Byte> getTrackedStepFlags() {
      return this.trackedStepFlags;
   }

   public byte getStepFlags() {
      return this.stepFlags;
   }

   public String getCustomId() {
      return this.customId;
   }

   @Nullable
   public ModelBone getParent() {
      return this.parent;
   }

   public boolean isRenderer() {
      return this.isRenderer;
   }

   public float getYaw() {
      return this.yaw;
   }

   public boolean isHasGlobalRotation() {
      return this.hasGlobalRotation;
   }

   public Color getLastColor() {
      return this.lastColor;
   }

   public boolean isEnchanted() {
      return this.enchanted;
   }

   public boolean isVisible() {
      return this.visible;
   }

   public boolean isMarkedDestroy() {
      return this.markedDestroy;
   }

   public void setCachedPosition(Vector3f cachedPosition) {
      this.cachedPosition = cachedPosition;
   }

   public void setCachedLeftRotation(Vector3f cachedLeftRotation) {
      this.cachedLeftRotation = cachedLeftRotation;
   }

   public void setCachedScale(Vector3f cachedScale) {
      this.cachedScale = cachedScale;
   }

   public void setCachedRightRotation(Vector3f cachedRightRotation) {
      this.cachedRightRotation = cachedRightRotation;
   }

   public void setGlobalPosition(Vector3f globalPosition) {
      this.globalPosition = globalPosition;
   }

   public void setGlobalLeftRotation(Quaternionf globalLeftRotation) {
      this.globalLeftRotation = globalLeftRotation;
   }

   public void setGlobalScale(Vector3f globalScale) {
      this.globalScale = globalScale;
   }

   public void setGlobalRightRotation(Quaternionf globalRightRotation) {
      this.globalRightRotation = globalRightRotation;
   }

   public void setCustomId(String customId) {
      this.customId = customId;
   }

   public void setRenderer(boolean isRenderer) {
      this.isRenderer = isRenderer;
   }

   public void setYaw(float yaw) {
      this.yaw = yaw;
   }

   public void setHasGlobalRotation(boolean hasGlobalRotation) {
      this.hasGlobalRotation = hasGlobalRotation;
   }

   public void setDefaultTint(Color defaultTint) {
      this.defaultTint = defaultTint;
   }

   public void setDamageTint(Color damageTint) {
      this.damageTint = damageTint;
   }

   public void setGlowColor(Integer glowColor) {
      this.glowColor = glowColor;
   }

   public void setBlockLight(int blockLight) {
      this.blockLight = blockLight;
   }

   public void setSkyLight(int skyLight) {
      this.skyLight = skyLight;
   }

   public void setVisible(boolean visible) {
      this.visible = visible;
   }

   public void setMarkedDestroy(boolean markedDestroy) {
      this.markedDestroy = markedDestroy;
   }
}
