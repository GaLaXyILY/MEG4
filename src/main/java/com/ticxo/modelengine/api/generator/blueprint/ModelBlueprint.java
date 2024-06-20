package com.ticxo.modelengine.api.generator.blueprint;

import com.google.common.collect.ImmutableMap;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.animation.BlueprintAnimation;
import com.ticxo.modelengine.api.entity.Hitbox;
import com.ticxo.modelengine.api.error.ErrorIncompatibleBone;
import com.ticxo.modelengine.api.error.ErrorUnknownBoneBehavior;
import com.ticxo.modelengine.api.error.WarnIncompatibleBoneBehavior;
import com.ticxo.modelengine.api.error.WarningDuplicateBoneName;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehaviorRegistry;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehaviorType;
import com.ticxo.modelengine.api.model.bone.render.DefaultRenderType;
import com.ticxo.modelengine.api.model.bone.render.IRenderType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class ModelBlueprint {
   private final Map<String, BlueprintBone> bones = new LinkedHashMap();
   private final Map<String, BlueprintAnimation> animations = new LinkedHashMap();
   private final transient Set<String> animationDescendingPriority = new LinkedHashSet();
   private final Map<String, String> animationsPlaceholders = new LinkedHashMap();
   private String name;
   private Hitbox mainHitbox = new Hitbox(0.6D, 1.8D, 0.6D, 1.44D);
   private float shadowRadius = 0.0F;
   private transient Map<String, BlueprintBone> flatMap;
   private transient boolean flatViewConstructed;
   private transient boolean descendingAnimationConstructed;
   private transient boolean boneBehaviorCached;

   public void finalizeModel() {
      this.constructFlatBoneMap();
      this.constructDescendingAnimation();
      this.cacheBoneBehaviors();
   }

   public void constructFlatBoneMap() {
      if (!this.flatViewConstructed) {
         this.flatViewConstructed = true;
         Map<String, BlueprintBone> tempMap = new LinkedHashMap();
         Set<BlueprintBone> bones = new LinkedHashSet(this.bones.values());
         LinkedHashSet buffer = new LinkedHashSet();

         while(!bones.isEmpty()) {
            Iterator var4 = bones.iterator();

            while(var4.hasNext()) {
               BlueprintBone bone = (BlueprintBone)var4.next();
               buffer.addAll(bone.getChildren().values());
               if (tempMap.containsKey(bone.getName())) {
                  (new WarningDuplicateBoneName(bone.getName())).log();
               } else {
                  tempMap.put(bone.getName(), bone);
               }
            }

            bones.clear();
            bones.addAll(buffer);
            buffer.clear();
         }

         this.flatMap = ImmutableMap.copyOf(tempMap);
      }
   }

   public void constructDescendingAnimation() {
      if (!this.descendingAnimationConstructed) {
         this.descendingAnimationConstructed = true;
         ArrayList<String> list = new ArrayList();
         this.animations.keySet().forEach((s) -> {
            list.add(0, s);
         });
         this.animationDescendingPriority.addAll(list);
      }
   }

   public void cacheBoneBehaviors() {
      if (!this.boneBehaviorCached) {
         this.boneBehaviorCached = true;

         assert this.flatMap != null : "Bone flat view map is null.";

         BoneBehaviorRegistry behaviorRegistry = ModelEngineAPI.getAPI().getBoneBehaviorRegistry();
         Iterator var2 = this.flatMap.entrySet().iterator();

         while(var2.hasNext()) {
            Entry<String, BlueprintBone> boneEntry = (Entry)var2.next();
            String name = (String)boneEntry.getKey();
            BlueprintBone bone = (BlueprintBone)boneEntry.getValue();
            Iterator var6 = bone.getBehaviors().entrySet().iterator();

            while(var6.hasNext()) {
               Entry<String, Map<String, Object>> entry = (Entry)var6.next();
               String bbId = (String)entry.getKey();
               BoneBehaviorType<?> behaviorType = behaviorRegistry.getById(bbId);
               if (behaviorType == null) {
                  (new ErrorUnknownBoneBehavior(name, bbId)).log();
               } else if (!this.canBoneUseBehavior(bone, behaviorType)) {
                  (new ErrorIncompatibleBone(bone.isRenderer(), name, bbId)).log();
               } else {
                  if (!behaviorType.test(bone.getCachedBehaviorProvider().keySet())) {
                     (new WarnIncompatibleBoneBehavior(name, bbId)).log();
                  }

                  behaviorType.assignCachedProvider(bone, (Map)entry.getValue());
               }
            }

            var6 = behaviorRegistry.getIds().iterator();

            while(var6.hasNext()) {
               String id = (String)var6.next();
               BoneBehaviorType<?> type = behaviorRegistry.getById(id);
               if (type != null) {
                  type.assignForcedCachedProvider(bone);
               }
            }
         }

      }
   }

   private boolean canBoneUseBehavior(BlueprintBone bone, BoneBehaviorType<?> type) {
      IRenderType renderType = type.getRenderType();
      if (renderType == DefaultRenderType.ANY) {
         return true;
      } else if (renderType == DefaultRenderType.MODEL) {
         return bone.isRenderer();
      } else {
         return !bone.isRenderer();
      }
   }

   public Map<String, BlueprintBone> getBones() {
      return this.bones;
   }

   public Map<String, BlueprintAnimation> getAnimations() {
      return this.animations;
   }

   public Set<String> getAnimationDescendingPriority() {
      return this.animationDescendingPriority;
   }

   public Map<String, String> getAnimationsPlaceholders() {
      return this.animationsPlaceholders;
   }

   public String getName() {
      return this.name;
   }

   public Hitbox getMainHitbox() {
      return this.mainHitbox;
   }

   public float getShadowRadius() {
      return this.shadowRadius;
   }

   public Map<String, BlueprintBone> getFlatMap() {
      return this.flatMap;
   }

   public boolean isFlatViewConstructed() {
      return this.flatViewConstructed;
   }

   public boolean isDescendingAnimationConstructed() {
      return this.descendingAnimationConstructed;
   }

   public boolean isBoneBehaviorCached() {
      return this.boneBehaviorCached;
   }

   public void setName(String name) {
      this.name = name;
   }

   public void setMainHitbox(Hitbox mainHitbox) {
      this.mainHitbox = mainHitbox;
   }

   public void setShadowRadius(float shadowRadius) {
      this.shadowRadius = shadowRadius;
   }

   public void setFlatViewConstructed(boolean flatViewConstructed) {
      this.flatViewConstructed = flatViewConstructed;
   }

   public void setDescendingAnimationConstructed(boolean descendingAnimationConstructed) {
      this.descendingAnimationConstructed = descendingAnimationConstructed;
   }

   public void setBoneBehaviorCached(boolean boneBehaviorCached) {
      this.boneBehaviorCached = boneBehaviorCached;
   }
}
