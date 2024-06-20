package com.ticxo.modelengine.v1_19_R3;

import com.ticxo.modelengine.api.utils.ReflectionUtils;
import net.minecraft.server.level.PlayerChunkMap.EntityTracker;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ai.control.ControllerMove;
import net.minecraft.world.level.World;

public enum NMSFields implements ReflectionUtils.ReflectionEnum {
   ENTITY_ENTITY_COUNTER(Entity.class, "d", "ENTITY_COUNTER"),
   ENTITY_dimensions(Entity.class, "be", "dimensions"),
   ENTITY_eyeHeight(Entity.class, "bf", "eyeHeight"),
   LIVING_ENTITY_noJumpDelay(EntityLiving.class, "bY", "noJumpDelay"),
   LIVING_ENTITY_jumping(EntityLiving.class, "bi", "jumping"),
   MOB_lookControl(EntityInsentient.class, "bJ", "lookControl"),
   MOB_moveControl(EntityInsentient.class, "bK", "moveControl"),
   MOB_navigation(EntityInsentient.class, "bM", "navigation"),
   MOB_goalSelector(EntityInsentient.class, "bN", "goalSelector"),
   MOB_bodyRotationControl(EntityInsentient.class, "bS", "bodyRotationControl"),
   SERVER_GAME_PACKET_LISTENER_IMPL_clientIsFloating(PlayerConnection.class, "G", "clientIsFloating"),
   SERVER_GAME_PACKET_LISTENER_IMPL_connection(PlayerConnection.class, "h", "connection"),
   MOVE_CONTROL_operation(ControllerMove.class, "k", "operation"),
   TRACKED_ENTITY_serverEntity(EntityTracker.class, "b", "serverEntity"),
   TRACKED_ENTITY_range(EntityTracker.class, "d", "range"),
   LEVEL_threadSafeRandom(World.class, "C", "threadSafeRandom");

   private final Class<?> target;
   private final String obfuscated;
   private final String mapped;

   private NMSFields(Class<?> target, String obfuscated, String mapped) {
      this.target = target;
      this.obfuscated = obfuscated;
      this.mapped = mapped;
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

   // $FF: synthetic method
   private static NMSFields[] $values() {
      return new NMSFields[]{ENTITY_ENTITY_COUNTER, ENTITY_dimensions, ENTITY_eyeHeight, LIVING_ENTITY_noJumpDelay, LIVING_ENTITY_jumping, MOB_lookControl, MOB_moveControl, MOB_navigation, MOB_goalSelector, MOB_bodyRotationControl, SERVER_GAME_PACKET_LISTENER_IMPL_clientIsFloating, SERVER_GAME_PACKET_LISTENER_IMPL_connection, MOVE_CONTROL_operation, TRACKED_ENTITY_serverEntity, TRACKED_ENTITY_range, LEVEL_threadSafeRandom};
   }
}
