package com.ticxo.modelengine.core.command;

import com.ticxo.modelengine.api.model.ActiveModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Color;

public final class ModelOptionParser {
   private Boolean doDamageTint;
   private Boolean lockPitch;
   private Boolean lockYaw;
   private Boolean showHitbox;
   private Boolean showShadow;
   private Double stepHeight;
   private Double scale;
   private Double hitboxScale;
   private Integer viewRadius;
   private Color color;
   public Boolean hideSelfDisguise = false;

   public static ModelOptionParser parse(int offset, String[] argss) {
      ModelOptionParser options = new ModelOptionParser();
      if (argss.length <= offset) {
         return options;
      } else {
         String[] args = (String[])Arrays.copyOfRange(argss, offset, argss.length);

         for(int i = 0; i < args.length; ++i) {
            String var5 = args[i].toLowerCase();
            byte var6 = -1;
            switch(var5.hashCode()) {
            case -1010217229:
               if (var5.equals("stepheight")) {
                  var6 = 5;
               }
               break;
            case -992585865:
               if (var5.equals("viewradius")) {
                  var6 = 8;
               }
               break;
            case -914587403:
               if (var5.equals("lockpitch")) {
                  var6 = 1;
               }
               break;
            case 94842723:
               if (var5.equals("color")) {
                  var6 = 10;
               }
               break;
            case 109250890:
               if (var5.equals("scale")) {
                  var6 = 6;
               }
               break;
            case 338721124:
               if (var5.equals("lockyaw")) {
                  var6 = 2;
               }
               break;
            case 651274677:
               if (var5.equals("dodamagetint")) {
                  var6 = 0;
               }
               break;
            case 945827469:
               if (var5.equals("hideselfdisguise")) {
                  var6 = 9;
               }
               break;
            case 1374209685:
               if (var5.equals("showhitbox")) {
                  var6 = 3;
               }
               break;
            case 1687642717:
               if (var5.equals("showshadow")) {
                  var6 = 4;
               }
               break;
            case 1700381426:
               if (var5.equals("hitboxscale")) {
                  var6 = 7;
               }
            }

            switch(var6) {
            case 0:
               ++i;
               options.doDamageTint = getNextBoolean(args, i);
               break;
            case 1:
               ++i;
               options.lockPitch = getNextBoolean(args, i);
               break;
            case 2:
               ++i;
               options.lockYaw = getNextBoolean(args, i);
               break;
            case 3:
               ++i;
               options.showHitbox = getNextBoolean(args, i);
               break;
            case 4:
               ++i;
               options.showShadow = getNextBoolean(args, i);
               break;
            case 5:
               ++i;
               options.stepHeight = getNextDouble(args, i);
               break;
            case 6:
               ++i;
               options.scale = getNextDouble(args, i);
               break;
            case 7:
               ++i;
               options.hitboxScale = getNextDouble(args, i);
               break;
            case 8:
               ++i;
               options.viewRadius = getNextInteger(args, i);
               break;
            case 9:
               ++i;
               options.hideSelfDisguise = getNextBoolean(args, i);
               break;
            case 10:
               ++i;
               String colorString = getNextString(args, i);
               if (colorString.startsWith("#")) {
                  colorString = colorString.substring(1);
               }

               options.color = Color.fromRGB(Integer.parseInt(colorString, 16));
            }
         }

         return options;
      }
   }

   public static List<String> getTabCompletion(int offset, String[] argss) {
      String[] args = (String[])Arrays.copyOfRange(argss, offset, argss.length);
      List<String> completions = new ArrayList();
      if (args.length == 1) {
         completions.addAll(Arrays.asList("doDamageTint", "lockPitch", "lockYaw", "showHitbox", "showShadow", "stepHeight", "scale", "hitboxScale", "viewRadius", "hideSelfDisguise", "color"));
         return completions;
      } else {
         String lastArg = args[args.length - 2].toLowerCase();
         if (!isValueExpected(lastArg)) {
            completions.addAll(Arrays.asList("doDamageTint", "lockPitch", "lockYaw", "showHitbox", "showShadow", "stepHeight", "scale", "hitboxScale", "viewRadius", "hideSelfDisguise", "color"));
         }

         return completions;
      }
   }

   private static boolean isValueExpected(String lastArg) {
      return lastArg.matches("stepheight|scale|hitboxscale|viewradius|color");
   }

   public void applyDisguiseOptions(ActiveModel activeModel) {
      if (this.scale != null) {
         activeModel.setScale(this.scale);
      }

      if (this.hitboxScale != null) {
         activeModel.setHitboxScale(this.hitboxScale);
      }

      if (this.doDamageTint != null) {
         activeModel.setCanHurt(this.doDamageTint);
      }

      if (this.lockPitch != null) {
         activeModel.setLockPitch(this.lockPitch);
      }

      if (this.lockYaw != null) {
         activeModel.setLockYaw(this.lockYaw);
      }

      if (this.showHitbox != null) {
         activeModel.setHitboxVisible(this.showHitbox);
      }

      if (this.showShadow != null) {
         activeModel.setShadowVisible(this.showShadow);
      }

      if (this.viewRadius != null) {
         activeModel.getModeledEntity().getBase().setRenderRadius(this.viewRadius);
      }

      if (this.stepHeight != null) {
         activeModel.getModeledEntity().getBase().setMaxStepHeight(this.stepHeight);
      }

      if (this.color != null) {
         activeModel.setDefaultTint(this.color);
      }

   }

   private static Boolean getNextBoolean(String[] args, int index) {
      if (index >= args.length) {
         return true;
      } else {
         String nextArg = args[index].toLowerCase();
         return nextArg.equals("true") || !nextArg.equals("false");
      }
   }

   private static Double getNextDouble(String[] args, int index) {
      try {
         return index < args.length ? Double.parseDouble(args[index]) : null;
      } catch (NumberFormatException var3) {
         return null;
      }
   }

   private static Integer getNextInteger(String[] args, int index) {
      try {
         return index < args.length ? Integer.parseInt(args[index]) : null;
      } catch (NumberFormatException var3) {
         return null;
      }
   }

   private static String getNextString(String[] args, int index) {
      try {
         return index < args.length ? args[index] : null;
      } catch (NumberFormatException var3) {
         return null;
      }
   }
}
