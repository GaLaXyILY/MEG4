package com.ticxo.modelengine.core.vfx;

import com.ticxo.modelengine.api.entity.BaseEntity;
import com.ticxo.modelengine.api.generator.BaseItemEnum;
import com.ticxo.modelengine.api.utils.data.tracker.DataTracker;
import com.ticxo.modelengine.api.vfx.VFX;
import com.ticxo.modelengine.api.vfx.render.VFXRenderer;
import com.ticxo.modelengine.core.vfx.render.VFXDisplayRendererImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class VFXImpl implements VFX {
   private final BaseEntity<?> base;
   private final VFXRenderer renderer;
   private final Vector3f modelScale = new Vector3f();
   private final boolean initialized;
   private final List<Runnable> queuedTask = new ArrayList();
   private final DataTracker<ItemStack> modelTracker;
   private boolean isBaseEntityVisible;
   private boolean destroyed;
   private boolean removed;
   private float yaw;
   private float pitch;
   private Vector origin;
   private Vector3f position;
   private Vector3f rotation;
   private Vector3f scale;
   private Color color;
   private boolean enchanted;
   private boolean visible;

   public VFXImpl(@NotNull BaseEntity<?> base, @Nullable Function<VFX, VFXRenderer> rendererSupplier, @Nullable Consumer<VFX> consumer) {
      this.modelTracker = new DataTracker(new ItemStack(Material.AIR));
      this.isBaseEntityVisible = true;
      this.origin = new Vector();
      this.position = new Vector3f();
      this.rotation = new Vector3f();
      this.scale = new Vector3f(1.0F);
      this.visible = true;
      this.base = base;
      this.registerSelf();
      this.setOrigin(base.getLocation().toVector());
      VFXRenderer renderer = rendererSupplier == null ? new VFXDisplayRendererImpl(this) : (VFXRenderer)rendererSupplier.apply(this);
      this.renderer = (VFXRenderer)(renderer == null ? new VFXDisplayRendererImpl(this) : renderer);
      if (consumer != null) {
         consumer.accept(this);
      }

      this.renderer.initialize();
      synchronized(this.queuedTask) {
         this.queuedTask.forEach(Runnable::run);
         this.initialized = true;
      }
   }

   public boolean tick() {
      if (!this.initialized) {
         return true;
      } else {
         this.setOrigin(this.base.getLocation().toVector());
         this.setYaw(this.base.getYHeadRot());
         this.setPitch(this.base.getXHeadRot());
         this.renderer.readVFXData();
         return !this.removed && !this.base.isRemoved();
      }
   }

   public void destroy() {
      this.destroyed = true;
   }

   public void markRemoved() {
      this.removed = true;
   }

   public void queuePostInitTask(Runnable runnable) {
      synchronized(this.queuedTask) {
         if (this.initialized) {
            runnable.run();
         } else {
            this.queuedTask.add(runnable);
         }

      }
   }

   public void setBaseEntityVisible(boolean flag) {
      if (this.isBaseEntityVisible() != flag) {
         this.isBaseEntityVisible = flag;
         this.base.setVisible(flag);
      }
   }

   public void setModelScale(int scale) {
      this.modelScale.set((float)scale);
   }

   public ItemStack getModel() {
      return (ItemStack)this.modelTracker.get();
   }

   public void setModel(ItemStack stack) {
      this.modelTracker.set(stack);
   }

   public void setColor(Color color) {
      this.color = color;
      ItemStack model = (ItemStack)this.modelTracker.get();
      BaseItemEnum base = BaseItemEnum.fromMaterial(model.getType());
      if (base != null) {
         ItemMeta meta = model.getItemMeta();
         base.color(meta, color);
         model.setItemMeta(meta);
         this.modelTracker.markDirty();
      }
   }

   public void setEnchanted(boolean flag) {
      if (this.isEnchanted() != flag) {
         this.enchanted = flag;
         ItemStack model = (ItemStack)this.modelTracker.get();
         if (flag) {
            model.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);
         } else {
            model.removeEnchantment(Enchantment.VANISHING_CURSE);
         }

         this.modelTracker.markDirty();
      }
   }

   public BaseEntity<?> getBase() {
      return this.base;
   }

   public VFXRenderer getRenderer() {
      return this.renderer;
   }

   public Vector3f getModelScale() {
      return this.modelScale;
   }

   public boolean isInitialized() {
      return this.initialized;
   }

   public List<Runnable> getQueuedTask() {
      return this.queuedTask;
   }

   public DataTracker<ItemStack> getModelTracker() {
      return this.modelTracker;
   }

   public boolean isBaseEntityVisible() {
      return this.isBaseEntityVisible;
   }

   public boolean isDestroyed() {
      return this.destroyed;
   }

   public boolean isRemoved() {
      return this.removed;
   }

   public float getYaw() {
      return this.yaw;
   }

   public float getPitch() {
      return this.pitch;
   }

   public Vector getOrigin() {
      return this.origin;
   }

   public Vector3f getPosition() {
      return this.position;
   }

   public Vector3f getRotation() {
      return this.rotation;
   }

   public Vector3f getScale() {
      return this.scale;
   }

   public Color getColor() {
      return this.color;
   }

   public boolean isEnchanted() {
      return this.enchanted;
   }

   public boolean isVisible() {
      return this.visible;
   }

   public void setYaw(float yaw) {
      this.yaw = yaw;
   }

   public void setPitch(float pitch) {
      this.pitch = pitch;
   }

   public void setOrigin(Vector origin) {
      this.origin = origin;
   }

   public void setPosition(Vector3f position) {
      this.position = position;
   }

   public void setRotation(Vector3f rotation) {
      this.rotation = rotation;
   }

   public void setScale(Vector3f scale) {
      this.scale = scale;
   }

   public void setVisible(boolean visible) {
      this.visible = visible;
   }
}
