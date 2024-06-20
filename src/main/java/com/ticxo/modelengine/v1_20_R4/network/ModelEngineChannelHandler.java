package com.ticxo.modelengine.v1_20_R4.network;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.interaction.DynamicHitbox;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModelUpdaters;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.model.bone.BoneBehaviorTypes;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehavior;
import com.ticxo.modelengine.api.model.bone.render.renderer.MountRenderer;
import com.ticxo.modelengine.api.model.bone.type.Mount;
import com.ticxo.modelengine.api.mount.controller.MountController;
import com.ticxo.modelengine.api.nms.entity.EntityHandler;
import com.ticxo.modelengine.api.nms.network.ProtectedPacket;
import com.ticxo.modelengine.api.utils.data.tracker.CollectionDataTracker;
import com.ticxo.modelengine.api.utils.math.TMath;
import com.ticxo.modelengine.v1_20_R4.entity.EntityUtils;
import com.ticxo.modelengine.v1_20_R4.network.patch.ServerboundInteractPacketWrapper;
import com.ticxo.modelengine.v1_20_R4.network.utils.NetworkUtils;
import com.ticxo.modelengine.v1_20_R4.network.utils.PacketInterceptor;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import it.unimi.dsi.fastutil.Pair;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.PacketListenerPlayIn;
import net.minecraft.network.protocol.game.PacketListenerPlayOut;
import net.minecraft.network.protocol.game.PacketPlayInSteerVehicle;
import net.minecraft.network.protocol.game.PacketPlayInUseEntity;
import net.minecraft.network.protocol.game.PacketPlayOutAnimation;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutEntityEffect;
import net.minecraft.network.protocol.game.PacketPlayOutEntityEquipment;
import net.minecraft.network.protocol.game.PacketPlayOutEntityHeadRotation;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutEntityStatus;
import net.minecraft.network.protocol.game.PacketPlayOutEntityTeleport;
import net.minecraft.network.protocol.game.PacketPlayOutEntityVelocity;
import net.minecraft.network.protocol.game.PacketPlayOutMount;
import net.minecraft.network.protocol.game.PacketPlayOutRemoveEntityEffect;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.network.protocol.game.PacketPlayOutEntity.PacketPlayOutEntityLook;
import net.minecraft.network.protocol.game.PacketPlayOutEntity.PacketPlayOutRelEntityMove;
import net.minecraft.network.protocol.game.PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.network.syncher.DataWatcher.b;
import net.minecraft.server.level.EntityPlayer;
import org.bukkit.craftbukkit.v1_20_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ModelEngineChannelHandler extends ChannelDuplexHandler {
   private final Player player;
   private final EntityPlayer serverPlayer;
   private final ModelUpdaters updaters;
   private final EntityHandler entityHandler;
   private final PacketInterceptor<PacketListenerPlayOut> writeInterceptors;
   private final PacketInterceptor<PacketListenerPlayIn> readInterceptors;

   public ModelEngineChannelHandler(Player player) {
      this.player = player;
      this.serverPlayer = ((CraftPlayer)player).getHandle();
      this.updaters = ModelEngineAPI.getAPI().getModelUpdaters();
      this.entityHandler = ModelEngineAPI.getEntityHandler();
      this.writeInterceptors = new PacketInterceptor();
      this.writeInterceptors.register(PacketPlayOutSpawnEntity.class, this::handleAddEntity).register(PacketPlayOutEntityDestroy.class, this::handleRemoveEntities).register(PacketPlayOutRelEntityMove.class, this::handleEntityId).register(PacketPlayOutEntityLook.class, this::handleEntityId).register(PacketPlayOutRelEntityMoveLook.class, this::handleEntityId).register(PacketPlayOutEntityHeadRotation.class, this::handleEntityId).register(PacketPlayOutEntityStatus.class, this::handleEntityId).register(PacketPlayOutEntityVelocity.class, this::handleEntityMotion).register(PacketPlayOutEntityTeleport.class, this::handleTeleportEntity).register(PacketPlayOutAnimation.class, this::handleAnimate).register(PacketPlayOutEntityMetadata.class, this::handleEntityData).register(PacketPlayOutEntityEquipment.class, this::handleSetEquipment).register(PacketPlayOutRemoveEntityEffect.class, this::handleEntityId).register(PacketPlayOutEntityEffect.class, this::handleUpdateMobEffect).registerPost(PacketPlayOutSpawnEntity.class, this::handleAddEntityPost);
      this.readInterceptors = new PacketInterceptor();
      this.readInterceptors.register(PacketPlayInUseEntity.class, this::handleInteract).register(PacketPlayInSteerVehicle.class, this::handlePlayerInput);
   }

   public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
      if (msg instanceof ProtectedPacket) {
         ProtectedPacket protectedPacket = (ProtectedPacket)msg;
         super.write(ctx, protectedPacket.packet(), promise);
      } else if (!(msg instanceof Packet)) {
         super.write(ctx, msg, promise);
      } else {
         Packet packet = (Packet)msg;

         try {
            ArrayList list;
            ClientboundBundlePacket packet;
            if (packet instanceof ClientboundBundlePacket) {
               ClientboundBundlePacket bundle = (ClientboundBundlePacket)packet;
               list = new ArrayList();
               Iterator var7 = bundle.a().iterator();

               while(var7.hasNext()) {
                  Packet<PacketListenerPlayOut> subPacket = (Packet)var7.next();
                  Packet<PacketListenerPlayOut> result = this.writeInterceptors.accept(subPacket);
                  if (result != null) {
                     list.add(result);
                     list.addAll(this.writeInterceptors.acceptPost(result));
                  }
               }

               packet = new ClientboundBundlePacket(list);
               super.write(ctx, packet, promise);
            } else {
               packet = this.writeInterceptors.accept(packet);
               if (packet == null) {
                  return;
               }

               list = new ArrayList();
               list.add(packet);
               list.addAll(this.writeInterceptors.acceptPost(packet));
               if (list.size() == 1) {
                  super.write(ctx, packet, promise);
               } else {
                  packet = new ClientboundBundlePacket(list);
                  super.write(ctx, packet, promise);
               }
            }
         } catch (Throwable var10) {
            var10.printStackTrace();
         }

      }
   }

   public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) throws Exception {
      if (!(msg instanceof Packet)) {
         super.channelRead(ctx, msg);
      } else {
         Packet<PacketListenerPlayIn> packet = this.readInterceptors.accept((Packet)msg);
         if (packet != null) {
            super.channelRead(ctx, packet);
            this.readInterceptors.acceptPost(packet);
         }
      }
   }

   private PacketPlayOutSpawnEntity handleAddEntity(PacketPlayOutSpawnEntity packet) {
      return this.shouldShow(packet.a()) ? packet : null;
   }

   private List<Packet<PacketListenerPlayOut>> handleAddEntityPost(PacketPlayOutSpawnEntity packet) {
      return this.handleMount(packet.d());
   }

   private List<Packet<PacketListenerPlayOut>> handleMount(UUID uuid) {
      Pair<ActiveModel, MountController> pair = ModelEngineAPI.getMountPairManager().get(uuid);
      if (pair == null) {
         return null;
      } else {
         ActiveModel model = (ActiveModel)pair.left();
         ArrayList<Packet<PacketListenerPlayOut>> list = new ArrayList();
         model.getBehaviorRenderer(BoneBehaviorTypes.MOUNT).ifPresent((behaviorRenderer) -> {
            if (behaviorRenderer instanceof MountRenderer) {
               MountRenderer renderer = (MountRenderer)behaviorRenderer;
               MountController controller = (MountController)pair.right();
               Mount patt6286$temp = controller.getMount();
               if (patt6286$temp instanceof BoneBehavior) {
                  BoneBehavior behavior = (BoneBehavior)patt6286$temp;
                  MountRenderer.Mount mount = (MountRenderer.Mount)renderer.getRendered().get(behavior.getBone().getBoneId());
                  CollectionDataTracker<Integer> ids = mount.getPassengers();
                  PacketDataSerializer buf = NetworkUtils.createByteBuf();
                  buf.c(mount.getMountId());
                  buf.c(ids.size());
                  Objects.requireNonNull(buf);
                  ids.forEach(buf::c);
                  list.add(new PacketPlayOutMount(buf));
               }
            }

         });
         return list;
      }
   }

   private PacketPlayOutEntityDestroy handleRemoveEntities(PacketPlayOutEntityDestroy packet) {
      PacketDataSerializer buf = NetworkUtils.readData(packet);
      int size = buf.n();
      HashSet<Integer> set = new HashSet(size);

      int id;
      for(int i = 0; i < size; ++i) {
         id = buf.n();
         if (this.shouldShow(id)) {
            set.add(id);
         }
      }

      if (set.size() == size) {
         return packet;
      } else {
         buf = NetworkUtils.createByteBuf();
         buf.c(set.size());
         Iterator var7 = set.iterator();

         while(var7.hasNext()) {
            id = (Integer)var7.next();
            buf.c(id);
         }

         return new PacketPlayOutEntityDestroy(buf);
      }
   }

   private <U extends PacketListener, T extends Packet<U>> Packet<U> handleEntityId(T packet) {
      PacketDataSerializer buf = NetworkUtils.readData(packet);
      int id = buf.n();
      return this.shouldShow(id) ? packet : null;
   }

   private PacketPlayOutEntityVelocity handleEntityMotion(PacketPlayOutEntityVelocity packet) {
      return this.shouldShow(packet.a()) ? packet : null;
   }

   private PacketPlayOutEntityTeleport handleTeleportEntity(PacketPlayOutEntityTeleport packet) {
      return this.shouldShow(packet.a()) ? packet : null;
   }

   private PacketPlayOutAnimation handleAnimate(PacketPlayOutAnimation packet) {
      return this.shouldShow(packet.a()) ? packet : null;
   }

   private PacketPlayOutEntityMetadata handleEntityData(PacketPlayOutEntityMetadata packet) {
      if (!this.shouldShow(packet.a())) {
         return null;
      } else if (packet.a() != this.player.getEntityId()) {
         return packet;
      } else {
         if (this.entityHandler.isForcedInvisible(this.player)) {
            PacketDataSerializer buf = NetworkUtils.createByteBuf();
            buf.c(packet.a());
            Iterator var3 = packet.d().iterator();

            while(var3.hasNext()) {
               b<?> item = (b)var3.next();
               if (item.a() == 0) {
                  byte data = (Byte)item.c();
                  data = TMath.setBit(data, 5, true);
                  EntityUtils.writeData(buf, 0, DataWatcherRegistry.a, data);
               } else {
                  EntityUtils.writeDataUnsafe(buf, item.a(), item.b(), item.c());
               }
            }

            buf.k(255);
            packet = new PacketPlayOutEntityMetadata(buf);
         }

         return packet;
      }
   }

   private PacketPlayOutEntityEquipment handleSetEquipment(PacketPlayOutEntityEquipment packet) {
      return this.shouldShow(packet.a()) ? packet : null;
   }

   private PacketPlayOutEntityEffect handleUpdateMobEffect(PacketPlayOutEntityEffect packet) {
      return packet.a() != this.player.getEntityId() && !this.shouldShow(packet.a()) ? null : packet;
   }

   private boolean shouldShow(int id) {
      if (ModelEngineAPI.isRenderCanceled(id)) {
         return false;
      } else {
         ModeledEntity entity = this.updaters.getModeledEntity(id);
         return entity == null || entity.isBaseEntityVisible();
      }
   }

   private PacketPlayInUseEntity handleInteract(PacketPlayInUseEntity packet) {
      PacketDataSerializer buf = NetworkUtils.readData(packet);
      int entityId = buf.n();
      if (entityId == DynamicHitbox.getHitboxId()) {
         DynamicHitbox hitbox = ModelEngineAPI.getInteractionTracker().getDynamicHitbox(this.player.getUniqueId());
         if (hitbox != null) {
            return new ServerboundInteractPacketWrapper(entityId, hitbox.getTarget(), packet);
         }
      }

      ActiveModel activeModel = ModelEngineAPI.getInteractionTracker().getModelRelay(entityId);
      if (activeModel != null) {
         ModeledEntity modeledEntity = activeModel.getModeledEntity();
         if (modeledEntity == null) {
            return packet;
         } else {
            int id = modeledEntity.getBase().getEntityId();
            return new ServerboundInteractPacketWrapper(entityId, id, packet);
         }
      } else {
         Integer relayed = ModelEngineAPI.getInteractionTracker().getEntityRelay(entityId);
         return (PacketPlayInUseEntity)(relayed != null ? new ServerboundInteractPacketWrapper(entityId, relayed, packet) : packet);
      }
   }

   private PacketPlayInSteerVehicle handlePlayerInput(PacketPlayInSteerVehicle inputPacket) {
      MountController controller = ModelEngineAPI.getMountPairManager().getController(this.player.getUniqueId());
      if (controller != null) {
         MountController.MountInput input = controller.getInput();
         if (input == null) {
            controller.setInput(new MountController.MountInput(inputPacket.a(), inputPacket.d(), inputPacket.e(), inputPacket.f()));
         } else {
            input.setSide(inputPacket.a());
            input.setFront(inputPacket.d());
            input.setJump(inputPacket.e());
            input.setSneak(inputPacket.f());
         }
      }

      return inputPacket;
   }
}
