package com.ticxo.modelengine.core.generator.parser.modelengine.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.ticxo.modelengine.api.utils.data.GSONUtils;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.UUID;
import java.util.function.Consumer;
import org.joml.Vector3f;

public class ModelEngineDeserializer implements JsonDeserializer<ModelEngineExtraData> {
   public ModelEngineExtraData deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
      ModelEngineExtraData data = new ModelEngineExtraData();
      GSONUtils.ifPresent(jsonElement, "modelEngineFormatMeta", (meta) -> {
         GSONUtils.ifPresent(meta, "boneProperties", (propertiesElement) -> {
            if (propertiesElement.isJsonObject()) {
               JsonObject properties = propertiesElement.getAsJsonObject();
               Iterator var3 = properties.keySet().iterator();

               while(var3.hasNext()) {
                  String id = (String)var3.next();
                  UUID uuid = UUID.fromString(id);
                  JsonObject dataObject = properties.getAsJsonObject(id);
                  ModelEngineExtraData.BoneProperty boneProperty = new ModelEngineExtraData.BoneProperty();
                  boneProperty.renderByDefault = (Boolean)GSONUtils.get(dataObject, "renderByDefault", JsonElement::getAsBoolean, true);
                  boneProperty.dupeTarget = (UUID)GSONUtils.get(dataObject, "copyModel", (copyId) -> {
                     return UUID.fromString(copyId.getAsString());
                  });
                  GSONUtils.ifPresent(dataObject, "copyScale", (from) -> {
                     JsonArray array = from.getAsJsonArray();
                     boneProperty.boneScale = new Vector3f(array.get(0).getAsFloat(), array.get(1).getAsFloat(), array.get(2).getAsFloat());
                  });
                  String propertyBehavior = (String)GSONUtils.get(dataObject, "boneBehavior", JsonElement::getAsString, "");
                  byte var10 = -1;
                  switch(propertyBehavior.hashCode()) {
                  case 3198432:
                     if (propertyBehavior.equals("head")) {
                        var10 = 0;
                     }
                     break;
                  case 1973722931:
                     if (propertyBehavior.equals("segment")) {
                        var10 = 1;
                     }
                  }

                  switch(var10) {
                  case 0:
                     boneProperty.behaviors.add(ModelEngineExtraData.Behavior.HEAD);
                     break;
                  case 1:
                     boneProperty.behaviors.add(ModelEngineExtraData.Behavior.SEGMENT);
                  }

                  String propertyType = (String)GSONUtils.get(dataObject, "type", JsonElement::getAsString, "bone");
                  byte var11 = -1;
                  switch(propertyType.hashCode()) {
                  case -1217012392:
                     if (propertyType.equals("hitbox")) {
                        var11 = 0;
                     }
                     break;
                  case 3526149:
                     if (propertyType.equals("seat")) {
                        var11 = 1;
                     }
                  }

                  Boolean driver;
                  switch(var11) {
                  case 0:
                     driver = (Boolean)GSONUtils.get(dataObject, "orientated", JsonElement::getAsBoolean, false);
                     boneProperty.behaviors.add(driver ? ModelEngineExtraData.Behavior.SUB_OBB : ModelEngineExtraData.Behavior.SUB_AABB);
                     break;
                  case 1:
                     driver = (Boolean)GSONUtils.get(dataObject, "driver", JsonElement::getAsBoolean, false);
                     boneProperty.behaviors.add(driver ? ModelEngineExtraData.Behavior.DRIVER : ModelEngineExtraData.Behavior.PASSENGER);
                     break;
                  default:
                     String boneOption = (String)GSONUtils.get(dataObject, "boneOptions", JsonElement::getAsString, "");
                     byte var14 = -1;
                     switch(boneOption.hashCode()) {
                     case -1957691651:
                        if (boneOption.equals("item_model")) {
                           var14 = 3;
                        }
                        break;
                     case 98331279:
                        if (boneOption.equals("ghost")) {
                           var14 = 0;
                        }
                        break;
                     case 102846045:
                        if (boneOption.equals("leash")) {
                           var14 = 1;
                        }
                        break;
                     case 1721972015:
                        if (boneOption.equals("nametag")) {
                           var14 = 2;
                        }
                     }

                     switch(var14) {
                     case 0:
                        boneProperty.behaviors.add(ModelEngineExtraData.Behavior.GHOST);
                        break;
                     case 1:
                        boneProperty.behaviors.add(ModelEngineExtraData.Behavior.LEASH);
                        break;
                     case 2:
                        boneProperty.behaviors.add(ModelEngineExtraData.Behavior.NAMETAG);
                        break;
                     case 3:
                        String itemPosition = (String)GSONUtils.get(dataObject, "itemPosition", JsonElement::getAsString, "head");
                        byte var17 = -1;
                        switch(itemPosition.hashCode()) {
                        case -1436108128:
                           if (itemPosition.equals("rightArm")) {
                              var17 = 1;
                           }
                           break;
                        case 3198432:
                           if (itemPosition.equals("head")) {
                              var17 = 0;
                           }
                           break;
                        case 55414997:
                           if (itemPosition.equals("leftArm")) {
                              var17 = 2;
                           }
                        }

                        switch(var17) {
                        case 0:
                           boneProperty.behaviors.add(ModelEngineExtraData.Behavior.ITEM_HEAD);
                           break;
                        case 1:
                           boneProperty.behaviors.add(ModelEngineExtraData.Behavior.ITEM_RIGHT);
                           break;
                        case 2:
                           boneProperty.behaviors.add(ModelEngineExtraData.Behavior.ITEM_LEFT);
                        }
                     }
                  }

                  GSONUtils.ifArray(dataObject, "customOptions", (Consumer)((custom) -> {
                     String customId = (String)GSONUtils.get(custom, "name", JsonElement::getAsString, "");
                     String customValue = (String)GSONUtils.get(custom, "value", JsonElement::getAsString, "");
                     Boolean customEnabled = (Boolean)GSONUtils.get(custom, "enabled", JsonElement::getAsBoolean, true);
                     ModelEngineExtraData.CustomOption customOption = new ModelEngineExtraData.CustomOption(customId, customValue, customEnabled);
                     boneProperty.getCustomOptions().add(customOption);
                  }));
                  data.getBoneProperties().put(uuid, boneProperty);
               }
            }

         });
      });
      return data;
   }
}
