package com.ticxo.modelengine.v1_20_R1.entity;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.entity.BaseEntity;
import com.ticxo.modelengine.api.entity.Hitbox;
import com.ticxo.modelengine.api.entity.data.BukkitEntityData;
import com.ticxo.modelengine.api.entity.data.IEntityData;
import com.ticxo.modelengine.api.interaction.DynamicHitbox;
import com.ticxo.modelengine.api.model.bone.ModelBone;
import com.ticxo.modelengine.api.model.bone.type.SubHitbox;
import com.ticxo.modelengine.api.nms.entity.EntityHandler;
import com.ticxo.modelengine.api.nms.entity.HitboxEntity;
import com.ticxo.modelengine.api.nms.entity.wrapper.BodyRotationController;
import com.ticxo.modelengine.api.nms.entity.wrapper.LookController;
import com.ticxo.modelengine.api.nms.entity.wrapper.MoveController;
import com.ticxo.modelengine.api.nms.entity.wrapper.TrackedEntity;
import com.ticxo.modelengine.api.nms.impl.TempTrackedEntity;
import com.ticxo.modelengine.api.utils.RaceConditionUtil;
import com.ticxo.modelengine.api.utils.ReflectionUtils;
import com.ticxo.modelengine.api.utils.config.ConfigProperty;
import com.ticxo.modelengine.api.utils.config.DebugToggle;
import com.ticxo.modelengine.api.utils.logger.TLogger;
import com.ticxo.modelengine.api.utils.math.TMath;
import com.ticxo.modelengine.api.utils.promise.Promise;
import com.ticxo.modelengine.v1_20_R1.NMSFields;
import com.ticxo.modelengine.v1_20_R1.NMSMethods;
import com.ticxo.modelengine.v1_20_R1.entity.controller.BodyRotationControlWrapper;
import com.ticxo.modelengine.v1_20_R1.entity.controller.LookControlWrapper;
import com.ticxo.modelengine.v1_20_R1.entity.controller.MoveControlWrapper;
import com.ticxo.modelengine.v1_20_R1.entity.hitbox.HitboxEntityImpl;
import com.ticxo.modelengine.v1_20_R1.entity.navigation.AmphibiousNavigationWrapper;
import com.ticxo.modelengine.v1_20_R1.entity.navigation.FlyingNavigationWrapper;
import com.ticxo.modelengine.v1_20_R1.entity.navigation.GroundNavigationWrapper;
import com.ticxo.modelengine.v1_20_R1.entity.navigation.WallClimberNavigationWrapper;
import com.ticxo.modelengine.v1_20_R1.entity.navigation.WaterBoundNavigationWrapper;
import com.ticxo.modelengine.v1_20_R1.network.patch.PatchedServerGamePacketListener;
import com.ticxo.modelengine.v1_20_R1.network.utils.NetworkUtils;
import com.ticxo.modelengine.v1_20_R1.network.utils.Packets;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.game.PacketPlayInBlockPlace;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutEntitySound;
import net.minecraft.network.protocol.game.PacketPlayOutMount;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.level.PlayerChunkMap.EntityTracker;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntitySize;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.goal.PathfinderGoal;
import net.minecraft.world.entity.ai.goal.PathfinderGoalSelector;
import net.minecraft.world.entity.ai.goal.PathfinderGoalWrapped;
import net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation;
import net.minecraft.world.entity.ai.navigation.Navigation;
import net.minecraft.world.entity.ai.navigation.NavigationAbstract;
import net.minecraft.world.entity.ai.navigation.NavigationFlying;
import net.minecraft.world.entity.ai.navigation.NavigationGuardian;
import net.minecraft.world.entity.ai.navigation.NavigationSpider;
import net.minecraft.world.item.EnumAnimation;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.InstrumentItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.Vec3D;
import net.minecraft.world.phys.MovingObjectPosition.EnumMovingObjectType;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftLocation;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class EntityHandlerImpl implements EntityHandler {
   private static final AtomicInteger ENTITY_COUNTER;
   private final Set<UUID> forceInvisible = new HashSet();
   private double forceRenderWidth;
   private double forceRenderHeight;

   public EntityHandlerImpl() {
      ModelEngineAPI.getAPI().getConfigManager().registerReferenceUpdate(this::updateConfig);
   }

   public void updateConfig() {
      this.forceRenderWidth = ConfigProperty.BLOCK_CULL_IGNORE_SIZE_WIDTH.getDouble();
      this.forceRenderHeight = ConfigProperty.BLOCK_CULL_IGNORE_SIZE_HEIGHT.getDouble();
   }

   public int getNextEntityId() {
      return ENTITY_COUNTER == null ? 0 : ENTITY_COUNTER.incrementAndGet();
   }

   public void setHitbox(Entity entity, @NotNull Hitbox hitbox) {
      net.minecraft.world.entity.Entity nms = EntityUtils.nms(entity);
      EntitySize box = new EntitySize((float)hitbox.getMaxWidth(), (float)hitbox.getHeight(), true);
      ReflectionUtils.set(nms, NMSFields.ENTITY_dimensions, box);
      ReflectionUtils.set(nms, NMSFields.ENTITY_eyeHeight, (float)hitbox.getEyeHeight());
      nms.a(box.a(nms.dg()));
   }

   public void setStepHeight(Entity entity, double height) {
      EntityUtils.nms(entity).r((float)height);
   }

   public double getStepHeight(Entity entity) {
      return (double)EntityUtils.nms(entity).dC();
   }

   public void setPosition(Entity entity, double x, double y, double z) {
      EntityUtils.nms(entity).e(x, y, z);
   }

   public void movePassenger(Entity entity, double x, double y, double z) {
      net.minecraft.world.entity.Entity nmsEntity = EntityUtils.nms(entity);
      double seatY = y + nmsEntity.bw();
      nmsEntity.e(x, seatY, z);
      nmsEntity.f(Vec3D.b);
      nmsEntity.n();
      if (nmsEntity instanceof EntityPlayer) {
         EntityPlayer player = (EntityPlayer)nmsEntity;
         ReflectionUtils.set(player.c, NMSFields.SERVER_GAME_PACKET_LISTENER_IMPL_clientIsFloating, false);
      }

   }

   public void forceSpawn(BaseEntity<?> entity, Player player) {
      IEntityData data = entity.getData();
      if (data instanceof BukkitEntityData) {
         BukkitEntityData bukkitEntityData = (BukkitEntityData)data;
         bukkitEntityData.getTracked().sendPairingData(player);
      }
   }

   public void forceDespawn(BaseEntity<?> entity, Player player) {
      NetworkUtils.send((Player)player, new PacketPlayOutEntityDestroy(new int[]{entity.getEntityId()}));
   }

   public void setForcedInvisible(Player player, boolean flag) {
      if (this.isForcedInvisible(player) != flag) {
         net.minecraft.world.entity.Entity nms = EntityUtils.nms(player);
         byte data = 0;

         for(int i = 0; i < 8; ++i) {
            data = TMath.setBit(data, i, nms.i(i));
         }

         if (flag) {
            this.forceInvisible.add(player.getUniqueId());
            data = TMath.setBit(data, 5, true);
         } else {
            this.forceInvisible.remove(player.getUniqueId());
         }

         PacketDataSerializer buf = NetworkUtils.createByteBuf();
         buf.d(player.getEntityId());
         EntityUtils.writeData(buf, 0, DataWatcherRegistry.a, data);
         buf.writeByte(255);
         PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(buf);
         NetworkUtils.send((Player)player, packet);
      }
   }

   public boolean isForcedInvisible(Player player) {
      return this.forceInvisible.contains(player.getUniqueId());
   }

   public BodyRotationController wrapBodyRotationControl(Entity entity, Supplier<BodyRotationController> def) {
      net.minecraft.world.entity.Entity var4 = EntityUtils.nms(entity);
      if (var4 instanceof EntityInsentient) {
         EntityInsentient mob = (EntityInsentient)var4;
         BodyRotationControlWrapper controller = new BodyRotationControlWrapper(mob);
         return (BodyRotationController)(ReflectionUtils.set(mob, NMSFields.MOB_bodyRotationControl, controller) ? controller : (BodyRotationController)def.get());
      } else {
         return (BodyRotationController)def.get();
      }
   }

   public MoveController wrapMoveController(Entity entity, Supplier<MoveController> def) {
      net.minecraft.world.entity.Entity var4 = EntityUtils.nms(entity);
      if (var4 instanceof EntityInsentient) {
         EntityInsentient mob = (EntityInsentient)var4;
         MoveControlWrapper controller = new MoveControlWrapper(mob, mob.G());
         return (MoveController)(ReflectionUtils.set(mob, NMSFields.MOB_moveControl, controller) ? controller : (MoveController)def.get());
      } else {
         return (MoveController)def.get();
      }
   }

   public LookController wrapLookController(Entity entity, Supplier<LookController> def) {
      net.minecraft.world.entity.Entity var4 = EntityUtils.nms(entity);
      if (var4 instanceof EntityInsentient) {
         EntityInsentient mob = (EntityInsentient)var4;
         LookControlWrapper controller = new LookControlWrapper(mob, mob.E());
         return (LookController)(ReflectionUtils.set(mob, NMSFields.MOB_lookControl, controller) ? controller : (LookController)def.get());
      } else {
         return (LookController)def.get();
      }
   }

   public void wrapNavigation(Entity entity) {
      net.minecraft.world.entity.Entity var3 = EntityUtils.nms(entity);
      if (var3 instanceof EntityInsentient) {
         EntityInsentient mob = (EntityInsentient)var3;

         try {
            Field navField = ReflectionUtils.getField(NMSFields.MOB_navigation);
            NavigationAbstract oldNav = mob.J();
            Object newNav;
            if (oldNav instanceof NavigationSpider) {
               NavigationSpider wallClimberNavigation = (NavigationSpider)oldNav;
               newNav = new WallClimberNavigationWrapper(mob, wallClimberNavigation);
            } else if (oldNav instanceof Navigation) {
               Navigation groundPathNavigation = (Navigation)oldNav;
               newNav = new GroundNavigationWrapper(mob, groundPathNavigation);
            } else if (oldNav instanceof NavigationFlying) {
               NavigationFlying flyingPathNavigation = (NavigationFlying)oldNav;
               newNav = new FlyingNavigationWrapper(mob, flyingPathNavigation);
            } else if (oldNav instanceof NavigationGuardian) {
               newNav = new WaterBoundNavigationWrapper(mob);
            } else {
               if (!(oldNav instanceof AmphibiousPathNavigation)) {
                  EntityTypes var10000 = mob.ae();
                  TLogger.warn("Failed to create custom navigation for " + var10000 + ": " + mob.ct());
                  TLogger.warn("Reason: Navigation class type is " + oldNav.getClass().getSimpleName() + ".");
                  return;
               }

               AmphibiousPathNavigation amphibiousPathNavigation = (AmphibiousPathNavigation)oldNav;
               newNav = new AmphibiousNavigationWrapper(mob, amphibiousPathNavigation);
            }

            navField.set(mob, newNav);
            PathfinderGoalSelector goalSelector = (PathfinderGoalSelector)ReflectionUtils.getField(NMSFields.MOB_goalSelector).get(mob);
            RaceConditionUtil.wrapConmod(() -> {
               this.replaceNavigation(goalSelector, newNav);
            });
         } catch (IllegalAccessException var10) {
            var10.printStackTrace();
         }

      }
   }

   private void replaceNavigation(PathfinderGoalSelector goalSelector, NavigationAbstract newNav) {
      try {
         Iterator var3 = goalSelector.b().iterator();

         while(var3.hasNext()) {
            PathfinderGoalWrapped wrappedGoal = (PathfinderGoalWrapped)var3.next();
            PathfinderGoal goal = wrappedGoal.k();
            Field[] var6 = goal.getClass().getDeclaredFields();
            int var7 = var6.length;

            for(int var8 = 0; var8 < var7; ++var8) {
               Field field = var6[var8];
               field = ReflectionUtils.getField(goal.getClass(), field.getName());
               Object f = field.get(goal);
               if (f instanceof NavigationAbstract) {
                  field.set(goal, newNav);
               }
            }
         }
      } catch (IllegalAccessException var11) {
         var11.printStackTrace();
      }

   }

   public HitboxEntity createHitbox(Location location, ModelBone bone, SubHitbox subHitbox) {
      WorldServer level = ((CraftWorld)location.getWorld()).getHandle();
      HitboxEntityImpl entity = new HitboxEntityImpl(level, bone, subHitbox);
      entity.queueLocation((new Vector3f()).set(location.getX(), location.getY(), location.getZ()));
      entity.e(location.getX(), location.getY(), location.getZ());
      ModelEngineAPI.setRenderCanceled(entity.af(), true);
      Promise.start((Entity)entity.getBukkitEntity()).thenRunSync(() -> {
         level.b(entity);
         ModelEngineAPI.getInteractionTracker().addHitbox(entity);
      });
      return entity;
   }

   @Nullable
   public HitboxEntity castHitbox(Entity entity) {
      net.minecraft.world.entity.Entity var3 = EntityUtils.nms(entity);
      HitboxEntity var10000;
      if (var3 instanceof HitboxEntity) {
         HitboxEntity hitbox = (HitboxEntity)var3;
         var10000 = hitbox;
      } else {
         var10000 = null;
      }

      return var10000;
   }

   public boolean hurt(Entity entity, Object source, float amount) {
      if (source instanceof DamageSource) {
         DamageSource damageSource = (DamageSource)source;
         return EntityUtils.nms(entity).a(damageSource, amount);
      } else {
         throw new RuntimeException("Passed in source is not an NMS DamageSource.");
      }
   }

   public EntityHandler.InteractionResult interact(Entity entity, HumanEntity player, EquipmentSlot hand) {
      net.minecraft.world.entity.Entity var5 = EntityUtils.nms(entity);
      if (var5 instanceof EntityLiving) {
         EntityLiving livingEntity = (EntityLiving)var5;
         EnumInteractionResult result = livingEntity.a(((CraftPlayer)player).getHandle(), hand == EquipmentSlot.HAND ? EnumHand.a : EnumHand.b);
         EntityHandler.InteractionResult var10000;
         switch(result) {
         case a:
            var10000 = EntityHandler.InteractionResult.SUCCESS;
            break;
         case b:
            var10000 = EntityHandler.InteractionResult.CONSUME;
            break;
         case c:
            var10000 = EntityHandler.InteractionResult.CONSUME_PARTIAL;
            break;
         case d:
            var10000 = EntityHandler.InteractionResult.PASS;
            break;
         case e:
            var10000 = EntityHandler.InteractionResult.FAIL;
            break;
         default:
            throw new IncompatibleClassChangeError();
         }

         return var10000;
      } else {
         return EntityHandler.InteractionResult.FAIL;
      }
   }

   public void spawnDynamicHitbox(DynamicHitbox hitbox) {
      Vector location = (Vector)hitbox.getPositionTracker().get();
      final Packets.PacketSupplier pivotSpawn = NetworkUtils.createPivotSpawn(DynamicHitbox.getPivotId(), DynamicHitbox.getPivotUUID(), location.toVector3f().add(0.0F, -0.5202F, 0.0F));
      PacketDataSerializer pivotDataBuf = NetworkUtils.createByteBuf();
      pivotDataBuf.d(DynamicHitbox.getPivotId());
      EntityUtils.writeData(pivotDataBuf, 0, DataWatcherRegistry.a, (byte)32);
      EntityUtils.writeData(pivotDataBuf, 1, DataWatcherRegistry.b, Integer.MAX_VALUE);
      EntityUtils.writeData(pivotDataBuf, 8, DataWatcherRegistry.d, 0.0F);
      pivotDataBuf.writeByte(255);
      final PacketPlayOutEntityMetadata pivotData = new PacketPlayOutEntityMetadata(pivotDataBuf);
      final PacketPlayOutSpawnEntity hitboxSpawn = new PacketPlayOutSpawnEntity(DynamicHitbox.getHitboxId(), DynamicHitbox.getHitboxUUID(), location.getX(), location.getY() - 0.5202D, location.getZ(), 0.0F, 0.0F, EntityTypes.aL, 0, Vec3D.b, 0.0D);
      PacketDataSerializer hitboxDataBuf = NetworkUtils.createByteBuf();
      hitboxDataBuf.d(DynamicHitbox.getHitboxId());
      EntityUtils.writeData(hitboxDataBuf, 0, DataWatcherRegistry.a, (byte)32);
      EntityUtils.writeData(hitboxDataBuf, 1, DataWatcherRegistry.b, Integer.MAX_VALUE);
      EntityUtils.writeData(hitboxDataBuf, 16, DataWatcherRegistry.b, 2);
      hitboxDataBuf.writeByte(255);
      final PacketPlayOutEntityMetadata hitboxData = new PacketPlayOutEntityMetadata(hitboxDataBuf);
      PacketDataSerializer mountBuf = NetworkUtils.createByteBuf();
      mountBuf.d(DynamicHitbox.getPivotId());
      mountBuf.d(1);
      mountBuf.d(DynamicHitbox.getHitboxId());
      final PacketPlayOutMount mount = new PacketPlayOutMount(mountBuf);
      NetworkUtils.sendBundled(Set.of(hitbox.getPlayer()), new Packets() {
         {
            this.add(pivotSpawn);
            this.add(pivotData);
            this.add(hitboxSpawn);
            this.add(hitboxData);
            this.add(mount);
         }
      });
   }

   public void updateDynamicHitbox(DynamicHitbox hitbox) {
      Vector3f vector = ((Vector)hitbox.getPositionTracker().get()).toVector3f().add(0.0F, -0.5202F, 0.0F);
      NetworkUtils.send(hitbox.getPlayer(), NetworkUtils.createPivotTeleport(DynamicHitbox.getPivotId(), vector).supply(hitbox.getPlayer()));
   }

   public void destroyDynamicHitbox(DynamicHitbox hitbox) {
      PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(new int[]{DynamicHitbox.getHitboxId(), DynamicHitbox.getPivotId()});
      NetworkUtils.send((Player)hitbox.getPlayer(), destroy);
   }

   public void forceUseItem(Player player, EquipmentSlot hand) {
      ItemStack stack = player.getEquipment().getItem(hand);
      net.minecraft.world.item.ItemStack nmsStack = ((CraftItemStack)stack).handle;
      EntityPlayer nmsPlayer = (EntityPlayer)EntityUtils.nms(player);
      PlayerConnection connection = nmsPlayer.c;
      PacketPlayInBlockPlace useItemPacket = new PacketPlayInBlockPlace(hand == EquipmentSlot.HAND ? EnumHand.a : EnumHand.b, 0);
      useItemPacket.timestamp = System.currentTimeMillis();
      PatchedServerGamePacketListener.handleUseItem(useItemPacket, connection, (interactionResult) -> {
         if (nmsStack.s() != EnumAnimation.a && interactionResult == EnumInteractionResult.b) {
            PacketDataSerializer buf = NetworkUtils.createByteBuf();
            buf.d(player.getEntityId());
            EntityUtils.writeData(buf, 8, DataWatcherRegistry.a, (byte)(hand == EquipmentSlot.HAND ? 1 : 3));
            buf.writeByte(255);
            PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(buf);
            NetworkUtils.send((Player)player, packet);
            Item patt15792$temp = nmsStack.d();
            if (patt15792$temp instanceof InstrumentItem) {
               InstrumentItem instrumentItem = (InstrumentItem)patt15792$temp;
               Optional<? extends Holder<Instrument>> optional = (Optional)ReflectionUtils.call(instrumentItem, NMSMethods.INSTRUMENT_ITEM_getInstrument, nmsStack);
               optional.ifPresent((instrumentHolder) -> {
                  Instrument instrument = (Instrument)instrumentHolder.a();
                  Holder<SoundEffect> soundEvent = instrument.a();
                  float f = instrument.c() / 16.0F;
                  RandomSource random = (RandomSource)ReflectionUtils.get(nmsPlayer.dI(), NMSFields.LEVEL_threadSafeRandom);
                  PacketPlayOutEntitySound soundPacket = new PacketPlayOutEntitySound(soundEvent, SoundCategory.c, nmsPlayer, f, 1.0F, random.g());
                  NetworkUtils.send((Player)player, soundPacket);
               });
            }

         }
      });
   }

   public float getYRot(Entity entity) {
      return EntityUtils.nms(entity).dy();
   }

   public float getYHeadRot(Entity entity) {
      net.minecraft.world.entity.Entity nms = EntityUtils.nms(entity);
      if (nms instanceof EntityLiving) {
         EntityLiving livingEntity = (EntityLiving)nms;
         return livingEntity.cm();
      } else {
         return nms.dy();
      }
   }

   public float getXHeadRot(Entity entity) {
      return EntityUtils.nms(entity).dA();
   }

   public float getYBodyRot(Entity entity) {
      net.minecraft.world.entity.Entity nms = EntityUtils.nms(entity);
      if (nms instanceof EntityLiving) {
         EntityLiving livingEntity = (EntityLiving)nms;
         return livingEntity.aV;
      } else {
         return nms.dy();
      }
   }

   public void setYRot(Entity entity, float angle) {
      EntityUtils.nms(entity).a_(angle);
   }

   public void setYHeadRot(Entity entity, float angle) {
      net.minecraft.world.entity.Entity nms = EntityUtils.nms(entity);
      if (nms instanceof EntityLiving) {
         EntityLiving livingEntity = (EntityLiving)nms;
         livingEntity.n(angle);
      } else {
         nms.a_(angle);
      }

   }

   public void setXHeadRot(Entity entity, float angle) {
      EntityUtils.nms(entity).b_(angle);
   }

   public void setYBodyRot(Entity entity, float angle) {
      net.minecraft.world.entity.Entity nms = EntityUtils.nms(entity);
      if (nms instanceof EntityLiving) {
         EntityLiving livingEntity = (EntityLiving)nms;
         livingEntity.o(angle);
      } else {
         nms.a_(angle);
      }

   }

   public boolean isWalking(Entity entity) {
      net.minecraft.world.entity.Entity nms = EntityUtils.nms(entity);
      if (nms.ag < 1) {
         return false;
      } else {
         double dX = nms.dn() - nms.ab;
         double dZ = nms.dt() - nms.ad;
         return dX * dX + dZ * dZ > 2.500000277905201E-7D;
      }
   }

   public boolean isStrafing(Entity entity) {
      return false;
   }

   public boolean isJumping(Entity entity) {
      net.minecraft.world.entity.Entity var3 = EntityUtils.nms(entity);
      if (!(var3 instanceof EntityLiving)) {
         return false;
      } else {
         EntityLiving livingEntity = (EntityLiving)var3;
         Boolean flag = (Boolean)ReflectionUtils.get(livingEntity, NMSFields.LIVING_ENTITY_jumping, false);
         return flag != null && flag;
      }
   }

   public boolean isFlying(Entity entity) {
      return false;
   }

   public boolean isRemoved(Entity entity) {
      return EntityUtils.nms(entity).dD();
   }

   public int getGlowColor(Entity entity) {
      net.minecraft.world.entity.Entity nms = EntityUtils.nms(entity);
      return nms.k_();
   }

   public void setDeathTick(Entity entity, int tick) {
      net.minecraft.world.entity.Entity nms = EntityUtils.nms(entity);
      if (nms instanceof EntityLiving) {
         EntityLiving livingEntity = (EntityLiving)nms;
         livingEntity.aN = tick;
      }

   }

   public TrackedEntity wrapTrackedEntity(Entity entity) {
      WorldServer level = ((CraftWorld)entity.getWorld()).getHandle();
      Map<Integer, EntityTracker> map = level.k().a.K;
      EntityTracker tracker = (EntityTracker)map.get(entity.getEntityId());
      return (TrackedEntity)(tracker == null ? new TempTrackedEntity(entity) : new TrackedEntityImpl(entity, () -> {
         return (EntityTracker)map.get(entity.getEntityId());
      }, tracker));
   }

   public boolean shouldCull(Player player, Entity entity, @Nullable Hitbox cullHitbox) {
      CraftWorld world = (CraftWorld)player.getWorld();
      Vec3D start = CraftLocation.toVec3D(player.getEyeLocation());
      BoundingBox box = cullHitbox == null ? entity.getBoundingBox() : cullHitbox.createBoundingBox(entity.getLocation().toVector());
      if (!(box.getWidthX() >= this.forceRenderWidth) && !(box.getWidthZ() >= this.forceRenderWidth) && !(box.getHeight() >= this.forceRenderHeight)) {
         int minX = MathHelper.a(box.getMinX());
         int minY = MathHelper.a(box.getMinY());
         int minZ = MathHelper.a(box.getMinZ());
         int maxX = MathHelper.c(box.getMaxX()) - 1;
         int maxY = MathHelper.c(box.getMaxY()) - 1;
         int maxZ = MathHelper.c(box.getMaxZ()) - 1;
         EntityHandler.BoxRelToCam relX = EntityHandler.BoxRelToCam.from(minX, maxX, MathHelper.a(start.c));
         EntityHandler.BoxRelToCam relY = EntityHandler.BoxRelToCam.from(minY, maxY, MathHelper.a(start.d));
         EntityHandler.BoxRelToCam relZ = EntityHandler.BoxRelToCam.from(minZ, maxZ, MathHelper.a(start.e));
         if (relX == EntityHandler.BoxRelToCam.INSIDE && relY == EntityHandler.BoxRelToCam.INSIDE && relZ == EntityHandler.BoxRelToCam.INSIDE) {
            return false;
         } else {
            LinkedHashSet<Vec3D> points = new LinkedHashSet();

            for(int x = minX; x <= maxX; ++x) {
               byte xVisibleFace = 0;
               byte xVisibleFace = (byte)(xVisibleFace | (x == minX && relX == EntityHandler.BoxRelToCam.POSITIVE ? 1 : 0));
               xVisibleFace = (byte)(xVisibleFace | (x == maxX && relX == EntityHandler.BoxRelToCam.NEGATIVE ? 2 : 0));

               for(int y = minY; y <= maxY; ++y) {
                  byte yVisibleFace = (byte)(xVisibleFace | (y == minY && relY == EntityHandler.BoxRelToCam.POSITIVE ? 4 : 0));
                  yVisibleFace = (byte)(yVisibleFace | (y == maxY && relY == EntityHandler.BoxRelToCam.NEGATIVE ? 8 : 0));

                  for(int z = minZ; z <= maxZ; ++z) {
                     byte visibleFace = (byte)(yVisibleFace | (z == minZ && relZ == EntityHandler.BoxRelToCam.POSITIVE ? 16 : 0));
                     visibleFace = (byte)(visibleFace | (z == maxZ && relZ == EntityHandler.BoxRelToCam.NEGATIVE ? 32 : 0));
                     if (visibleFace != 0) {
                        Iterator var23 = EntityHandler.getPoints(visibleFace).iterator();

                        while(var23.hasNext()) {
                           EntityHandler.Point point = (EntityHandler.Point)var23.next();
                           points.add(new Vec3D((double)((float)x + point.x), (double)((float)y + point.y), (double)((float)z + point.z)));
                        }
                     }
                  }
               }
            }

            Iterator var25;
            Vec3D point;
            if (DebugToggle.isDebugging(DebugToggle.SHOW_CULL_POINTS)) {
               var25 = points.iterator();

               while(var25.hasNext()) {
                  point = (Vec3D)var25.next();
                  world.spawnParticle(Particle.REDSTONE, point.c, point.d, point.e, 1, new DustOptions(Color.RED, 0.2F));
               }
            }

            var25 = points.iterator();

            do {
               if (!var25.hasNext()) {
                  return true;
               }

               point = (Vec3D)var25.next();
            } while(!isVisible(world, start, point));

            return false;
         }
      } else {
         return false;
      }
   }

   private static boolean isVisible(CraftWorld world, Vec3D startPos, Vec3D endPos) {
      MovingObjectPositionBlock nmsHitResult = world.getHandle().a(new OcclusionClipContext(startPos, endPos));
      return nmsHitResult.c() == EnumMovingObjectType.a;
   }

   static {
      ENTITY_COUNTER = (AtomicInteger)ReflectionUtils.get(NMSFields.ENTITY_ENTITY_COUNTER);
   }
}
