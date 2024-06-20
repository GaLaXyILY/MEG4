package com.ticxo.modelengine.api.utils;

import java.text.DecimalFormat;

public class MiscUtils {
   public static final DecimalFormat FORMATTER = new DecimalFormat() {
      {
         this.setMaximumFractionDigits(1);
         this.setMinimumFractionDigits(1);
      }
   };
}
