package com.ticxo.modelengine.core.model.bone.behavior;

import com.ticxo.modelengine.api.model.bone.ModelBone;
import com.ticxo.modelengine.api.model.bone.behavior.AbstractBoneBehavior;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehaviorData;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehaviorType;
import com.ticxo.modelengine.api.model.bone.type.HeldItem;
import com.ticxo.modelengine.api.model.render.DisplayRenderer;
import com.ticxo.modelengine.api.utils.data.io.SavedData;
import java.util.function.Supplier;
import org.bukkit.entity.ItemDisplay.ItemDisplayTransform;
import org.bukkit.inventory.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class HeldItemImpl extends AbstractBoneBehavior<HeldItemImpl> implements HeldItem {
   private final Vector3f location = new Vector3f();
   private final Quaternionf rotation = new Quaternionf();
   private ItemDisplayTransform display;
   private HeldItem.ItemStackSupplier itemProvider;

   public HeldItemImpl(ModelBone bone, BoneBehaviorType<HeldItemImpl> type, BoneBehaviorData data) {
      super(bone, type, data);
      this.display = (ItemDisplayTransform)data.get("display");
   }

   public void onApply() {
      if (this.bone.getActiveModel().getModelRenderer() instanceof DisplayRenderer) {
         this.bone.setRenderer(true);
         this.bone.setModel(EMPTY);
      }

   }

   public void onFinalize() {
      float yaw = (180.0F - this.bone.getYaw()) * 0.017453292F;
      this.bone.getGlobalPosition().rotateY(yaw, this.location);
      this.bone.getGlobalLeftRotation().premul(this.rotation.rotationZYX(0.0F, yaw, 0.0F), this.rotation);
      if (this.itemProvider == null) {
         this.bone.setModel(EMPTY);
      } else {
         this.bone.setModel(this.itemProvider.supply());
      }

   }

   public void save(SavedData data) {
      if (this.itemProvider != null) {
         this.itemProvider.save().ifPresent((data1) -> {
            data.putData("supplier", data1);
         });
      }

   }

   public void load(SavedData data) {
      data.getData("supplier").ifPresent((data1) -> {
         String var2 = data1.getString("type", "");
         byte var3 = -1;
         switch(var2.hashCode()) {
         case -892481938:
            if (var2.equals("static")) {
               var3 = 0;
            }
            break;
         case 1076356494:
            if (var2.equals("equipment")) {
               var3 = 1;
            }
         }

         Object var10001;
         switch(var3) {
         case 0:
            var10001 = new HeldItem.StaticItemStackSupplier();
            break;
         case 1:
            var10001 = new HeldItem.EquipmentSupplier();
            break;
         default:
            var10001 = null;
         }

         this.itemProvider = (HeldItem.ItemStackSupplier)var10001;
         if (this.itemProvider != null) {
            this.itemProvider.load(data1);
         }

      });
   }

   public void clearItemProvider() {
      this.itemProvider = null;
   }

   public void setItemProvider(Supplier<ItemStack> stackSupplier) {
      this.setItemProvider((HeldItem.ItemStackSupplier)(new HeldItem.TemporaryItemStackSupplier(stackSupplier)));
   }

   public void setItemProvider(HeldItem.ItemStackSupplier stackSupplier) {
      this.itemProvider = stackSupplier;
   }

   public ItemStack getItem() {
      return this.bone.getModel();
   }

   public Vector3f getLocation() {
      return this.location;
   }

   public Quaternionf getRotation() {
      return this.rotation;
   }

   public ItemDisplayTransform getDisplay() {
      return this.display;
   }

   public void setDisplay(ItemDisplayTransform display) {
      this.display = display;
   }

   public HeldItem.ItemStackSupplier getItemProvider() {
      return this.itemProvider;
   }
}
