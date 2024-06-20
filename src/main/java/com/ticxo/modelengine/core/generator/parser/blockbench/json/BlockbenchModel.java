package com.ticxo.modelengine.core.generator.parser.blockbench.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.animation.BlueprintAnimation;
import com.ticxo.modelengine.api.animation.Timeline;
import com.ticxo.modelengine.api.animation.keyframe.KeyframeType;
import com.ticxo.modelengine.api.animation.keyframe.KeyframeTypes;
import com.ticxo.modelengine.api.animation.keyframe.data.KeyframeReaderRegistry;
import com.ticxo.modelengine.api.animation.keyframe.type.ScriptKeyframe;
import com.ticxo.modelengine.api.animation.keyframe.type.VectorKeyframe;
import com.ticxo.modelengine.api.entity.Hitbox;
import com.ticxo.modelengine.api.error.IError;
import com.ticxo.modelengine.api.error.WarnBadAngle;
import com.ticxo.modelengine.api.error.WarnMultipleAngle;
import com.ticxo.modelengine.api.error.WarnNoHitbox;
import com.ticxo.modelengine.api.generator.assets.BlueprintTexture;
import com.ticxo.modelengine.api.generator.assets.JavaItemModel;
import com.ticxo.modelengine.api.generator.assets.ModelAssets;
import com.ticxo.modelengine.api.generator.blueprint.BlueprintBone;
import com.ticxo.modelengine.api.generator.blueprint.ModelBlueprint;
import com.ticxo.modelengine.api.model.bone.BoneBehaviorTypes;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehaviorType;
import com.ticxo.modelengine.api.utils.data.ResourceLocation;
import com.ticxo.modelengine.api.utils.math.TMath;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Predicate;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class BlockbenchModel {
   private static final Gson gson = (new GsonBuilder()).registerTypeAdapter(BlueprintTexture.MCMeta.class, new MCMetaDeserializer()).create();
   protected final int[] resolution = new int[2];
   protected final Map<UUID, BlockbenchModel.Element> elements = new HashMap();
   protected final Map<String, BlockbenchModel.Bone> outliner = new HashMap();
   protected final Map<Integer, BlockbenchModel.Texture> textures = new HashMap();
   protected final Map<String, BlockbenchModel.Animation> animations = new LinkedHashMap();
   protected final transient Map<String, BlockbenchModel.Bone> flatOutliner = new HashMap();
   protected String animationPlaceholder = "";

   protected void finalizeOptions(BlockbenchModel.Bone bone, BlueprintBone blueprintBone) {
      Map<String, Object> subHitboxOptions = (Map)bone.getOptions().get(BoneBehaviorTypes.SUB_HITBOX.getId());
      if (subHitboxOptions != null) {
         Iterator var4 = bone.element.iterator();

         while(var4.hasNext()) {
            UUID uuid = (UUID)var4.next();
            BlockbenchModel.Element element = (BlockbenchModel.Element)this.elements.get(uuid);
            if (element instanceof BlockbenchModel.Cube) {
               BlockbenchModel.Cube cube = (BlockbenchModel.Cube)element;
               subHitboxOptions.put("dimension", new Hitbox((double)cube.width() * 0.0625D, (double)cube.height() * 0.0625D, (double)cube.depth() * 0.0625D, 0.0D));
               subHitboxOptions.put("origin", new Vector3f((cube.from[0] + cube.to[0]) * 0.03125F, (cube.from[1] + cube.to[1]) * 0.03125F, (cube.from[2] + cube.to[2]) * 0.03125F));
               break;
            }
         }

      }
   }

   protected BlueprintBone readBone(@Nullable BlueprintBone parent, BlockbenchModel.Bone bone) {
      this.flatOutliner.put(bone.name, bone);
      BlueprintBone blueprintBone = new BlueprintBone();
      this.finalizeOptions(bone, blueprintBone);
      blueprintBone.setName(bone.getName());
      blueprintBone.setGlobalPosition(new Vector3f(bone.getOrigin()[0] * 0.0625F, bone.getOrigin()[1] * 0.0625F, bone.getOrigin()[2] * 0.0625F));
      Vector3f rotation = new Vector3f(bone.getRotation()[0] * 0.017453292F, bone.getRotation()[1] * 0.017453292F, bone.getRotation()[2] * 0.017453292F);
      blueprintBone.setLocalRotation(rotation);
      blueprintBone.getLocalQuaternion().rotateZYX(rotation.z, rotation.y, rotation.x);
      if (parent != null) {
         blueprintBone.setLocalPosition(blueprintBone.getGlobalPosition().sub(parent.getGlobalPosition(), new Vector3f()));
         Quaternionf boneQuaternion = (new Quaternionf()).rotationZYX(rotation.z, rotation.y, rotation.x);
         Quaternionf parentQuaternion = parent.getGlobalQuaternion();
         parentQuaternion.mul(boneQuaternion, boneQuaternion);
         Vector3f global = TMath.getEulerAnglesZYX(boneQuaternion, new Vector3f());
         blueprintBone.setGlobalRotation(new Vector3f(global.x, global.y, global.z));
         blueprintBone.setGlobalQuaternion(boneQuaternion);
         Vector3f rotatedLocal = blueprintBone.getLocalPosition().rotate(parentQuaternion, new Vector3f());
         blueprintBone.setRotatedGlobalPosition(rotatedLocal.add(parent.getRotatedGlobalPosition()));
      } else {
         blueprintBone.setLocalPosition(new Vector3f(blueprintBone.getGlobalPosition()));
         blueprintBone.setGlobalRotation(rotation);
         blueprintBone.setGlobalQuaternion(new Quaternionf(blueprintBone.getLocalQuaternion()));
         blueprintBone.setRotatedGlobalPosition(new Vector3f(blueprintBone.getGlobalPosition()));
      }

      blueprintBone.setParent(parent);
      blueprintBone.getBehaviors().putAll(bone.options);
      Iterator var9 = bone.getChildBone().entrySet().iterator();

      while(var9.hasNext()) {
         Entry<String, BlockbenchModel.Bone> entry = (Entry)var9.next();
         blueprintBone.getChildren().put((String)entry.getKey(), this.readBone(blueprintBone, (BlockbenchModel.Bone)entry.getValue()));
      }

      return blueprintBone;
   }

   protected Map<String, BlockbenchModel.Bone> readBone(Map<String, BlockbenchModel.Bone> original, String name, Predicate<BlockbenchModel.Bone> consumer, Runnable missing) {
      HashMap<String, BlockbenchModel.Bone> localOutliner = new HashMap(original);
      LinkedList<Map<String, BlockbenchModel.Bone>> queue = new LinkedList();
      queue.add(localOutliner);

      while(!queue.isEmpty()) {
         Map<String, BlockbenchModel.Bone> bones = (Map)queue.pop();
         BlockbenchModel.Bone bone = (BlockbenchModel.Bone)bones.get(name);
         if (bone != null && consumer.test(bone)) {
            bones.remove(name);
            return localOutliner;
         }

         Iterator var9 = bones.values().iterator();

         while(var9.hasNext()) {
            BlockbenchModel.Bone childBones = (BlockbenchModel.Bone)var9.next();
            queue.add(childBones.getChildBone());
         }
      }

      missing.run();
      return localOutliner;
   }

   private Hitbox readHitbox(BlockbenchModel.Bone hitbox) {
      Iterator var2 = hitbox.getElement().iterator();

      BlockbenchModel.Element element;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         UUID uuid = (UUID)var2.next();
         element = (BlockbenchModel.Element)this.elements.get(uuid);
      } while(!(element instanceof BlockbenchModel.Cube));

      BlockbenchModel.Cube cube = (BlockbenchModel.Cube)element;
      float eyeHeight = hitbox.origin[1] <= 0.0F ? cube.origin[1] : hitbox.origin[1];
      if (eyeHeight <= 0.0F) {
         IError.BAD_EYE_HEIGHT.log();
      }

      return new Hitbox((double)(cube.width() * 0.0625F), (double)(cube.height() * 0.0625F), (double)(cube.depth() * 0.0625F), (double)(eyeHeight * 0.0625F));
   }

   private float readShadow(BlockbenchModel.Bone shadow) {
      Iterator var2 = shadow.getElement().iterator();

      BlockbenchModel.Element element;
      do {
         if (!var2.hasNext()) {
            return -1.0F;
         }

         UUID uuid = (UUID)var2.next();
         element = (BlockbenchModel.Element)this.elements.get(uuid);
      } while(!(element instanceof BlockbenchModel.Cube));

      BlockbenchModel.Cube cube = (BlockbenchModel.Cube)element;
      return Math.max(Math.abs(cube.to[0] - cube.from[0]), Math.abs(cube.to[2] - cube.from[2])) * 0.03125F;
   }

   public void populateBlueprint(ModelBlueprint blueprint) {
      Map var10001 = this.outliner;
      Predicate var10003 = (bone) -> {
         Hitbox mainHitbox = this.readHitbox(bone);
         if (mainHitbox == null) {
            return false;
         } else {
            blueprint.setMainHitbox(mainHitbox);
            return true;
         }
      };
      WarnNoHitbox var10004 = IError.NO_HITBOX;
      Objects.requireNonNull(var10004);
      Map<String, BlockbenchModel.Bone> localOutliner = this.readBone(var10001, "hitbox", var10003, var10004::log);
      localOutliner = this.readBone(localOutliner, "shadow", (bone) -> {
         float radius = this.readShadow(bone);
         if ((double)radius < 1.0E-5D) {
            return false;
         } else {
            blueprint.setShadowRadius(radius);
            return true;
         }
      }, () -> {
      });
      Iterator var3 = localOutliner.entrySet().iterator();

      Entry entry;
      while(var3.hasNext()) {
         entry = (Entry)var3.next();
         blueprint.getBones().put((String)entry.getKey(), this.readBone((BlueprintBone)null, (BlockbenchModel.Bone)entry.getValue()));
      }

      var3 = this.animations.entrySet().iterator();

      while(var3.hasNext()) {
         entry = (Entry)var3.next();
         String name = (String)entry.getKey();
         BlockbenchModel.Animation value = (BlockbenchModel.Animation)entry.getValue();
         BlueprintAnimation blueprintAnimation = new BlueprintAnimation(blueprint, name);
         Map<String, Map<Float, BlockbenchModel.Animation.Animator.Keyframe>> effectChannels = value.effects.getChannels();
         Map<Float, BlockbenchModel.Animation.Animator.Keyframe> script = (Map)effectChannels.get("timeline");
         Iterator var10;
         Entry timelineEntry;
         if (script != null) {
            var10 = script.entrySet().iterator();

            while(var10.hasNext()) {
               timelineEntry = (Entry)var10.next();
               BlockbenchModel.Animation.Animator.Keyframe bbFrame = (BlockbenchModel.Animation.Animator.Keyframe)timelineEntry.getValue();
               ScriptKeyframe frame = (ScriptKeyframe)blueprintAnimation.getGlobalTimeline().getKeyframe((Float)timelineEntry.getKey(), KeyframeTypes.SCRIPT);
               Iterator var14 = bbFrame.getData().iterator();

               while(var14.hasNext()) {
                  Map<String, String> data = (Map)var14.next();
                  String instructions = (String)data.getOrDefault("script", "");
                  String[] var17 = instructions.split("\n");
                  int var18 = var17.length;

                  for(int var19 = 0; var19 < var18; ++var19) {
                     String instruction = var17[var19];
                     frame.getScript().add(ScriptKeyframe.Script.from(instruction));
                  }
               }
            }
         }

         var10 = value.animators.entrySet().iterator();

         while(var10.hasNext()) {
            timelineEntry = (Entry)var10.next();
            BlockbenchModel.Animation.Animator animator = (BlockbenchModel.Animation.Animator)timelineEntry.getValue();
            Timeline timeline = new Timeline(blueprintAnimation, animator.globalRotation);
            putVectorKeyframes(animator, "position", timeline, KeyframeTypes.POSITION, -0.0625F, 0.0625F, 0.0625F);
            putVectorKeyframes(animator, "rotation", timeline, KeyframeTypes.ROTATION, -0.017453292F, -0.017453292F, 0.017453292F);
            putVectorKeyframes(animator, "scale", timeline, KeyframeTypes.SCALE, 1.0F, 1.0F, 1.0F);
            blueprintAnimation.getTimelines().put(animator.name, timeline);
         }

         blueprintAnimation.setLength((double)value.getLength());
         blueprintAnimation.setLoopMode(value.getLoop());
         blueprintAnimation.setOverride(value.isOverride());
         blueprint.getAnimations().put(name, blueprintAnimation);
      }

      String[] lines = this.animationPlaceholder.split("\n");
      String[] var22 = lines;
      int var23 = lines.length;

      for(int var24 = 0; var24 < var23; ++var24) {
         String line = var22[var24];
         String[] pair = line.split("=", 2);
         if (pair.length >= 2) {
            blueprint.getAnimationsPlaceholders().put(pair[0], pair[1]);
         }
      }

   }

   public void populateAssets(ModelBlueprint blueprint, ModelAssets assets) {
      Iterator var3 = this.textures.entrySet().iterator();

      Entry entry;
      while(var3.hasNext()) {
         entry = (Entry)var3.next();
         Integer id = (Integer)entry.getKey();
         BlockbenchModel.Texture bbTexture = (BlockbenchModel.Texture)entry.getValue();
         BlueprintTexture.MCMeta meta;
         if (bbTexture.raw_mcmeta == null) {
            meta = new BlueprintTexture.MCMeta();
            meta.setFrametime(bbTexture.frame_time);
            meta.setInterpolate(bbTexture.frame_interpolate ? true : null);
            if (bbTexture.frame_order != null) {
               int[] var8 = bbTexture.frame_order;
               int var9 = var8.length;

               for(int var10 = 0; var10 < var9; ++var10) {
                  int frame = var8[var10];
                  meta.addFrame(frame);
               }
            }
         } else {
            meta = (BlueprintTexture.MCMeta)gson.fromJson(bbTexture.raw_mcmeta, BlueprintTexture.MCMeta.class);
            meta.setMustGenerate(true);
         }

         BlueprintTexture texture = new BlueprintTexture();
         texture.setId(id);
         texture.setFrameWidth(bbTexture.uvWidth);
         texture.setFrameHeight(bbTexture.uvHeight);
         texture.setPath(new ResourceLocation(bbTexture.namespace, bbTexture.folder + "/" + bbTexture.name));
         texture.setMcMeta(meta);
         texture.setSource(bbTexture.source);
         assets.getTextures().add(texture);
      }

      var3 = blueprint.getFlatMap().entrySet().iterator();

      while(true) {
         label75:
         while(true) {
            String name;
            BlueprintBone bone;
            BlockbenchModel.Bone bbBone;
            do {
               do {
                  do {
                     if (!var3.hasNext()) {
                        return;
                     }

                     entry = (Entry)var3.next();
                     name = (String)entry.getKey();
                     bone = (BlueprintBone)entry.getValue();
                     bbBone = (BlockbenchModel.Bone)this.flatOutliner.get(name);
                  } while(bbBone == null);
               } while(!bbBone.export);
            } while(!this.shouldGenerate(bbBone));

            Iterator var25 = bone.getCachedBehaviorProvider().keySet().iterator();

            while(var25.hasNext()) {
               BoneBehaviorType<?> behaviorType = (BoneBehaviorType)var25.next();
               if (behaviorType.isIgnoreCubes()) {
                  continue label75;
               }
            }

            boolean hasElement = false;
            JavaItemModel javaItemModel = new JavaItemModel();
            javaItemModel.setName(name);
            Iterator var29 = bbBone.element.iterator();

            while(true) {
               BlockbenchModel.Element bbElement;
               do {
                  if (!var29.hasNext()) {
                     if (hasElement) {
                        bone.setRenderer(true);
                        bone.setScale(javaItemModel.scaleToFit());
                        assets.getModels().put(name, javaItemModel);
                     }
                     continue label75;
                  }

                  UUID elementId = (UUID)var29.next();
                  bbElement = (BlockbenchModel.Element)this.elements.get(elementId);
               } while(!(bbElement instanceof BlockbenchModel.Cube));

               BlockbenchModel.Cube cube = (BlockbenchModel.Cube)bbElement;
               JavaItemModel.JavaElement element = new JavaItemModel.JavaElement();
               element.from(bbBone.origin, cube.from, cube.inflate);
               element.to(bbBone.origin, cube.to, cube.inflate);
               element.setRotation(cube.rotation(bbBone));
               Iterator var15 = cube.faces.entrySet().iterator();

               while(var15.hasNext()) {
                  Entry<String, BlockbenchModel.Cube.Face> faceEntry = (Entry)var15.next();
                  String dir = (String)faceEntry.getKey();
                  BlockbenchModel.Cube.Face bbFace = (BlockbenchModel.Cube.Face)faceEntry.getValue();
                  if (!bbFace.isEmpty()) {
                     JavaItemModel.JavaElement.Face face = new JavaItemModel.JavaElement.Face();
                     face.setRotation(bbFace.rotation);
                     if (assets.getTextures().size() > bbFace.texture) {
                        BlueprintTexture texture = (BlueprintTexture)assets.getTextures().get(bbFace.texture);
                        if (texture != null) {
                           face.uv(texture.getFrameWidth(), texture.getFrameHeight(), bbFace.uv);
                           face.setTexture("#" + texture.getId());
                           javaItemModel.getTextures().put(String.valueOf(texture.getId()), texture.getPath().toString());
                        }
                     } else {
                        face.uv(16, 16, bbFace.uv);
                     }

                     element.getFaces().put(dir, face);
                  }
               }

               if (!element.getFaces().isEmpty()) {
                  hasElement = true;
                  javaItemModel.addElement(element);
               }
            }
         }
      }
   }

   protected boolean shouldGenerate(BlockbenchModel.Bone bbBone) {
      return true;
   }

   private static void putVectorKeyframes(BlockbenchModel.Animation.Animator animator, String channel, Timeline timeline, KeyframeType<VectorKeyframe, Vector3f> keyframeType, float scaleX, float scaleY, float scaleZ) {
      Map<Float, BlockbenchModel.Animation.Animator.Keyframe> frames = (Map)animator.channels.get(channel);
      if (frames != null) {
         Iterator var8 = frames.entrySet().iterator();

         while(var8.hasNext()) {
            Entry<Float, BlockbenchModel.Animation.Animator.Keyframe> entry = (Entry)var8.next();
            Float time = (Float)entry.getKey();
            BlockbenchModel.Animation.Animator.Keyframe keyframe = (BlockbenchModel.Animation.Animator.Keyframe)entry.getValue();
            VectorKeyframe vectorKeyframe = (VectorKeyframe)timeline.getKeyframe(time, keyframeType);
            if (keyframe.getData().size() >= 1) {
               KeyframeReaderRegistry reader = ModelEngineAPI.getAPI().getKeyframeReaderRegistry();
               Map<String, String> pre = (Map)keyframe.data.get(0);
               vectorKeyframe.setX(reader.tryParse((String)pre.getOrDefault("x", "0"))).setY(reader.tryParse((String)pre.getOrDefault("y", "0"))).setZ(reader.tryParse((String)pre.getOrDefault("z", "0"))).setXFactor(scaleX).setYFactor(scaleY).setZFactor(scaleZ);
               if (keyframe.getData().size() >= 2) {
                  Map<String, String> post = (Map)keyframe.data.get(1);
                  vectorKeyframe.setPostX(reader.tryParse((String)post.getOrDefault("x", "0"))).setPostY(reader.tryParse((String)post.getOrDefault("y", "0"))).setPostZ(reader.tryParse((String)post.getOrDefault("z", "0"))).setXFactor(scaleX).setYFactor(scaleY).setZFactor(scaleZ);
               }
            }

            vectorKeyframe.setInterpolation(keyframe.getInterpolation());
            if (vectorKeyframe.isBezier()) {
               vectorKeyframe.setBezierLeftTime(keyframe.bezierLeftTime[0], keyframe.bezierLeftTime[1], keyframe.bezierLeftTime[2]);
               vectorKeyframe.setBezierLeftValue(keyframe.bezierLeftValue[0], keyframe.bezierLeftValue[1], keyframe.bezierLeftValue[2]);
               vectorKeyframe.setBezierRightTime(keyframe.bezierRightTime[0], keyframe.bezierRightTime[1], keyframe.bezierRightTime[2]);
               vectorKeyframe.setBezierRightValue(keyframe.bezierRightValue[0], keyframe.bezierRightValue[1], keyframe.bezierRightValue[2]);
            }
         }

      }
   }

   public int[] getResolution() {
      return this.resolution;
   }

   public Map<UUID, BlockbenchModel.Element> getElements() {
      return this.elements;
   }

   public Map<String, BlockbenchModel.Bone> getOutliner() {
      return this.outliner;
   }

   public Map<Integer, BlockbenchModel.Texture> getTextures() {
      return this.textures;
   }

   public Map<String, BlockbenchModel.Animation> getAnimations() {
      return this.animations;
   }

   public Map<String, BlockbenchModel.Bone> getFlatOutliner() {
      return this.flatOutliner;
   }

   public String getAnimationPlaceholder() {
      return this.animationPlaceholder;
   }

   public static class Bone {
      protected final float[] origin = new float[3];
      protected final float[] rotation = new float[3];
      protected final Set<UUID> element = new HashSet();
      protected final Map<String, BlockbenchModel.Bone> childBone = new HashMap();
      protected final Map<String, Map<String, Object>> options = new HashMap();
      protected String name;
      protected UUID uuid;
      protected boolean export = true;

      public float[] getOrigin() {
         return this.origin;
      }

      public float[] getRotation() {
         return this.rotation;
      }

      public Set<UUID> getElement() {
         return this.element;
      }

      public Map<String, BlockbenchModel.Bone> getChildBone() {
         return this.childBone;
      }

      public Map<String, Map<String, Object>> getOptions() {
         return this.options;
      }

      public String getName() {
         return this.name;
      }

      public UUID getUuid() {
         return this.uuid;
      }

      public boolean isExport() {
         return this.export;
      }
   }

   public static class Element {
      protected String name;
      protected UUID uuid;
      protected boolean export = true;

      public String getName() {
         return this.name;
      }

      public UUID getUuid() {
         return this.uuid;
      }

      public boolean isExport() {
         return this.export;
      }
   }

   public static class Cube extends BlockbenchModel.Element {
      private static final float ANGLE_FACTOR = 0.044444446F;
      protected final float[] from = new float[3];
      protected final float[] to = new float[3];
      protected final float[] rotation = new float[3];
      protected final float[] origin = new float[3];
      protected final Map<String, BlockbenchModel.Cube.Face> faces = new HashMap();
      protected float inflate;

      public float width() {
         return Math.abs(this.to[0] - this.from[0]);
      }

      public float height() {
         return Math.abs(this.to[1] - this.from[1]);
      }

      public float depth() {
         return Math.abs(this.to[2] - this.from[2]);
      }

      @Nullable
      public JavaItemModel.JavaElement.Rotation rotation(BlockbenchModel.Bone bone) {
         int zeros = 0;
         float[] var3 = this.rotation;
         int i = var3.length;

         for(int var5 = 0; var5 < i; ++var5) {
            float angle = var3[var5];
            zeros += angle == 0.0F ? 1 : 0;
         }

         if (zeros == 3) {
            return null;
         } else {
            if (zeros <= 1) {
               (new WarnMultipleAngle(bone.name, this.name)).log();
            }

            JavaItemModel.JavaElement.Rotation javaRotation = new JavaItemModel.JavaElement.Rotation();
            i = TMath.absMax(this.rotation[0], this.rotation[1], this.rotation[2]);
            String var10001;
            switch(i) {
            case 1:
               var10001 = "y";
               break;
            case 2:
               var10001 = "z";
               break;
            default:
               var10001 = "x";
            }

            javaRotation.setAxis(var10001);
            float angle = this.rotation[i];
            if (angle > 45.0F || angle < -45.0F) {
               (new WarnBadAngle(bone.name, this.name, (double)angle)).log();
            }

            angle = TMath.clamp(angle, -45.0F, 45.0F);
            if (angle % 22.5F != 0.0F) {
               (new WarnBadAngle(bone.name, this.name, (double)angle)).log();
            }

            angle = (float)Math.round(angle * 0.044444446F) * 22.5F;
            javaRotation.setAngle(angle);
            javaRotation.origin(bone.origin, this.origin);
            return javaRotation;
         }
      }

      public float[] getFrom() {
         return this.from;
      }

      public float[] getTo() {
         return this.to;
      }

      public float[] getRotation() {
         return this.rotation;
      }

      public float[] getOrigin() {
         return this.origin;
      }

      public Map<String, BlockbenchModel.Cube.Face> getFaces() {
         return this.faces;
      }

      public float getInflate() {
         return this.inflate;
      }

      public static class Face {
         protected final float[] uv = new float[4];
         protected int rotation;
         protected int texture;

         public boolean isEmpty() {
            return this.texture == -1 || TMath.isSimilar(this.uv[0], this.uv[2]) || TMath.isSimilar(this.uv[1], this.uv[3]);
         }

         public float[] getUv() {
            return this.uv;
         }

         public int getRotation() {
            return this.rotation;
         }

         public int getTexture() {
            return this.texture;
         }
      }
   }

   public static class Animation {
      protected String name;
      protected BlueprintAnimation.LoopMode loop;
      protected boolean override;
      protected float length;
      protected BlockbenchModel.Animation.Animator effects;
      protected Map<UUID, BlockbenchModel.Animation.Animator> animators = new HashMap();

      public String getName() {
         return this.name;
      }

      public BlueprintAnimation.LoopMode getLoop() {
         return this.loop;
      }

      public boolean isOverride() {
         return this.override;
      }

      public float getLength() {
         return this.length;
      }

      public BlockbenchModel.Animation.Animator getEffects() {
         return this.effects;
      }

      public Map<UUID, BlockbenchModel.Animation.Animator> getAnimators() {
         return this.animators;
      }

      public static class Animator {
         protected String name;
         protected UUID uuid;
         protected boolean globalRotation;
         protected Map<String, Map<Float, BlockbenchModel.Animation.Animator.Keyframe>> channels = new HashMap();

         public String getName() {
            return this.name;
         }

         public UUID getUuid() {
            return this.uuid;
         }

         public boolean isGlobalRotation() {
            return this.globalRotation;
         }

         public Map<String, Map<Float, BlockbenchModel.Animation.Animator.Keyframe>> getChannels() {
            return this.channels;
         }

         public static class Keyframe {
            protected final List<Map<String, String>> data = new ArrayList();
            protected final float[] bezierLeftTime = new float[3];
            protected final float[] bezierLeftValue = new float[3];
            protected final float[] bezierRightTime = new float[3];
            protected final float[] bezierRightValue = new float[3];
            protected String channel;
            protected float time;
            protected String interpolation;

            public List<Map<String, String>> getData() {
               return this.data;
            }

            public float[] getBezierLeftTime() {
               return this.bezierLeftTime;
            }

            public float[] getBezierLeftValue() {
               return this.bezierLeftValue;
            }

            public float[] getBezierRightTime() {
               return this.bezierRightTime;
            }

            public float[] getBezierRightValue() {
               return this.bezierRightValue;
            }

            public String getChannel() {
               return this.channel;
            }

            public float getTime() {
               return this.time;
            }

            public String getInterpolation() {
               return this.interpolation;
            }
         }
      }
   }

   public static class Texture {
      protected String name;
      protected String folder;
      protected String namespace;
      protected String id;
      protected int frame_time;
      protected int[] frame_order;
      protected boolean frame_interpolate;
      protected UUID uuid;
      protected String raw_mcmeta;
      protected String source;
      protected int uvWidth;
      protected int uvHeight;

      public String getName() {
         return this.name;
      }

      public String getFolder() {
         return this.folder;
      }

      public String getNamespace() {
         return this.namespace;
      }

      public String getId() {
         return this.id;
      }

      public int getFrame_time() {
         return this.frame_time;
      }

      public int[] getFrame_order() {
         return this.frame_order;
      }

      public boolean isFrame_interpolate() {
         return this.frame_interpolate;
      }

      public UUID getUuid() {
         return this.uuid;
      }

      public String getRaw_mcmeta() {
         return this.raw_mcmeta;
      }

      public String getSource() {
         return this.source;
      }

      public int getUvWidth() {
         return this.uvWidth;
      }

      public int getUvHeight() {
         return this.uvHeight;
      }
   }

   public static class NullObject extends BlockbenchModel.Element {
      protected final float[] position = new float[3];

      public float[] getPosition() {
         return this.position;
      }
   }
}
