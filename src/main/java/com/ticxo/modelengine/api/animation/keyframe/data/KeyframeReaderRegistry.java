package com.ticxo.modelengine.api.animation.keyframe.data;

import com.ticxo.modelengine.api.utils.logger.TLogger;
import com.ticxo.modelengine.api.utils.registry.TUnaryRegistry;
import java.util.Iterator;
import java.util.function.Function;

public class KeyframeReaderRegistry extends TUnaryRegistry<Function<String, IKeyframeData>> {
   public IKeyframeData tryParse(String data) {
      if (data == null) {
         return IKeyframeData.EMPTY;
      } else {
         data = data.trim();
         if (data.isEmpty()) {
            return IKeyframeData.EMPTY;
         } else {
            try {
               return new DoubleData(Double.parseDouble(data));
            } catch (NumberFormatException var10) {
               String[] value = data.split(":", 2);
               if (value.length == 1) {
                  Iterator var11 = this.registry.values().iterator();

                  while(var11.hasNext()) {
                     Function func = (Function)var11.next();

                     try {
                        return (IKeyframeData)func.apply(value[0]);
                     } catch (Throwable var8) {
                     }
                  }
               } else {
                  Function<String, IKeyframeData> func = (Function)this.get(value[0]);
                  if (func != null) {
                     try {
                        return (IKeyframeData)func.apply(value[1]);
                     } catch (Throwable var7) {
                        TLogger.error(2, "------An error occurred while parsing the keyframe: " + data);
                        var7.printStackTrace();
                     }
                  } else {
                     Iterator var4 = this.registry.values().iterator();

                     while(var4.hasNext()) {
                        Function func2 = (Function)var4.next();

                        try {
                           return (IKeyframeData)func2.apply(data);
                        } catch (Throwable var9) {
                        }
                     }
                  }
               }

               TLogger.warn(2, "------Unknown keyframe data: " + data);
               return IKeyframeData.EMPTY;
            }
         }
      }
   }
}
