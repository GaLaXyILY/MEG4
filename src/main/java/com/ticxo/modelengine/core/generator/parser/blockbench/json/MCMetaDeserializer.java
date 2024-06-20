package com.ticxo.modelengine.core.generator.parser.blockbench.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.ticxo.modelengine.api.generator.assets.BlueprintTexture;
import com.ticxo.modelengine.api.utils.data.GSONUtils;
import java.lang.reflect.Type;
import java.util.function.Consumer;

public class MCMetaDeserializer implements JsonDeserializer<BlueprintTexture.MCMeta> {
   public BlueprintTexture.MCMeta deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
      JsonObject animation = (JsonObject)GSONUtils.get(jsonElement, "animation", JsonElement::getAsJsonObject);
      if (animation == null) {
         return null;
      } else {
         BlueprintTexture.MCMeta meta = new BlueprintTexture.MCMeta();
         meta.setInterpolate((Boolean)GSONUtils.get(animation, "interpolate", JsonElement::getAsBoolean));
         meta.setWidth((Integer)GSONUtils.get(animation, "width", JsonElement::getAsInt));
         meta.setHeight((Integer)GSONUtils.get(animation, "height", JsonElement::getAsInt));
         meta.setFrametime((Integer)GSONUtils.get(animation, "frametime", JsonElement::getAsInt));
         GSONUtils.ifArray(animation, "frames", (Consumer)((element) -> {
            if (element.isJsonPrimitive()) {
               meta.addFrame(element.getAsInt());
            } else if (element.isJsonObject()) {
               JsonObject frameObject = element.getAsJsonObject();
               Integer index = (Integer)GSONUtils.get(frameObject, "index", JsonElement::getAsInt);
               Integer time = (Integer)GSONUtils.get(frameObject, "time", JsonElement::getAsInt);
               if (index != null && time != null) {
                  meta.addFrame(index, time);
               }
            }

         }));
         return meta;
      }
   }
}
