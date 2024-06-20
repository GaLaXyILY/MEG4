package com.ticxo.modelengine.api.generator.blueprint;

import com.ticxo.modelengine.api.model.bone.behavior.BoneBehaviorType;
import java.util.LinkedHashMap;
import java.util.Map;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class BlueprintBone {
   private final Map<String, BlueprintBone> children = new LinkedHashMap();
   private final Map<String, Map<String, Object>> behaviors = new LinkedHashMap();
   private final transient Map<BoneBehaviorType<?>, BoneBehaviorType.CachedProvider<?>> cachedBehaviorProvider = new LinkedHashMap();
   private String name;
   private boolean isRenderer;
   private int scale = 1;
   private int dataId;
   private Vector3f localPosition;
   private Vector3f localRotation;
   private Quaternionf localQuaternion = new Quaternionf();
   private Vector3f globalPosition;
   private Vector3f rotatedGlobalPosition;
   private Vector3f globalRotation;
   private Quaternionf globalQuaternion = new Quaternionf();
   private BlueprintBone parent;
   private Vector3f modelScale = new Vector3f(1.0F);
   private BlueprintBone dupeTarget;
   private boolean renderByDefault = true;

   public Map<String, BlueprintBone> getChildren() {
      return this.children;
   }

   public Map<String, Map<String, Object>> getBehaviors() {
      return this.behaviors;
   }

   public Map<BoneBehaviorType<?>, BoneBehaviorType.CachedProvider<?>> getCachedBehaviorProvider() {
      return this.cachedBehaviorProvider;
   }

   public String getName() {
      return this.name;
   }

   public boolean isRenderer() {
      return this.isRenderer;
   }

   public int getScale() {
      return this.scale;
   }

   public int getDataId() {
      return this.dataId;
   }

   public Vector3f getLocalPosition() {
      return this.localPosition;
   }

   public Vector3f getLocalRotation() {
      return this.localRotation;
   }

   public Quaternionf getLocalQuaternion() {
      return this.localQuaternion;
   }

   public Vector3f getGlobalPosition() {
      return this.globalPosition;
   }

   public Vector3f getRotatedGlobalPosition() {
      return this.rotatedGlobalPosition;
   }

   public Vector3f getGlobalRotation() {
      return this.globalRotation;
   }

   public Quaternionf getGlobalQuaternion() {
      return this.globalQuaternion;
   }

   public BlueprintBone getParent() {
      return this.parent;
   }

   public Vector3f getModelScale() {
      return this.modelScale;
   }

   public BlueprintBone getDupeTarget() {
      return this.dupeTarget;
   }

   public boolean isRenderByDefault() {
      return this.renderByDefault;
   }

   public void setName(String name) {
      this.name = name;
   }

   public void setRenderer(boolean isRenderer) {
      this.isRenderer = isRenderer;
   }

   public void setScale(int scale) {
      this.scale = scale;
   }

   public void setDataId(int dataId) {
      this.dataId = dataId;
   }

   public void setLocalPosition(Vector3f localPosition) {
      this.localPosition = localPosition;
   }

   public void setLocalRotation(Vector3f localRotation) {
      this.localRotation = localRotation;
   }

   public void setLocalQuaternion(Quaternionf localQuaternion) {
      this.localQuaternion = localQuaternion;
   }

   public void setGlobalPosition(Vector3f globalPosition) {
      this.globalPosition = globalPosition;
   }

   public void setRotatedGlobalPosition(Vector3f rotatedGlobalPosition) {
      this.rotatedGlobalPosition = rotatedGlobalPosition;
   }

   public void setGlobalRotation(Vector3f globalRotation) {
      this.globalRotation = globalRotation;
   }

   public void setGlobalQuaternion(Quaternionf globalQuaternion) {
      this.globalQuaternion = globalQuaternion;
   }

   public void setParent(BlueprintBone parent) {
      this.parent = parent;
   }

   public void setModelScale(Vector3f modelScale) {
      this.modelScale = modelScale;
   }

   public void setDupeTarget(BlueprintBone dupeTarget) {
      this.dupeTarget = dupeTarget;
   }

   public void setRenderByDefault(boolean renderByDefault) {
      this.renderByDefault = renderByDefault;
   }
}
