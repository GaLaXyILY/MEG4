package com.ticxo.modelengine.v1_20_R2.network.patch;

import com.destroystokyo.paper.event.player.PlayerUseUnknownEntityEvent;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.ServerInfo;
import com.ticxo.modelengine.api.entity.BaseEntity;
import com.ticxo.modelengine.api.events.BaseEntityInteractEvent;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.nms.entity.EntityHandler;
import com.ticxo.modelengine.api.utils.ReflectionUtils;
import com.ticxo.modelengine.v1_20_R2.NMSMethods;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.network.protocol.PlayerConnectionUtils;
import net.minecraft.network.protocol.game.PacketListenerPlayIn;
import net.minecraft.network.protocol.game.PacketPlayInBlockPlace;
import net.minecraft.network.protocol.game.PacketPlayInUseEntity;
import net.minecraft.network.protocol.game.PacketPlayOutAttachEntity;
import net.minecraft.network.protocol.game.PacketPlayOutEntityEquipment;
import net.minecraft.network.protocol.game.PacketPlayInUseEntity.c;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityExperienceOrb;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.projectile.EntityArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.EnumGamemode;
import net.minecraft.world.level.RayTrace;
import net.minecraft.world.level.RayTrace.BlockCollisionOption;
import net.minecraft.world.level.RayTrace.FluidCollisionOption;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.MovingObjectPosition;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.Vec3D;
import net.minecraft.world.phys.MovingObjectPosition.EnumMovingObjectType;
import org.bukkit.craftbukkit.v1_20_R2.CraftServer;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R2.event.CraftEventFactory;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Consumer;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class PatchedServerGamePacketListener {
   private static final Logger LOGGER = LogUtils.getLogger();

   public static void handleInteract(ServerboundInteractPacketWrapper interactPacket, PacketListenerPlayIn listener) {
      if (listener instanceof PlayerConnection) {
         final PlayerConnection connection = (PlayerConnection)listener;
         PlayerConnectionUtils.a(interactPacket, connection, connection.p().x());
         final EntityPlayer player = connection.p();
         if (!player.eX()) {
            final CraftPlayer craftPlayer = player.getBukkitEntity();
            final WorldServer serverLevel = player.x();
            final CraftServer craftServer = serverLevel.n().server;
            final Entity entity = interactPacket.a(serverLevel);
            interactPacket.a(new c() {
               public void a(EnumHand hand) {
                  if (interactPacket.isFakeInteraction()) {
                     EntityHandler entityHandler = ModelEngineAPI.getNMSHandler().getEntityHandler();
                     entityHandler.forceUseItem(craftPlayer, EquipmentSlot.HAND);
                     entityHandler.forceUseItem(craftPlayer, EquipmentSlot.OFF_HAND);
                  }
               }

               public void a(EnumHand hand, Vec3D pos) {
               }

               public void a() {
               }
            });
            if (entity != player || player.M_()) {
               player.E();
               player.f(interactPacket.a());
               final ActiveModel activeModel = ModelEngineAPI.getInteractionTracker().getModelRelay(interactPacket.getOriginalId());
               if (activeModel != null) {
                  final BaseEntity<?> base = activeModel.getModeledEntity().getBase();
                  interactPacket.a(new c() {
                     public void a(EnumHand hand) {
                        craftServer.getPluginManager().callEvent(new BaseEntityInteractEvent(craftPlayer, base, activeModel, BaseEntityInteractEvent.Action.INTERACT, EquipmentSlot.HAND, interactPacket.a(), craftPlayer.getInventory().getItem(EquipmentSlot.HAND), (Vector)null));
                        craftServer.getPluginManager().callEvent(new BaseEntityInteractEvent(craftPlayer, base, activeModel, BaseEntityInteractEvent.Action.INTERACT, EquipmentSlot.OFF_HAND, interactPacket.a(), craftPlayer.getInventory().getItem(EquipmentSlot.OFF_HAND), (Vector)null));
                     }

                     public void a(EnumHand hand, Vec3D pos) {
                        Vector position = new Vector(pos.c, pos.d, pos.e);
                        craftServer.getPluginManager().callEvent(new BaseEntityInteractEvent(craftPlayer, base, activeModel, BaseEntityInteractEvent.Action.INTERACT_ON, EquipmentSlot.HAND, interactPacket.a(), craftPlayer.getInventory().getItem(EquipmentSlot.HAND), position));
                        craftServer.getPluginManager().callEvent(new BaseEntityInteractEvent(craftPlayer, base, activeModel, BaseEntityInteractEvent.Action.INTERACT_ON, EquipmentSlot.OFF_HAND, interactPacket.a(), craftPlayer.getInventory().getItem(EquipmentSlot.OFF_HAND), position));
                     }

                     public void a() {
                        craftServer.getPluginManager().callEvent(new BaseEntityInteractEvent(craftPlayer, base, activeModel, BaseEntityInteractEvent.Action.ATTACK, EquipmentSlot.HAND, interactPacket.a(), craftPlayer.getInventory().getItem(EquipmentSlot.HAND), (Vector)null));
                     }
                  });
               }

               if (entity == null) {
                  if (ServerInfo.IS_PAPER) {
                     interactPacket.a(new c() {
                        public void a(EnumHand hand) {
                           PatchedServerGamePacketListener.callPlayerUseUnknownEntityEvent(craftServer, craftPlayer, interactPacket, hand, (Vec3D)null);
                        }

                        public void a(EnumHand hand, Vec3D pos) {
                           PatchedServerGamePacketListener.callPlayerUseUnknownEntityEvent(craftServer, craftPlayer, interactPacket, hand, pos);
                        }

                        public void a() {
                           PatchedServerGamePacketListener.callPlayerUseUnknownEntityEvent(craftServer, craftPlayer, interactPacket, EnumHand.a, (Vec3D)null);
                        }
                     });
                  }

               } else if (serverLevel.B_().a(entity.dl())) {
                  AxisAlignedBB boundingBox = entity.cG();
                  if (!(boundingBox.e(player.bp()) >= PlayerConnection.d)) {
                     interactPacket.a(new c() {
                        private void performInteraction(EnumHand enumhand, PatchedServerGamePacketListener.EntityInteraction entityInteraction, PlayerInteractEntityEvent event) {
                           ItemStack itemstack = player.b(enumhand);
                           if (itemstack.a(serverLevel.G())) {
                              ItemStack itemstack1 = itemstack.p();
                              ItemStack itemInHand = player.b(enumhand);
                              boolean triggerLeashUpdate = itemInHand.d() == Items.tQ && entity instanceof EntityInsentient;
                              Item origItem = player.fR().f().d();
                              craftServer.getPluginManager().callEvent(event);
                              if (entity instanceof Bucketable && entity instanceof EntityLiving && origItem.k() == Items.pL && (event.isCancelled() || player.fR().f().d() != origItem)) {
                                 entity.al().resendPossiblyDesyncedEntity(player);
                                 player.bS.b();
                              }

                              if (triggerLeashUpdate && (event.isCancelled() || player.fR().f().d() != origItem)) {
                                 connection.b(new PacketPlayOutAttachEntity(entity, ((EntityInsentient)entity).fS()));
                              }

                              if (event.isCancelled() || player.fR().f().d() != origItem) {
                                 entity.al().refresh(player);
                                 if (entity instanceof Allay) {
                                    Allay allay = (Allay)entity;
                                    connection.b(new PacketPlayOutEntityEquipment(entity.ah(), (List)Arrays.stream(EnumItemSlot.values()).map((slot) -> {
                                       return Pair.of(slot, allay.stripMeta(allay.c(slot), true));
                                    }).collect(Collectors.toList())));
                                    player.bS.b();
                                 }
                              }

                              if (event.isCancelled()) {
                                 player.bS.b();
                                 return;
                              }

                              EnumInteractionResult enuminteractionresult = entityInteraction.run(player, entity, enumhand);
                              if (!itemInHand.b() && itemInHand.L() <= -1) {
                                 player.bS.b();
                              }

                              if (enuminteractionresult.a()) {
                                 CriterionTriggers.Q.a(player, itemstack1, entity);
                                 if (enuminteractionresult.b()) {
                                    player.a(enumhand, true);
                                 }
                              }
                           }

                        }

                        public void a(@NotNull EnumHand hand) {
                           this.performInteraction(hand, EntityHuman::a, new PlayerInteractEntityEvent(connection.getCraftPlayer(), entity.getBukkitEntity(), hand == EnumHand.b ? EquipmentSlot.OFF_HAND : EquipmentSlot.HAND));
                        }

                        public void a(@NotNull EnumHand hand, @NotNull Vec3D pos) {
                           this.performInteraction(hand, (entityplayer, entity1, enumhand1) -> {
                              return entity1.a(entityplayer, pos, enumhand1);
                           }, new PlayerInteractAtEntityEvent(connection.getCraftPlayer(), entity.getBukkitEntity(), new Vector(pos.c, pos.d, pos.e), hand == EnumHand.b ? EquipmentSlot.OFF_HAND : EquipmentSlot.HAND));
                        }

                        public void a() {
                           if (!(entity instanceof EntityItem) && !(entity instanceof EntityExperienceOrb) && !(entity instanceof EntityArrow) && (entity != player || player.M_())) {
                              ItemStack itemstack = player.b(EnumHand.a);
                              if (itemstack.a(serverLevel.G())) {
                                 player.d(entity);
                                 if (!itemstack.b() && itemstack.L() <= -1) {
                                    player.bS.b();
                                 }
                              }
                           }

                        }
                     });
                  }
               }
            }
         }
      }
   }

   public static void handleUseItem(PacketPlayInBlockPlace packet, PacketListenerPlayIn listener, Consumer<EnumInteractionResult> afterUse) {
      if (listener instanceof PlayerConnection) {
         PlayerConnection connection = (PlayerConnection)listener;
         EntityPlayer player = connection.p();
         PlayerConnectionUtils.a(packet, connection, player.x());
         if (!player.eX()) {
            boolean shouldLimit = Boolean.TRUE.equals(ReflectionUtils.call(connection, NMSMethods.SERVER_GAME_PACKET_LISTENER_IMPL_checkLimit, packet.timestamp));
            if (shouldLimit) {
               connection.a(packet.d());
               WorldServer worldserver = player.x();
               EnumHand enumhand = packet.a();
               ItemStack itemstack = player.b(enumhand);
               player.E();
               if (!itemstack.b() && itemstack.a(worldserver.G())) {
                  float f1 = player.dD();
                  float f2 = player.dB();
                  double d0 = player.dq();
                  double d1 = player.ds() + (double)player.cH();
                  double d2 = player.dw();
                  Vec3D vec3d = new Vec3D(d0, d1, d2);
                  float f3 = MathHelper.b(-f2 * 0.017453292F - 3.1415927F);
                  float f4 = MathHelper.a(-f2 * 0.017453292F - 3.1415927F);
                  float f5 = -MathHelper.b(-f1 * 0.017453292F);
                  float f6 = MathHelper.a(-f1 * 0.017453292F);
                  float f7 = f4 * f5;
                  float f8 = f3 * f5;
                  double d3 = player.e.b() == EnumGamemode.b ? 5.0D : 4.5D;
                  Vec3D vec3d1 = vec3d.b((double)f7 * d3, (double)f6 * d3, (double)f8 * d3);
                  MovingObjectPosition movingobjectposition = player.dL().a(new RayTrace(vec3d, vec3d1, BlockCollisionOption.b, FluidCollisionOption.a, player));
                  boolean cancelled;
                  if (movingobjectposition != null && movingobjectposition.c() == EnumMovingObjectType.b) {
                     MovingObjectPositionBlock movingobjectpositionblock = (MovingObjectPositionBlock)movingobjectposition;
                     if (player.e.firedInteract && player.e.interactPosition.equals(movingobjectpositionblock.a()) && player.e.interactHand == enumhand && ItemStack.c(player.e.interactItemStack, itemstack)) {
                        cancelled = player.e.interactResult;
                     } else {
                        PlayerInteractEvent event = CraftEventFactory.callPlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, movingobjectpositionblock.a(), movingobjectpositionblock.b(), itemstack, true, enumhand, movingobjectpositionblock.e());
                        cancelled = event.useItemInHand() == Result.DENY;
                     }

                     player.e.firedInteract = false;
                  } else {
                     PlayerInteractEvent event = CraftEventFactory.callPlayerInteractEvent(player, Action.RIGHT_CLICK_AIR, itemstack, enumhand);
                     cancelled = event.useItemInHand() == Result.DENY;
                  }

                  if (cancelled) {
                     player.resyncUsingItem(player);
                     player.getBukkitEntity().updateInventory();
                     return;
                  }

                  itemstack = player.b(enumhand);
                  if (itemstack.b()) {
                     return;
                  }

                  EnumInteractionResult enuminteractionresult = player.e.a(player, worldserver, itemstack, enumhand);
                  afterUse.accept(enuminteractionresult);
                  if (enuminteractionresult.b()) {
                     player.a(enumhand, true);
                  }
               }

            }
         }
      }
   }

   private static void callPlayerUseUnknownEntityEvent(CraftServer cserver, CraftPlayer craftPlayer, PacketPlayInUseEntity packet, EnumHand hand, @Nullable Vec3D vector) {
      cserver.getPluginManager().callEvent(new PlayerUseUnknownEntityEvent(craftPlayer, packet.getEntityId(), packet.isAttack(), hand == EnumHand.a ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND, vector != null ? new Vector(vector.c, vector.d, vector.e) : null));
   }

   @FunctionalInterface
   private interface EntityInteraction {
      EnumInteractionResult run(EntityPlayer var1, Entity var2, EnumHand var3);
   }
}
