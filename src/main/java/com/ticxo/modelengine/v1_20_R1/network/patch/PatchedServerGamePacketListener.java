package com.ticxo.modelengine.v1_20_R1.network.patch;

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
import com.ticxo.modelengine.v1_20_R1.NMSMethods;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import me.coley.recaf.metadata.InsnComment;
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
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.Vec3D;
import net.minecraft.world.phys.MovingObjectPosition.EnumMovingObjectType;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.event.CraftEventFactory;
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
         PlayerConnectionUtils.a(interactPacket, connection, connection.f().x());
         final EntityPlayer player = connection.f();
         if (!player.eT()) {
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
            if (entity != player || player.G_()) {
               player.C();
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

               } else if (serverLevel.w_().a(entity.di())) {
                  AxisAlignedBB boundingBox = entity.cE();
                  if (!(boundingBox.e(player.bm()) >= PlayerConnection.a)) {
                     interactPacket.a(new c() {
                        private void performInteraction(EnumHand enumhand, PatchedServerGamePacketListener.EntityInteraction entityInteraction, PlayerInteractEntityEvent event) {
                           ItemStack itemstack = player.b(enumhand);
                           if (itemstack.a(serverLevel.G())) {
                              ItemStack itemstack1 = itemstack.p();
                              ItemStack itemInHand = player.b(enumhand);
                              boolean triggerLeashUpdate = itemInHand.d() == Items.tQ && entity instanceof EntityInsentient;
                              Item origItem = player.fN().f().d();
                              craftServer.getPluginManager().callEvent(event);
                              if (entity instanceof Bucketable && entity instanceof EntityLiving && origItem.k() == Items.pL && (event.isCancelled() || player.fN().f().d() != origItem)) {
                                 entity.aj().resendPossiblyDesyncedEntity(player);
                                 player.bR.b();
                              }

                              if (triggerLeashUpdate && (event.isCancelled() || player.fN().f().d() != origItem)) {
                                 connection.a(new PacketPlayOutAttachEntity(entity, ((EntityInsentient)entity).fP()));
                              }

                              if (event.isCancelled() || player.fN().f().d() != origItem) {
                                 entity.aj().refresh(player);
                                 if (entity instanceof Allay) {
                                    Allay allay = (Allay)entity;
                                    connection.a(new PacketPlayOutEntityEquipment(entity.af(), (List)Arrays.stream(EnumItemSlot.values()).map((slot) -> {
                                       return Pair.of(slot, allay.stripMeta(allay.c(slot), true));
                                    }).collect(Collectors.toList())));
                                    player.bR.b();
                                 }
                              }

                              if (event.isCancelled()) {
                                 player.bR.b();
                                 return;
                              }

                              EnumInteractionResult enuminteractionresult = entityInteraction.run(player, entity, enumhand);
                              if (!itemInHand.b() && itemInHand.L() <= -1) {
                                 player.bR.b();
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
                           if (!(entity instanceof EntityItem) && !(entity instanceof EntityExperienceOrb) && !(entity instanceof EntityArrow) && (entity != player || player.G_())) {
                              ItemStack itemstack = player.b(EnumHand.a);
                              if (itemstack.a(serverLevel.G())) {
                                 player.d(entity);
                                 if (!itemstack.b() && itemstack.L() <= -1) {
                                    player.bR.b();
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

   @InsnComment(
      At_0 = "EFdmR3QrR2M1UWdoNTaEFWOwUVW3FkeNJTUE9keRRlT592MaJWQ20UWOZ2T6hXYPF0YGhXN"
   )
   public static void handleUseItem(PacketPlayInBlockPlace var0, PacketListenerPlayIn var1, Consumer<EnumInteractionResult> var2) {
      if (var1 instanceof PlayerConnection) {
         PlayerConnection var3 = (PlayerConnection)var1;
         EntityPlayer var4 = var3.f();
         PlayerConnectionUtils.a(var0, var3, var4.x());
         if (!var4.eT()) {
            boolean var5 = Boolean.TRUE.equals(ReflectionUtils.call(var3, NMSMethods.SERVER_GAME_PACKET_LISTENER_IMPL_checkLimit, var0.timestamp));
            if (var5) {
               var3.a(var0.c());
               WorldServer var6 = var4.x();
               EnumHand var7 = var0.a();
               ItemStack var8 = var4.b(var7);
               var4.C();
               if (!var8.b() && var8.a(var6.G())) {
                  float var9 = var4.dA();
                  float var10 = var4.dy();
                  double var11 = var4.dn();
                  double var13 = var4.dp() + (double)var4.cF();
                  double var15 = var4.dt();
                  Vec3D var17 = new Vec3D(var11, var13, var15);
                  float var18 = MathHelper.b(-var10 * 0.017453292F - 3.1415927F);
                  float var19 = MathHelper.a(-var10 * 0.017453292F - 3.1415927F);
                  float var20 = -MathHelper.b(-var9 * 0.017453292F);
                  float var21 = MathHelper.a(-var9 * 0.017453292F);
                  float var22 = var19 * var20;
                  float var23 = var18 * var20;
                  double var24 = var4.e.b() == EnumGamemode.b ? 5.0D : 4.5D;
                  Vec3D var26 = var17.b((double)var22 * var24, (double)var21 * var24, (double)var23 * var24);
                  MovingObjectPositionBlock var27 = var4.dI().a(new RayTrace(var17, var26, BlockCollisionOption.b, FluidCollisionOption.a, var4));
                  PlayerInteractEvent var28;
                  boolean var30;
                  if (var27 != null && var27.c() == EnumMovingObjectType.b) {
                     MovingObjectPositionBlock var29 = (MovingObjectPositionBlock)var27;
                     if (var4.e.firedInteract && var4.e.interactPosition.equals(var29.a()) && var4.e.interactHand == var7 && ItemStack.c(var4.e.interactItemStack, var8)) {
                        var30 = var4.e.interactResult;
                     } else {
                        var28 = CraftEventFactory.callPlayerInteractEvent(var4, Action.RIGHT_CLICK_BLOCK, var29.a(), var29.b(), var8, true, var7, var29.e());
                        var30 = var28.useItemInHand() == Result.DENY;
                     }

                     var4.e.firedInteract = false;
                  } else {
                     var28 = CraftEventFactory.callPlayerInteractEvent(var4, Action.RIGHT_CLICK_AIR, var8, var7);
                     var30 = var28.useItemInHand() == Result.DENY;
                  }

                  if (var30) {
                     var4.resyncUsingItem(var4);
                     var4.getBukkitEntity().updateInventory();
                     return;
                  }

                  var8 = var4.b(var7);
                  if (var8.b()) {
                     return;
                  }

                  EnumInteractionResult var31 = var4.e.a(var4, var6, var8, var7);
                  var2.accept(var31);
                  if (var31.b()) {
                     var4.a(var7, true);
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
