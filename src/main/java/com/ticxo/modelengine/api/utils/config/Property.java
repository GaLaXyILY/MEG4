package com.ticxo.modelengine.api.utils.config;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.entity.CullType;
import com.ticxo.modelengine.api.generator.BaseItemEnum;
import java.util.Locale;

public interface Property {
   String getPath();

   Object getDef();

   default int getInt() {
      return ModelEngineAPI.getAPI().getConfigManager().getInt(this);
   }

   default double getDouble() {
      return ModelEngineAPI.getAPI().getConfigManager().getDouble(this);
   }

   default String getString() {
      return ModelEngineAPI.getAPI().getConfigManager().getString(this);
   }

   default boolean getBoolean() {
      return ModelEngineAPI.getAPI().getConfigManager().getBoolean(this);
   }

   default BaseItemEnum getBaseItem() {
      return BaseItemEnum.get(this.getString().toUpperCase(Locale.ENGLISH));
   }

   default CullType getCullType() {
      return CullType.get(this.getString().toUpperCase(Locale.ENGLISH));
   }
}
