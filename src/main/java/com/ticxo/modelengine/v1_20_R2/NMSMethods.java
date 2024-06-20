package com.ticxo.modelengine.v1_20_R2;

import com.ticxo.modelengine.api.utils.ReflectionUtils;
import net.minecraft.server.level.PlayerChunkMap.EntityTracker;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.item.InstrumentItem;
import net.minecraft.world.item.ItemStack;

public enum NMSMethods implements ReflectionUtils.MethodEnum {
   TRACKED_ENTITY_getEffectiveRange(EntityTracker.class, "b", "getEffectiveRange", new Class[0]),
   SERVER_GAME_PACKET_LISTENER_IMPL_checkLimit(PlayerConnection.class, "checkLimit", "checkLimit", new Class[]{Long.TYPE}),
   INSTRUMENT_ITEM_getInstrument(InstrumentItem.class, "d", "getInstrument", new Class[]{ItemStack.class});

   private final Class<?> target;
   private final String obfuscated;
   private final String mapped;
   private final Class<?>[] parameterClasses;

   private NMSMethods(Class<?> target, String obfuscated, String mapped, Class<?>... parameterClasses) {
      this.target = target;
      this.obfuscated = obfuscated;
      this.mapped = mapped;
      this.parameterClasses = parameterClasses;
   }

   public Class<?> getTarget() {
      return this.target;
   }

   public String getObfuscated() {
      return this.obfuscated;
   }

   public String getMapped() {
      return this.mapped;
   }

   public Class<?>[] getParameterClasses() {
      return this.parameterClasses;
   }

   // $FF: synthetic method
   private static NMSMethods[] $values() {
      return new NMSMethods[]{TRACKED_ENTITY_getEffectiveRange, SERVER_GAME_PACKET_LISTENER_IMPL_checkLimit, INSTRUMENT_ITEM_getInstrument};
   }
}
