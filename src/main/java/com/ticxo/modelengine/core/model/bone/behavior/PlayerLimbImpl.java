package com.ticxo.modelengine.core.model.bone.behavior;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.ticxo.modelengine.api.model.bone.ModelBone;
import com.ticxo.modelengine.api.model.bone.behavior.AbstractBoneBehavior;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehaviorData;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehaviorType;
import com.ticxo.modelengine.api.model.bone.type.PlayerLimb;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.Nullable;

public class PlayerLimbImpl extends AbstractBoneBehavior<PlayerLimbImpl> implements PlayerLimb {
   private final PlayerLimb.Limb limbType;
   private boolean isSlim;

   public PlayerLimbImpl(ModelBone bone, BoneBehaviorType<PlayerLimbImpl> type, BoneBehaviorData data) {
      super(bone, type, data);
      this.limbType = (PlayerLimb.Limb)data.get("limb");
   }

   public void onApply() {
      this.bone.setRenderer(true);
      this.bone.setModel(new ItemStack(Material.AIR));
   }

   public void onFinalize() {
      super.onFinalize();
      this.bone.getGlobalPosition().add(0.0F, this.isSlim ? this.limbType.slimYOffset : this.limbType.defaultYOffset, 0.0F);
   }

   public void setTexture(@Nullable Player player) {
      if (player == null) {
         this.bone.setModel(new ItemStack(Material.AIR));
      } else {
         this.setTexture(player.getPlayerProfile());
      }
   }

   public void setTexture(@Nullable PlayerProfile profile) {
      if (profile == null) {
         this.bone.setModel(new ItemStack(Material.AIR));
      } else {
         ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
         ItemMeta meta = playerHead.getItemMeta();
         ((SkullMeta)meta).setPlayerProfile(profile);
         Integer var10001;
         switch(profile.getTextures().getSkinModel()) {
         case CLASSIC:
            this.isSlim = false;
            var10001 = this.limbType.defaultId;
            break;
         case SLIM:
            this.isSlim = true;
            var10001 = this.limbType.slimId;
            break;
         default:
            throw new IncompatibleClassChangeError();
         }

         meta.setCustomModelData(var10001);
         playerHead.setItemMeta(meta);
         this.bone.setModel(playerHead);
      }
   }

   public PlayerLimb.Limb getLimbType() {
      return this.limbType;
   }
}
