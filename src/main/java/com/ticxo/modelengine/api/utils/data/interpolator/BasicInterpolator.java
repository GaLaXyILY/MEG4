package com.ticxo.modelengine.api.utils.data.interpolator;

public class BasicInterpolator<IN> extends Interpolator<IN, IN> {
   public BasicInterpolator() {
      this.setParseFunc((value) -> {
         return value;
      });
   }
}
