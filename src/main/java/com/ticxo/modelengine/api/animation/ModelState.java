package com.ticxo.modelengine.api.animation;

import com.ticxo.modelengine.api.utils.config.ConfigProperty;
import com.ticxo.modelengine.api.utils.config.Property;
import java.util.Locale;
import org.jetbrains.annotations.Nullable;

public enum ModelState implements Property {
   IDLE(BlueprintAnimation.LoopMode.LOOP, false),
   WALK(BlueprintAnimation.LoopMode.LOOP, false),
   STRAFE(BlueprintAnimation.LoopMode.LOOP, false),
   JUMP_START(BlueprintAnimation.LoopMode.ONCE, true),
   JUMP(BlueprintAnimation.LoopMode.LOOP, true),
   JUMP_END(BlueprintAnimation.LoopMode.ONCE, true),
   HOVER(BlueprintAnimation.LoopMode.LOOP, true),
   FLY(BlueprintAnimation.LoopMode.LOOP, true),
   SPAWN(BlueprintAnimation.LoopMode.ONCE, true),
   DEATH(BlueprintAnimation.LoopMode.HOLD, true);

   private final String path;
   private final Object def;
   private final BlueprintAnimation.LoopMode loopMode;
   private final boolean override;

   private ModelState(BlueprintAnimation.LoopMode loopMode, boolean override) {
      String var10001 = ConfigProperty.DEFAULT_NAMES.getPath();
      this.path = var10001 + "." + this.name();
      this.def = this.name().toLowerCase(Locale.ENGLISH);
      this.loopMode = loopMode;
      this.override = override;
   }

   @Nullable
   public static ModelState get(String value) {
      try {
         return valueOf(value.toUpperCase(Locale.ENGLISH));
      } catch (IllegalArgumentException var2) {
         return null;
      }
   }

   public String getPath() {
      return this.path;
   }

   public Object getDef() {
      return this.def;
   }

   public BlueprintAnimation.LoopMode getLoopMode() {
      return this.loopMode;
   }

   public boolean isOverride() {
      return this.override;
   }

   // $FF: synthetic method
   private static ModelState[] $values() {
      return new ModelState[]{IDLE, WALK, STRAFE, JUMP_START, JUMP, JUMP_END, HOVER, FLY, SPAWN, DEATH};
   }
}
