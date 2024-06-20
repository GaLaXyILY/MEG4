package com.ticxo.modelengine.core.model.bone.behavior;

import com.ticxo.modelengine.api.model.bone.ModelBone;
import com.ticxo.modelengine.api.model.bone.behavior.AbstractBoneBehavior;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehaviorData;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehaviorType;
import com.ticxo.modelengine.api.model.bone.type.Ghost;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class GhostImpl extends AbstractBoneBehavior<GhostImpl> implements Ghost {
   public GhostImpl(ModelBone bone, BoneBehaviorType<GhostImpl> type, BoneBehaviorData data) {
      super(bone, type, data);
   }

   public void onApply() {
      this.bone.setRenderer(true);
      this.bone.setModel(new ItemStack(Material.AIR));
   }

   public ItemStack getModel() {
      return this.bone.getModel();
   }

   public void setModel(int data) {
      this.bone.setModel(data);
   }

   public void setModel(ItemStack stack) {
      this.bone.setModel(stack);
   }
}
