package com.ticxo.modelengine.core.generator.parser.blockbench.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.ticxo.modelengine.api.animation.BlueprintAnimation;
import com.ticxo.modelengine.api.model.bone.BoneBehaviorTypes;
import com.ticxo.modelengine.api.model.bone.type.PlayerLimb;
import com.ticxo.modelengine.api.utils.TFile;
import com.ticxo.modelengine.api.utils.config.ConfigProperty;
import com.ticxo.modelengine.api.utils.data.GSONUtils;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.bukkit.entity.ItemDisplay.ItemDisplayTransform;
import org.jetbrains.annotations.Nullable;

public class BlockbenchDeserializer<T extends BlockbenchModel> implements JsonDeserializer<T> {
   private final Map<UUID, BlockbenchModel.Bone> boneCache = new HashMap();
   private final Map<UUID, BlockbenchModel.Texture> textureCache = new HashMap();
   private final Supplier<T> supplier;
   private final boolean readOptions;

   public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
      T model = (BlockbenchModel)this.supplier.get();
      JsonObject root = json.getAsJsonObject();
      String namespace = ConfigProperty.NAMESPACE.getString().toLowerCase(Locale.ENGLISH);
      GSONUtils.ifPresent(root, "resolution", (element) -> {
         GSONUtils.ifPresent(element, "width", (width) -> {
            model.resolution[0] = width.getAsInt();
         });
         GSONUtils.ifPresent(element, "height", (height) -> {
            model.resolution[1] = height.getAsInt();
         });
      });
      GSONUtils.ifArray(root, "elements", (Consumer)((element) -> {
         String type = (String)GSONUtils.get(element, "type", JsonElement::getAsString, "cube");
         byte var5 = -1;
         switch(type.hashCode()) {
         case 3064885:
            if (type.equals("cube")) {
               var5 = 0;
            }
            break;
         case 1680073975:
            if (type.equals("null_object")) {
               var5 = 1;
            }
         }

         Object bbElement;
         switch(var5) {
         case 0:
            BlockbenchModel.Cube cube = new BlockbenchModel.Cube();
            GSONUtils.ifPresent(element, "from", (from) -> {
               JsonArray array = from.getAsJsonArray();
               cube.from[0] = array.get(0).getAsFloat();
               cube.from[1] = array.get(1).getAsFloat();
               cube.from[2] = array.get(2).getAsFloat();
            });
            GSONUtils.ifPresent(element, "to", (to) -> {
               JsonArray array = to.getAsJsonArray();
               cube.to[0] = array.get(0).getAsFloat();
               cube.to[1] = array.get(1).getAsFloat();
               cube.to[2] = array.get(2).getAsFloat();
            });
            GSONUtils.ifPresent(element, "rotation", (rotation) -> {
               JsonArray array = rotation.getAsJsonArray();
               cube.rotation[0] = array.get(0).getAsFloat();
               cube.rotation[1] = array.get(1).getAsFloat();
               cube.rotation[2] = array.get(2).getAsFloat();
            });
            cube.inflate = (Float)GSONUtils.get(element, "inflate", JsonElement::getAsFloat, 0.0F);
            GSONUtils.ifPresent(element, "origin", (origin) -> {
               JsonArray array = origin.getAsJsonArray();
               cube.origin[0] = array.get(0).getAsFloat();
               cube.origin[1] = array.get(1).getAsFloat();
               cube.origin[2] = array.get(2).getAsFloat();
            });
            GSONUtils.ifPresent(element, "faces", (e) -> {
               JsonObject faces = e.getAsJsonObject();
               Iterator var3 = faces.keySet().iterator();

               while(var3.hasNext()) {
                  String dir = (String)var3.next();
                  BlockbenchModel.Cube.Face face = new BlockbenchModel.Cube.Face();
                  JsonObject faceObject = faces.getAsJsonObject(dir);
                  GSONUtils.ifPresent(faceObject, "uv", (uv) -> {
                     JsonArray array = uv.getAsJsonArray();
                     face.uv[0] = array.get(0).getAsFloat();
                     face.uv[1] = array.get(1).getAsFloat();
                     face.uv[2] = array.get(2).getAsFloat();
                     face.uv[3] = array.get(3).getAsFloat();
                  });
                  face.rotation = (Integer)GSONUtils.get(faceObject, "rotation", JsonElement::getAsInt, 0);
                  face.texture = (Integer)GSONUtils.get(faceObject, "texture", JsonElement::getAsInt, -1);
                  cube.faces.put(dir, face);
               }

            });
            bbElement = cube;
            break;
         case 1:
            BlockbenchModel.NullObject nullObject = new BlockbenchModel.NullObject();
            GSONUtils.ifPresent(element, "position", (position) -> {
               JsonArray array = position.getAsJsonArray();
               nullObject.position[0] = array.get(0).getAsFloat();
               nullObject.position[1] = array.get(1).getAsFloat();
               nullObject.position[2] = array.get(2).getAsFloat();
            });
            bbElement = nullObject;
            break;
         default:
            return;
         }

         ((BlockbenchModel.Element)bbElement).name = (String)GSONUtils.get(element, "name", JsonElement::getAsString, "");
         ((BlockbenchModel.Element)bbElement).uuid = (UUID)GSONUtils.get(element, "uuid", (ele) -> {
            return UUID.fromString(ele.getAsString());
         });
         ((BlockbenchModel.Element)bbElement).export = (Boolean)GSONUtils.get(element, "export", JsonElement::getAsBoolean, true);
         model.elements.put(((BlockbenchModel.Element)bbElement).uuid, bbElement);
      }));
      GSONUtils.ifPresent(root, "outliner", (outliner) -> {
         this.readBones(outliner, model.outliner, (Set)null);
      });
      GSONUtils.ifArray(root, "textures", (BiConsumer)((index, texture) -> {
         BlockbenchModel.Texture bbTexture = new BlockbenchModel.Texture();
         bbTexture.name = TFile.removeExtension(((String)GSONUtils.get(texture, "name", JsonElement::getAsString, "")).toLowerCase(Locale.ENGLISH));
         bbTexture.namespace = ((String)GSONUtils.get(texture, "namespace", (element) -> {
            String s = element.getAsString();
            return s != null && !s.isEmpty() ? s : null;
         }, namespace)).toLowerCase(Locale.ENGLISH);
         bbTexture.folder = bbTexture.namespace.equals(namespace) ? "entity" : ((String)GSONUtils.get(texture, "folder", JsonElement::getAsString, "entity")).toLowerCase(Locale.ENGLISH);
         bbTexture.id = (String)GSONUtils.get(texture, "id", JsonElement::getAsString, "");
         bbTexture.frame_time = (Integer)GSONUtils.get(texture, "frame_time", JsonElement::getAsInt, 1);
         bbTexture.frame_order = (int[])GSONUtils.get(texture, "frame_order", (element) -> {
            String orderString = element.getAsString();
            if (orderString != null && !orderString.isBlank()) {
               String[] order = orderString.split(" ");
               int[] intOrder = new int[order.length];

               for(int i = 0; i < order.length; ++i) {
                  intOrder[i] = Integer.parseInt(order[i]);
               }

               return intOrder;
            } else {
               return new int[0];
            }
         });
         bbTexture.frame_interpolate = (Boolean)GSONUtils.get(texture, "frame_interpolate", JsonElement::getAsBoolean, false);
         bbTexture.uuid = (UUID)GSONUtils.get(texture, "uuid", (jsonElement) -> {
            return UUID.fromString(jsonElement.getAsString());
         });
         bbTexture.source = (String)GSONUtils.get(texture, "source", JsonElement::getAsString);
         bbTexture.uvWidth = (Integer)GSONUtils.get(texture, "uv_width", JsonElement::getAsInt, model.resolution[0]);
         bbTexture.uvHeight = (Integer)GSONUtils.get(texture, "uv_height", JsonElement::getAsInt, model.resolution[1]);
         model.textures.put(index, bbTexture);
         this.textureCache.put(bbTexture.uuid, bbTexture);
      }));
      GSONUtils.ifPresent(root, "mcmetas", (element) -> {
         JsonObject elementData = element.getAsJsonObject();
         Iterator var3 = elementData.entrySet().iterator();

         while(var3.hasNext()) {
            Entry<String, JsonElement> entry = (Entry)var3.next();
            UUID uuid = UUID.fromString((String)entry.getKey());
            BlockbenchModel.Texture texture = (BlockbenchModel.Texture)this.textureCache.get(uuid);
            if (texture != null) {
               texture.raw_mcmeta = ((JsonElement)entry.getValue()).toString();
            }
         }

      });
      GSONUtils.ifArray(root, "animations", (Consumer)((animation) -> {
         BlockbenchModel.Animation bbAnimation = new BlockbenchModel.Animation();
         bbAnimation.name = ((String)GSONUtils.get(animation, "name", JsonElement::getAsString, "")).toLowerCase(Locale.ENGLISH);
         bbAnimation.loop = BlueprintAnimation.LoopMode.get((String)GSONUtils.get(animation, "loop", JsonElement::getAsString, "once"));
         bbAnimation.override = (Boolean)GSONUtils.get(animation, "override", JsonElement::getAsBoolean, false);
         bbAnimation.length = (Float)GSONUtils.get(animation, "length", JsonElement::getAsFloat, 0.0F);
         bbAnimation.effects = new BlockbenchModel.Animation.Animator();
         bbAnimation.effects.name = "effects";
         GSONUtils.ifPresent(animation, "animators", (animators) -> {
            JsonObject jsonAnimators = animators.getAsJsonObject();

            JsonObject jsonAnimator;
            BlockbenchModel.Animation.Animator bbAnimator;
            for(Iterator var4 = jsonAnimators.keySet().iterator(); var4.hasNext(); GSONUtils.ifArray(jsonAnimator, "keyframes", (Consumer)((keyframe) -> {
               JsonObject jsonKeyframe = keyframe.getAsJsonObject();
               BlockbenchModel.Animation.Animator.Keyframe bbKeyframe = new BlockbenchModel.Animation.Animator.Keyframe();
               bbKeyframe.channel = (String)GSONUtils.get(jsonKeyframe, "channel", JsonElement::getAsString, "unknown");
               bbKeyframe.time = (Float)GSONUtils.get(jsonKeyframe, "time", JsonElement::getAsFloat, 0.0F);
               bbKeyframe.interpolation = (String)GSONUtils.get(jsonKeyframe, "interpolation", JsonElement::getAsString, "linear");
               GSONUtils.ifPresent(jsonKeyframe, "bezier_left_time", (leftTime) -> {
                  JsonArray array = leftTime.getAsJsonArray();
                  bbKeyframe.bezierLeftTime[0] = array.get(0).getAsFloat();
                  bbKeyframe.bezierLeftTime[1] = array.get(1).getAsFloat();
                  bbKeyframe.bezierLeftTime[2] = array.get(2).getAsFloat();
               });
               GSONUtils.ifPresent(jsonKeyframe, "bezier_left_value", (leftValue) -> {
                  JsonArray array = leftValue.getAsJsonArray();
                  bbKeyframe.bezierLeftValue[0] = array.get(0).getAsFloat();
                  bbKeyframe.bezierLeftValue[1] = array.get(1).getAsFloat();
                  bbKeyframe.bezierLeftValue[2] = array.get(2).getAsFloat();
               });
               GSONUtils.ifPresent(jsonKeyframe, "bezier_right_time", (rightTime) -> {
                  JsonArray array = rightTime.getAsJsonArray();
                  bbKeyframe.bezierRightTime[0] = array.get(0).getAsFloat();
                  bbKeyframe.bezierRightTime[1] = array.get(1).getAsFloat();
                  bbKeyframe.bezierRightTime[2] = array.get(2).getAsFloat();
               });
               GSONUtils.ifPresent(jsonKeyframe, "bezier_right_value", (rightValue) -> {
                  JsonArray array = rightValue.getAsJsonArray();
                  bbKeyframe.bezierRightValue[0] = array.get(0).getAsFloat();
                  bbKeyframe.bezierRightValue[1] = array.get(1).getAsFloat();
                  bbKeyframe.bezierRightValue[2] = array.get(2).getAsFloat();
               });
               GSONUtils.ifArray(jsonKeyframe, "data_points", (Consumer)((data) -> {
                  JsonObject jsonData = data.getAsJsonObject();
                  HashMap<String, String> dataMap = new HashMap();
                  Iterator var4 = jsonData.keySet().iterator();

                  while(var4.hasNext()) {
                     String dataKey = (String)var4.next();
                     dataMap.put(dataKey, jsonData.get(dataKey).getAsString());
                  }

                  bbKeyframe.data.add(dataMap);
               }));
               ((Map)bbAnimator.channels.computeIfAbsent(bbKeyframe.channel, (s) -> {
                  return new HashMap();
               })).put(bbKeyframe.time, bbKeyframe);
            }))) {
               String key = (String)var4.next();
               jsonAnimator = jsonAnimators.getAsJsonObject(key);
               if (key.equals("effects")) {
                  bbAnimator = bbAnimation.effects;
               } else {
                  UUID uuid = UUID.fromString(key);
                  bbAnimator = (BlockbenchModel.Animation.Animator)bbAnimation.animators.computeIfAbsent(uuid, (id) -> {
                     BlockbenchModel.Animation.Animator animator = new BlockbenchModel.Animation.Animator();
                     BlockbenchModel.Bone bone = (BlockbenchModel.Bone)this.boneCache.get(id);
                     animator.name = bone == null ? "" : bone.name;
                     animator.uuid = id;
                     animator.globalRotation = (Boolean)GSONUtils.get(jsonAnimator, "rotation_global", JsonElement::getAsBoolean, false);
                     return animator;
                  });
               }
            }

         });
         model.animations.put(bbAnimation.name, bbAnimation);
      }));
      GSONUtils.ifPresent(root, "animation_variable_placeholders", (element) -> {
         model.animationPlaceholder = element.getAsString();
      });
      this.boneCache.clear();
      this.textureCache.clear();
      return model;
   }

   private void readBones(JsonElement element, Map<String, BlockbenchModel.Bone> boneMap, @Nullable Set<UUID> elementSet) {
      if (element instanceof JsonArray) {
         JsonArray array = (JsonArray)element;
         Iterator var5 = array.iterator();

         while(var5.hasNext()) {
            JsonElement entry = (JsonElement)var5.next();
            if (!entry.isJsonObject()) {
               if (elementSet != null) {
                  elementSet.add(UUID.fromString(entry.getAsString()));
               }
            } else {
               JsonObject jsonBone = entry.getAsJsonObject();
               BlockbenchModel.Bone bbBone = new BlockbenchModel.Bone();
               bbBone.name = ((String)GSONUtils.get(jsonBone, "name", JsonElement::getAsString, "")).toLowerCase(Locale.ENGLISH);
               GSONUtils.ifPresent(entry, "origin", (origin) -> {
                  JsonArray originArray = origin.getAsJsonArray();
                  bbBone.origin[0] = originArray.get(0).getAsFloat();
                  bbBone.origin[1] = originArray.get(1).getAsFloat();
                  bbBone.origin[2] = originArray.get(2).getAsFloat();
               });
               GSONUtils.ifPresent(entry, "rotation", (rotation) -> {
                  JsonArray rotationArray = rotation.getAsJsonArray();
                  bbBone.rotation[0] = rotationArray.get(0).getAsFloat();
                  bbBone.rotation[1] = rotationArray.get(1).getAsFloat();
                  bbBone.rotation[2] = rotationArray.get(2).getAsFloat();
               });
               bbBone.uuid = (UUID)GSONUtils.get(jsonBone, "uuid", (uuid) -> {
                  return UUID.fromString(uuid.getAsString());
               });
               bbBone.export = (Boolean)GSONUtils.get(jsonBone, "export", JsonElement::getAsBoolean, true);
               GSONUtils.ifPresent(jsonBone, "children", (children) -> {
                  this.readBones(children, bbBone.childBone, bbBone.element);
               });
               bbBone.name = this.readOptions(bbBone);
               boneMap.put(bbBone.name, bbBone);
               this.boneCache.put(bbBone.uuid, bbBone);
            }
         }

      }
   }

   private String readOptions(BlockbenchModel.Bone bone) {
      if (!this.readOptions) {
         return bone.name;
      } else {
         String name = bone.name;
         Map<String, Map<String, Object>> options = bone.options;
         if (name.startsWith("h_")) {
            name = name.substring(2);
            options.put(BoneBehaviorTypes.HEAD.getId(), new HashMap());
         } else if (name.startsWith("hi_")) {
            name = name.substring(3);
            options.put(BoneBehaviorTypes.HEAD.getId(), new HashMap<String, Object>() {
               {
                  this.put("inherited", true);
               }
            });
         }

         if (name.equals("mount")) {
            options.put(BoneBehaviorTypes.MOUNT.getId(), new HashMap<String, Object>() {
               {
                  this.put("driver", true);
               }
            });
            return name;
         } else if (name.startsWith("b_")) {
            options.put(BoneBehaviorTypes.SUB_HITBOX.getId(), new HashMap());
            return name;
         } else if (name.startsWith("ob_")) {
            options.put(BoneBehaviorTypes.SUB_HITBOX.getId(), new HashMap<String, Object>() {
               {
                  this.put("obb", true);
               }
            });
            return name;
         } else {
            String[] split = name.split("_");
            int i;
            String tag;
            byte var8;
            StringBuilder builder;
            int j;
            if (!bone.element.isEmpty()) {
               for(i = 0; i < split.length; ++i) {
                  tag = split[i];
                  var8 = -1;
                  switch(tag.hashCode()) {
                  case 113749:
                     if (tag.equals("seg")) {
                        var8 = 0;
                     }
                     break;
                  case 106463762:
                     if (tag.equals("pbody")) {
                        var8 = 4;
                     }
                     break;
                  case 106632784:
                     if (tag.equals("phead")) {
                        var8 = 1;
                     }
                     break;
                  case 106748640:
                     if (tag.equals("plarm")) {
                        var8 = 3;
                     }
                     break;
                  case 106758802:
                     if (tag.equals("plleg")) {
                        var8 = 6;
                     }
                     break;
                  case 106927386:
                     if (tag.equals("prarm")) {
                        var8 = 2;
                     }
                     break;
                  case 106937548:
                     if (tag.equals("prleg")) {
                        var8 = 5;
                     }
                  }

                  switch(var8) {
                  case 0:
                     options.put(BoneBehaviorTypes.SEGMENT.getId(), new HashMap());
                     break;
                  case 1:
                     options.put(BoneBehaviorTypes.PLAYER_LIMB.getId(), new HashMap<String, Object>() {
                        {
                           this.put("limb", PlayerLimb.Limb.HEAD);
                        }
                     });
                     break;
                  case 2:
                     options.put(BoneBehaviorTypes.PLAYER_LIMB.getId(), new HashMap<String, Object>() {
                        {
                           this.put("limb", PlayerLimb.Limb.RIGHT_ARM);
                        }
                     });
                     break;
                  case 3:
                     options.put(BoneBehaviorTypes.PLAYER_LIMB.getId(), new HashMap<String, Object>() {
                        {
                           this.put("limb", PlayerLimb.Limb.LEFT_ARM);
                        }
                     });
                     break;
                  case 4:
                     options.put(BoneBehaviorTypes.PLAYER_LIMB.getId(), new HashMap<String, Object>() {
                        {
                           this.put("limb", PlayerLimb.Limb.BODY);
                        }
                     });
                     break;
                  case 5:
                     options.put(BoneBehaviorTypes.PLAYER_LIMB.getId(), new HashMap<String, Object>() {
                        {
                           this.put("limb", PlayerLimb.Limb.RIGHT_LEG);
                        }
                     });
                     break;
                  case 6:
                     options.put(BoneBehaviorTypes.PLAYER_LIMB.getId(), new HashMap<String, Object>() {
                        {
                           this.put("limb", PlayerLimb.Limb.LEFT_LEG);
                        }
                     });
                     break;
                  default:
                     builder = new StringBuilder(tag);

                     for(j = i + 1; j < split.length; ++j) {
                        builder.append("_").append(split[j]);
                     }

                     return builder.toString();
                  }
               }

               return name;
            } else {
               for(i = 0; i < split.length; ++i) {
                  tag = split[i];
                  var8 = -1;
                  switch(tag.hashCode()) {
                  case 103:
                     if (tag.equals("g")) {
                        var8 = 0;
                     }
                     break;
                  case 108:
                     if (tag.equals("l")) {
                        var8 = 6;
                     }
                     break;
                  case 112:
                     if (tag.equals("p")) {
                        var8 = 2;
                     }
                     break;
                  case 3359:
                     if (tag.equals("ih")) {
                        var8 = 5;
                     }
                     break;
                  case 3363:
                     if (tag.equals("il")) {
                        var8 = 4;
                     }
                     break;
                  case 3369:
                     if (tag.equals("ir")) {
                        var8 = 3;
                     }
                     break;
                  case 114586:
                     if (tag.equals("tag")) {
                        var8 = 1;
                     }
                     break;
                  case 106463762:
                     if (tag.equals("pbody")) {
                        var8 = 10;
                     }
                     break;
                  case 106632784:
                     if (tag.equals("phead")) {
                        var8 = 7;
                     }
                     break;
                  case 106748640:
                     if (tag.equals("plarm")) {
                        var8 = 9;
                     }
                     break;
                  case 106758802:
                     if (tag.equals("plleg")) {
                        var8 = 12;
                     }
                     break;
                  case 106927386:
                     if (tag.equals("prarm")) {
                        var8 = 8;
                     }
                     break;
                  case 106937548:
                     if (tag.equals("prleg")) {
                        var8 = 11;
                     }
                  }

                  switch(var8) {
                  case 0:
                     options.put(BoneBehaviorTypes.GHOST.getId(), new HashMap());
                     break;
                  case 1:
                     options.put(BoneBehaviorTypes.NAMETAG.getId(), new HashMap());
                     break;
                  case 2:
                     options.put(BoneBehaviorTypes.MOUNT.getId(), new HashMap<String, Object>() {
                        {
                           this.put("driver", false);
                        }
                     });
                     break;
                  case 3:
                     options.put(BoneBehaviorTypes.ITEM.getId(), new HashMap<String, Object>() {
                        {
                           this.put("display", ItemDisplayTransform.THIRDPERSON_RIGHTHAND);
                        }
                     });
                     break;
                  case 4:
                     options.put(BoneBehaviorTypes.ITEM.getId(), new HashMap<String, Object>() {
                        {
                           this.put("display", ItemDisplayTransform.THIRDPERSON_LEFTHAND);
                        }
                     });
                     break;
                  case 5:
                     options.put(BoneBehaviorTypes.ITEM.getId(), new HashMap<String, Object>() {
                        {
                           this.put("display", ItemDisplayTransform.HEAD);
                        }
                     });
                     break;
                  case 6:
                     options.put(BoneBehaviorTypes.LEASH.getId(), new HashMap());
                     break;
                  case 7:
                     options.put(BoneBehaviorTypes.PLAYER_LIMB.getId(), new HashMap<String, Object>() {
                        {
                           this.put("limb", PlayerLimb.Limb.HEAD);
                        }
                     });
                     break;
                  case 8:
                     options.put(BoneBehaviorTypes.PLAYER_LIMB.getId(), new HashMap<String, Object>() {
                        {
                           this.put("limb", PlayerLimb.Limb.RIGHT_ARM);
                        }
                     });
                     break;
                  case 9:
                     options.put(BoneBehaviorTypes.PLAYER_LIMB.getId(), new HashMap<String, Object>() {
                        {
                           this.put("limb", PlayerLimb.Limb.LEFT_ARM);
                        }
                     });
                     break;
                  case 10:
                     options.put(BoneBehaviorTypes.PLAYER_LIMB.getId(), new HashMap<String, Object>() {
                        {
                           this.put("limb", PlayerLimb.Limb.BODY);
                        }
                     });
                     break;
                  case 11:
                     options.put(BoneBehaviorTypes.PLAYER_LIMB.getId(), new HashMap<String, Object>() {
                        {
                           this.put("limb", PlayerLimb.Limb.RIGHT_LEG);
                        }
                     });
                     break;
                  case 12:
                     options.put(BoneBehaviorTypes.PLAYER_LIMB.getId(), new HashMap<String, Object>() {
                        {
                           this.put("limb", PlayerLimb.Limb.LEFT_LEG);
                        }
                     });
                     break;
                  default:
                     builder = new StringBuilder(tag);

                     for(j = i + 1; j < split.length; ++j) {
                        builder.append("_").append(split[j]);
                     }

                     return builder.toString();
                  }
               }

               return name;
            }
         }
      }
   }

   public BlockbenchDeserializer(Supplier<T> supplier, boolean readOptions) {
      this.supplier = supplier;
      this.readOptions = readOptions;
   }
}
