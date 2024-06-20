package com.ticxo.modelengine.core.generator.parser.modelengine.json;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.error.ErrorMissingBoneBehaviorData;
import com.ticxo.modelengine.api.error.ErrorUnknownBoneBehavior;
import com.ticxo.modelengine.api.model.bone.BoneBehaviorTypes;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehaviorRegistry;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehaviorType;
import com.ticxo.modelengine.api.model.bone.type.PlayerLimb;
import com.ticxo.modelengine.core.generator.parser.blockbench.json.BlockbenchModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Consumer;
import org.bukkit.entity.ItemDisplay.ItemDisplayTransform;
import org.joml.Vector3f;

public class ModelEngineExtraData {
   protected final Map<UUID, ModelEngineExtraData.BoneProperty> boneProperties = new HashMap();

   public Map<UUID, ModelEngineExtraData.BoneProperty> getBoneProperties() {
      return this.boneProperties;
   }

   public static class CustomOption {
      protected final String id;
      protected final String jsonValue;
      protected final boolean enabled;

      public void populate(BlockbenchModel.Bone bone) {
         if (this.enabled) {
            BoneBehaviorRegistry registry = ModelEngineAPI.getAPI().getBoneBehaviorRegistry();
            BoneBehaviorType<?> type = registry.getById(this.id);
            if (type == null) {
               (new ErrorUnknownBoneBehavior(this.id, bone.getName())).log();
            } else {
               HashMap<String, Object> map = new HashMap();
               Gson gson = registry.getGson();
               JsonObject jsonObject = (JsonObject)gson.fromJson(this.jsonValue, JsonObject.class);
               Iterator var7 = type.getRequiredArguments().entrySet().iterator();

               Entry entry;
               String key;
               JsonElement jsonData;
               Class dataType;
               Object value;
               while(var7.hasNext()) {
                  entry = (Entry)var7.next();
                  key = (String)entry.getKey();
                  if (!jsonObject.has(key)) {
                     (new ErrorMissingBoneBehaviorData(bone.getName(), type, key)).log();
                     return;
                  }

                  jsonData = jsonObject.get(key);
                  dataType = (Class)entry.getValue();
                  value = gson.fromJson(jsonData, dataType);
                  map.put(key, value);
               }

               var7 = type.getOptionalArguments().entrySet().iterator();

               while(var7.hasNext()) {
                  entry = (Entry)var7.next();
                  key = (String)entry.getKey();
                  if (jsonObject.has(key)) {
                     jsonData = jsonObject.get(key);
                     dataType = (Class)entry.getValue();
                     value = gson.fromJson(jsonData, dataType);
                     map.put(key, value);
                  }
               }

               bone.getOptions().put(this.id, map);
            }
         }
      }

      public String getId() {
         return this.id;
      }

      public String getJsonValue() {
         return this.jsonValue;
      }

      public boolean isEnabled() {
         return this.enabled;
      }

      public CustomOption(String id, String jsonValue, boolean enabled) {
         this.id = id;
         this.jsonValue = jsonValue;
         this.enabled = enabled;
      }
   }

   public static class BoneProperty {
      protected final Set<ModelEngineExtraData.Behavior> behaviors = new LinkedHashSet();
      protected final List<ModelEngineExtraData.CustomOption> customOptions = new ArrayList();
      protected UUID dupeTarget;
      protected Vector3f boneScale;
      protected boolean renderByDefault = true;

      public Set<ModelEngineExtraData.Behavior> getBehaviors() {
         return this.behaviors;
      }

      public List<ModelEngineExtraData.CustomOption> getCustomOptions() {
         return this.customOptions;
      }

      public UUID getDupeTarget() {
         return this.dupeTarget;
      }

      public Vector3f getBoneScale() {
         return this.boneScale;
      }

      public boolean isRenderByDefault() {
         return this.renderByDefault;
      }
   }

   public static enum Behavior {
      HEAD((options) -> {
         options.put(BoneBehaviorTypes.HEAD.getId(), new HashMap());
      }),
      SEGMENT((options) -> {
         options.put(BoneBehaviorTypes.SEGMENT.getId(), new HashMap());
      }),
      GHOST((options) -> {
         options.put(BoneBehaviorTypes.GHOST.getId(), new HashMap());
      }),
      LEASH((options) -> {
         options.put(BoneBehaviorTypes.LEASH.getId(), new HashMap());
      }),
      NAMETAG((options) -> {
         options.put(BoneBehaviorTypes.NAMETAG.getId(), new HashMap());
      }),
      ITEM_HEAD((options) -> {
         options.put(BoneBehaviorTypes.ITEM.getId(), new HashMap<String, Object>() {
            {
               this.put("display", ItemDisplayTransform.HEAD);
            }
         });
      }),
      ITEM_RIGHT((options) -> {
         options.put(BoneBehaviorTypes.ITEM.getId(), new HashMap<String, Object>() {
            {
               this.put("display", ItemDisplayTransform.THIRDPERSON_RIGHTHAND);
            }
         });
      }),
      ITEM_LEFT((options) -> {
         options.put(BoneBehaviorTypes.ITEM.getId(), new HashMap<String, Object>() {
            {
               this.put("display", ItemDisplayTransform.THIRDPERSON_LEFTHAND);
            }
         });
      }),
      SUB_AABB((options) -> {
         options.put(BoneBehaviorTypes.SUB_HITBOX.getId(), new HashMap());
      }),
      SUB_OBB((options) -> {
         options.put(BoneBehaviorTypes.SUB_HITBOX.getId(), new HashMap<String, Object>() {
            {
               this.put("obb", true);
            }
         });
      }),
      DRIVER((options) -> {
         options.put(BoneBehaviorTypes.MOUNT.getId(), new HashMap<String, Object>() {
            {
               this.put("driver", true);
            }
         });
      }),
      PASSENGER((options) -> {
         options.put(BoneBehaviorTypes.MOUNT.getId(), new HashMap<String, Object>() {
            {
               this.put("driver", false);
            }
         });
      }),
      PLAYER_HEAD((options) -> {
         options.put(BoneBehaviorTypes.PLAYER_LIMB.getId(), new HashMap<String, Object>() {
            {
               this.put("limb", PlayerLimb.Limb.HEAD);
            }
         });
      }),
      PLAYER_BODY((options) -> {
         options.put(BoneBehaviorTypes.PLAYER_LIMB.getId(), new HashMap<String, Object>() {
            {
               this.put("limb", PlayerLimb.Limb.BODY);
            }
         });
      }),
      PLAYER_RIGHT_ARM((options) -> {
         options.put(BoneBehaviorTypes.PLAYER_LIMB.getId(), new HashMap<String, Object>() {
            {
               this.put("limb", PlayerLimb.Limb.RIGHT_ARM);
            }
         });
      }),
      PLAYER_LEFT_ARM((options) -> {
         options.put(BoneBehaviorTypes.PLAYER_LIMB.getId(), new HashMap<String, Object>() {
            {
               this.put("limb", PlayerLimb.Limb.LEFT_ARM);
            }
         });
      }),
      PLAYER_RIGHT_LEG((options) -> {
         options.put(BoneBehaviorTypes.PLAYER_LIMB.getId(), new HashMap<String, Object>() {
            {
               this.put("limb", PlayerLimb.Limb.RIGHT_LEG);
            }
         });
      }),
      PLAYER_LEFT_LEG((options) -> {
         options.put(BoneBehaviorTypes.PLAYER_LIMB.getId(), new HashMap<String, Object>() {
            {
               this.put("limb", PlayerLimb.Limb.LEFT_LEG);
            }
         });
      });

      private final Consumer<Map<String, Map<String, Object>>> optionPopulate;

      public void populate(BlockbenchModel.Bone bone) {
         this.optionPopulate.accept(bone.getOptions());
      }

      private Behavior(Consumer<Map<String, Map<String, Object>>> optionPopulate) {
         this.optionPopulate = optionPopulate;
      }

      // $FF: synthetic method
      private static ModelEngineExtraData.Behavior[] $values() {
         return new ModelEngineExtraData.Behavior[]{HEAD, SEGMENT, GHOST, LEASH, NAMETAG, ITEM_HEAD, ITEM_RIGHT, ITEM_LEFT, SUB_AABB, SUB_OBB, DRIVER, PASSENGER, PLAYER_HEAD, PLAYER_BODY, PLAYER_RIGHT_ARM, PLAYER_LEFT_ARM, PLAYER_RIGHT_LEG, PLAYER_LEFT_LEG};
      }
   }
}
