package com.ticxo.modelengine.v1_20_R3.entity.hitbox;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.bone.ModelBone;
import com.ticxo.modelengine.api.model.bone.type.SubHitbox;
import com.ticxo.modelengine.api.nms.entity.EntityHandler;
import com.ticxo.modelengine.api.nms.entity.HitboxEntity;
import com.ticxo.modelengine.api.utils.ReflectionUtils;
import com.ticxo.modelengine.api.utils.math.OrientedBoundingBox;
import com.ticxo.modelengine.v1_20_R3.NMSFields;
import com.ticxo.modelengine.v1_20_R3.entity.EntityUtils;
import java.util.Iterator;
import java.util.UUID;
import net.minecraft.core.NonNullList;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.EnumMainHand;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_20_R3.util.CraftLocation;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class HitboxEntityImpl extends EntityLiving implements HitboxEntity {
   private final NonNullList<ItemStack> handItems;
   private final NonNullList<ItemStack> armorItems;
   private final ModelBone bone;
   private final SubHitbox subHitbox;
   private OBB obb;
   private Vector3f location;
   private boolean markRemoved;

   public HitboxEntityImpl(World world, @NotNull ModelBone bone, @NotNull SubHitbox subHitbox) {
      super(EntityTypes.aJ, world);
      this.handItems = NonNullList.a(2, ItemStack.f);
      this.armorItems = NonNullList.a(4, ItemStack.f);
      this.bone = bone;
      this.subHitbox = subHitbox;
      this.m(true);
      this.e(true);
      this.af = true;
   }

   public EnumMainHand fm() {
      return EnumMainHand.b;
   }

   public Iterable<ItemStack> bL() {
      return this.armorItems;
   }

   public ItemStack c(EnumItemSlot slot) {
      ItemStack var10000;
      switch(slot.a()) {
      case a:
         var10000 = (ItemStack)this.handItems.get(slot.b());
         break;
      case b:
         var10000 = (ItemStack)this.armorItems.get(slot.b());
         break;
      default:
         throw new IncompatibleClassChangeError();
      }

      return var10000;
   }

   public void a(EnumItemSlot slot, ItemStack stack) {
      this.e(stack);
      switch(slot.a()) {
      case a:
         this.a(slot, (ItemStack)this.handItems.set(slot.b(), stack), stack);
         break;
      case b:
         this.a(slot, (ItemStack)this.armorItems.set(slot.b(), stack), stack);
      }

   }

   public boolean ce() {
      return true;
   }

   public void g(Entity entity) {
   }

   @NotNull
   protected AxisAlignedBB aq() {
      if (this.subHitbox == null) {
         return super.aq();
      } else {
         Vector3f pos = this.subHitbox.getLocation();
         Vector3f dim = this.subHitbox.getDimension();
         float halfX;
         float halfZ;
         if (this.subHitbox.isOBB()) {
            halfX = dim.x * 0.5F;
            halfZ = dim.y * 0.5F;
            float halfZ = dim.z * 0.5F;
            Quaternionf rot = this.subHitbox.getRotation();
            float yaw = this.subHitbox.getYaw();
            this.obb = new OBB((double)(pos.x - halfX), (double)(pos.y - halfZ), (double)(pos.z - halfZ), (double)(pos.x + halfX), (double)(pos.y + halfZ), (double)(pos.z + halfZ), rot, yaw);
            return this.obb;
         } else {
            halfX = dim.x * 0.5F;
            halfZ = dim.z * 0.5F;
            return new AxisAlignedBB((double)(pos.x - halfX), (double)pos.y, (double)(pos.z - halfZ), (double)(pos.x + halfX), (double)(pos.y + dim.y), (double)(pos.z + halfZ));
         }
      }
   }

   public void l() {
      if (this.markRemoved) {
         this.am();
      } else {
         super.l();
         if (this.bone != null && this.subHitbox != null) {
            if (!this.bone.getActiveModel().getModeledEntity().getBase().isAlive()) {
               this.am();
            } else if (this.location.isFinite()) {
               Vec3D vec = new Vec3D(this.location);
               this.b(vec);
               Iterator var2 = this.subHitbox.getBoundEntities().values().iterator();

               while(var2.hasNext()) {
                  org.bukkit.entity.Entity entity = (org.bukkit.entity.Entity)var2.next();
                  EntityUtils.nms(entity).b(vec);
               }

            }
         } else {
            this.am();
         }
      }
   }

   public boolean aY() {
      return true;
   }

   protected void M_() {
   }

   protected void D(Entity entity) {
   }

   public boolean bu() {
      return false;
   }

   public boolean a(DamageSource source, float amount) {
      Iterator var3 = this.subHitbox.getBoundEntities().values().iterator();

      while(var3.hasNext()) {
         org.bukkit.entity.Entity entity = (org.bukkit.entity.Entity)var3.next();
         EntityUtils.nms(entity).a(source, amount);
      }

      if (this.subHitbox.getDamageMultiplier() <= 1.0E-5F) {
         return false;
      } else {
         Entity var5 = source.d();
         CraftHumanEntity var10000;
         if (var5 instanceof EntityHuman) {
            EntityHuman player = (EntityHuman)var5;
            var10000 = player.getBukkitEntity();
         } else {
            var10000 = null;
         }

         CraftHumanEntity cause = var10000;
         return this.bone.getActiveModel().getModeledEntity().getBase().hurt(cause, source, amount * this.subHitbox.getDamageMultiplier());
      }
   }

   @NotNull
   public EnumInteractionResult a(EntityHuman player, EnumHand hand) {
      CraftHumanEntity var4 = player.getBukkitEntity();
      if (var4 instanceof Player) {
         Player craftPlayer = (Player)var4;
         Iterator var8 = this.subHitbox.getBoundEntities().values().iterator();

         while(var8.hasNext()) {
            org.bukkit.entity.Entity entity = (org.bukkit.entity.Entity)var8.next();
            PlayerInteractAtEntityEvent event = new PlayerInteractAtEntityEvent(craftPlayer, entity, new Vector(0, 0, 0), hand == EnumHand.b ? EquipmentSlot.OFF_HAND : EquipmentSlot.HAND);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
               EntityUtils.nms(entity).a(player, hand);
            }
         }
      }

      if (this.subHitbox.getDamageMultiplier() <= 1.0E-5F) {
         return EnumInteractionResult.d;
      } else {
         EntityHandler.InteractionResult result = this.bone.getActiveModel().getModeledEntity().getBase().interact(player.getBukkitEntity(), hand == EnumHand.a ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND);
         EnumInteractionResult var10000;
         switch(result) {
         case SUCCESS:
            var10000 = EnumInteractionResult.a;
            break;
         case CONSUME:
            var10000 = EnumInteractionResult.b;
            break;
         case CONSUME_PARTIAL:
            var10000 = EnumInteractionResult.c;
            break;
         case PASS:
            var10000 = EnumInteractionResult.d;
            break;
         case FAIL:
            var10000 = EnumInteractionResult.e;
            break;
         default:
            throw new IncompatibleClassChangeError();
         }

         return var10000;
      }
   }

   public boolean dK() {
      return false;
   }

   public CraftEntity getBukkitEntity() {
      CraftEntity bukkitEntity = this.getBukkitEntityR();
      if (bukkitEntity == null) {
         synchronized(this) {
            CraftEntity bukkitEntity = new CraftLivingEntity(this.dM().getCraftServer(), this);
            this.setBukkitEntityR(bukkitEntity);
            return bukkitEntity;
         }
      } else {
         return bukkitEntity;
      }
   }

   private void setBukkitEntityR(CraftEntity craftEntity) {
      ReflectionUtils.set(this, NMSFields.ENTITY_bukkitEntity, craftEntity);
   }

   private CraftEntity getBukkitEntityR() {
      return (CraftEntity)ReflectionUtils.get(this, NMSFields.ENTITY_bukkitEntity);
   }

   protected int m(int air) {
      return air;
   }

   protected int n(int air) {
      return air;
   }

   public void a(RemovalReason reason) {
      super.a(reason);
      ModelEngineAPI.setRenderCanceled(this.aj(), false);
      ModelEngineAPI.getInteractionTracker().removeHitbox(this.getEntityId());
   }

   public int getEntityId() {
      return this.aj();
   }

   public UUID getUniqueId() {
      return this.cw();
   }

   public void queueLocation(Vector3f location) {
      this.location = location;
   }

   public Location getLocation() {
      return CraftLocation.toBukkit(this.dk(), this.dM().getWorld(), this.getBukkitYaw(), this.dE());
   }

   @Nullable
   public OrientedBoundingBox getOrientedBoundingBox() {
      return this.obb == null ? null : this.obb.getBukkitOBB();
   }

   public void markRemoved() {
      this.markRemoved = true;
      ModelEngineAPI.setRenderCanceled(this.aj(), false);
      ModelEngineAPI.getInteractionTracker().removeHitbox(this.getEntityId());
   }

   public ModelBone getBone() {
      return this.bone;
   }

   public SubHitbox getSubHitbox() {
      return this.subHitbox;
   }
}
