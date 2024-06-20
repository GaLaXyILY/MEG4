package com.ticxo.modelengine.core.mythic.mechanics.bone;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.model.bone.BoneBehaviorTypes;
import com.ticxo.modelengine.api.model.bone.ModelBone;
import com.ticxo.modelengine.api.model.bone.type.PlayerLimb;
import com.ticxo.modelengine.api.utils.MojangAPI;
import com.ticxo.modelengine.api.utils.math.TMath;
import com.ticxo.modelengine.core.mythic.MythicUtils;
import com.ticxo.modelengine.core.mythic.utils.MythicMechanic;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.profile.PlayerTextures;
import org.bukkit.profile.PlayerTextures.SkinModel;

@MythicMechanic(
   name = "modelplayerskin",
   aliases = {"modelskin"}
)
public class ModelSkinMechanic implements ITargetedEntitySkill {
   private final PlaceholderString modelId;
   private final PlaceholderString pbone;
   private final PlaceholderString username;
   private final PlaceholderString uuid;
   private final PlaceholderString skin;
   private final Boolean isSlim;

   public ModelSkinMechanic(MythicLineConfig mlc) {
      this.modelId = mlc.getPlaceholderString(new String[]{"m", "mid", "model", "modelid"}, (String)null, new String[0]);
      this.pbone = mlc.getPlaceholderString(new String[]{"p", "pbone", "limbs"}, (String)null, new String[0]);
      this.uuid = mlc.getPlaceholderString(new String[]{"uuid"}, (String)null, new String[0]);
      this.username = mlc.getPlaceholderString(new String[]{"user", "name", "username"}, (String)null, new String[0]);
      this.skin = mlc.getPlaceholderString(new String[]{"s", "skin"}, (String)null, new String[0]);
      String slim = mlc.getString(new String[]{"slim"}, (String)null, new String[0]);
      this.isSlim = slim == null ? null : Boolean.parseBoolean(slim);
   }

   public SkillResult castAtEntity(SkillMetadata meta, AbstractEntity target) {
      AbstractEntity caster = meta.getCaster().getEntity();
      ModeledEntity model = ModelEngineAPI.getModeledEntity(caster.getUniqueId());
      if (model == null) {
         return SkillResult.CONDITION_FAILED;
      } else {
         String username = MythicUtils.getOrNull(this.username, meta, target);
         if (username != null) {
            MojangAPI.getUUIDFromUsernamePromise(username).thenApplyAsync(MojangAPI::fromUUID).thenAcceptAsync((profilex) -> {
               this.apply(model, profilex, meta, target);
            });
            return SkillResult.SUCCESS;
         } else {
            String uuidString = MythicUtils.getOrNull(this.uuid, meta, target);
            if (uuidString != null) {
               UUID uuid = TMath.parseUUID(uuidString);
               MojangAPI.fromUUIDPromise(uuid).thenAcceptAsync((profilex) -> {
                  this.apply(model, profilex, meta, target);
               });
               return SkillResult.SUCCESS;
            } else {
               String skin = MythicUtils.getOrNull(this.skin, meta, target);
               PlayerProfile profile;
               if (skin == null) {
                  Entity var10 = target.getBukkitEntity();
                  if (!(var10 instanceof Player)) {
                     return SkillResult.INVALID_TARGET;
                  }

                  Player player = (Player)var10;
                  profile = player.getPlayerProfile();
               } else {
                  profile = MojangAPI.fromBase64(skin);
               }

               return this.apply(model, profile, meta, target);
            }
         }
      }
   }

   public boolean getTargetsCreatives() {
      return true;
   }

   private SkillResult apply(ModeledEntity model, PlayerProfile profile, SkillMetadata meta, AbstractEntity target) {
      PlayerProfile finalProfile = this.forceModel(profile);
      String modelId = MythicUtils.getOrNullLowercase(this.modelId, meta, target);
      String pbone = MythicUtils.getOrNullLowercase(this.pbone, meta, target);
      if (modelId == null && pbone == null) {
         Iterator var15 = model.getModels().values().iterator();

         while(var15.hasNext()) {
            ActiveModel activeModel = (ActiveModel)var15.next();
            Iterator var13 = activeModel.getBones().values().iterator();

            while(var13.hasNext()) {
               ModelBone modelBone = (ModelBone)var13.next();
               modelBone.getBoneBehavior(BoneBehaviorTypes.PLAYER_LIMB).ifPresent((playerLimb) -> {
                  ((PlayerLimb)playerLimb).setTexture(finalProfile);
               });
            }
         }

         return SkillResult.SUCCESS;
      } else {
         List<String> limbs = pbone == null ? null : List.of(pbone.split(","));
         if (modelId == null) {
            Iterator var9 = model.getModels().values().iterator();

            while(var9.hasNext()) {
               ActiveModel activeModel = (ActiveModel)var9.next();
               Iterator var11 = limbs.iterator();

               while(var11.hasNext()) {
                  String limb = (String)var11.next();
                  activeModel.getBone(limb).flatMap((modelBonex) -> {
                     return modelBonex.getBoneBehavior(BoneBehaviorTypes.PLAYER_LIMB);
                  }).ifPresent((playerLimb) -> {
                     ((PlayerLimb)playerLimb).setTexture(finalProfile);
                  });
               }
            }
         } else {
            model.getModel(modelId).ifPresent((activeModelx) -> {
               Iterator var3;
               if (limbs == null) {
                  var3 = activeModelx.getBones().values().iterator();

                  while(var3.hasNext()) {
                     ModelBone modelBone = (ModelBone)var3.next();
                     modelBone.getBoneBehavior(BoneBehaviorTypes.PLAYER_LIMB).ifPresent((playerLimb) -> {
                        ((PlayerLimb)playerLimb).setTexture(finalProfile);
                     });
                  }
               } else {
                  var3 = limbs.iterator();

                  while(var3.hasNext()) {
                     String seat = (String)var3.next();
                     activeModelx.getBone(seat).flatMap((modelBonex) -> {
                        return modelBonex.getBoneBehavior(BoneBehaviorTypes.PLAYER_LIMB);
                     }).ifPresent((playerLimb) -> {
                        ((PlayerLimb)playerLimb).setTexture(finalProfile);
                     });
                  }
               }

            });
         }

         return SkillResult.SUCCESS;
      }
   }

   private PlayerProfile forceModel(PlayerProfile profile) {
      if (this.isSlim == null) {
         return profile;
      } else {
         PlayerTextures texture = profile.getTextures();
         SkinModel model = this.isSlim ? SkinModel.SLIM : SkinModel.CLASSIC;
         if (model != texture.getSkinModel()) {
            texture.setSkin(texture.getSkin(), model);
            profile.setTextures(texture);
         }

         return profile;
      }
   }
}
