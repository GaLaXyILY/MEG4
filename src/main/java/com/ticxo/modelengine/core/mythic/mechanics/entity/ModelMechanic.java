package com.ticxo.modelengine.core.mythic.mechanics.entity;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.ServerInfo;
import com.ticxo.modelengine.api.animation.handler.AnimationHandler;
import com.ticxo.modelengine.api.generator.blueprint.ModelBlueprint;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.model.bone.BoneBehaviorTypes;
import com.ticxo.modelengine.api.model.bone.manager.MountData;
import com.ticxo.modelengine.api.model.bone.manager.MountManager;
import com.ticxo.modelengine.api.model.bone.type.NameTag;
import com.ticxo.modelengine.core.animation.handler.PriorityHandler;
import com.ticxo.modelengine.core.animation.handler.StateMachineHandler;
import com.ticxo.modelengine.core.mythic.MythicUtils;
import com.ticxo.modelengine.core.mythic.utils.MythicMechanic;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import io.lumine.mythic.api.skills.placeholders.PlaceholderInt;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import java.util.Iterator;
import java.util.function.Function;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Entity;

@MythicMechanic(
   name = "model",
   aliases = {}
)
public class ModelMechanic implements ITargetedEntitySkill {
   private final PlaceholderString modelId;
   private final PlaceholderString nametag;
   private final boolean hitbox;
   private final boolean remove;
   private final boolean killOwner;
   private final boolean invisible;
   private final boolean doDamageTint;
   private final boolean canDrive;
   private final boolean canRide;
   private final boolean lockPitch;
   private final boolean lockYaw;
   private final boolean initRenderer;
   private final boolean showHitbox;
   private final boolean showShadow;
   private final PlaceholderDouble stepHeight;
   private final PlaceholderDouble scale;
   private final PlaceholderDouble hitboxScale;
   private final PlaceholderInt viewRadius;
   private final boolean useStateMachine;
   private final boolean syncBodyYaw;
   private final boolean shouldSave;

   public ModelMechanic(MythicLineConfig mlc) {
      this.modelId = mlc.getPlaceholderString(new String[]{"m", "mid", "model", "modelid"}, (String)null, new String[0]);
      this.hitbox = mlc.getBoolean(new String[]{"h", "hitbox"}, true);
      this.remove = mlc.getBoolean(new String[]{"r", "remove"}, false);
      this.killOwner = mlc.getBoolean(new String[]{"ko", "killowner"}, false);
      this.invisible = mlc.getBoolean(new String[]{"i", "invis", "invisible"}, true);
      this.doDamageTint = mlc.getBoolean(new String[]{"d", "tint", "damagetint"}, true);
      this.nametag = mlc.getPlaceholderString(new String[]{"n", "name", "nametag"}, (String)null, new String[0]);
      this.canDrive = mlc.getBoolean(new String[]{"drive"}, false);
      this.canRide = mlc.getBoolean(new String[]{"ride"}, false);
      this.lockPitch = mlc.getBoolean(new String[]{"lp", "lpitch", "lockpitch"}, false);
      this.lockYaw = mlc.getBoolean(new String[]{"ly", "lyaw", "lockyaw"}, false);
      this.stepHeight = mlc.getPlaceholderDouble(new String[]{"s", "step"}, 0.5D, new String[0]);
      this.viewRadius = mlc.getPlaceholderInteger(new String[]{"rad", "radius"}, 0, new String[0]);
      this.scale = mlc.getPlaceholderDouble(new String[]{"scale"}, 1.0D, new String[0]);
      this.hitboxScale = mlc.getPlaceholderDouble(new String[]{"hitboxscale"}, this.scale, new String[0]);
      this.useStateMachine = mlc.getBoolean(new String[]{"usm", "state", "statemachine", "usestatemachine"}, false);
      this.initRenderer = mlc.getBoolean(new String[]{"init", "initrender"}, true);
      this.showHitbox = mlc.getBoolean(new String[]{"showhitbox"}, true);
      this.showShadow = mlc.getBoolean(new String[]{"showshadow"}, true);
      this.syncBodyYaw = mlc.getBoolean(new String[]{"syncbody"}, true);
      this.shouldSave = mlc.getBoolean(new String[]{"save"}, false);
   }

   public SkillResult castAtEntity(SkillMetadata meta, AbstractEntity target) {
      return this.remove ? this.removeModel(meta, target) : this.addModel(meta, target);
   }

   private SkillResult removeModel(SkillMetadata meta, AbstractEntity target) {
      ModeledEntity model = ModelEngineAPI.getModeledEntity(target.getUniqueId());
      if (model == null) {
         return SkillResult.CONDITION_FAILED;
      } else {
         String id = MythicUtils.getOrNullLowercase(this.modelId, meta, target);
         if (id == null) {
            Iterator var5 = model.getModels().values().iterator();

            while(var5.hasNext()) {
               ActiveModel activeModel = (ActiveModel)var5.next();
               model.removeModel(activeModel.getBlueprint().getName()).ifPresent(ActiveModel::destroy);
            }
         } else {
            model.removeModel(id).ifPresent(ActiveModel::destroy);
         }

         if (this.killOwner) {
            target.remove();
            return SkillResult.SUCCESS;
         } else {
            if (model.getModels().isEmpty()) {
               model.setBaseEntityVisible(true);
               ModelEngineAPI.removeModeledEntity(target.getUniqueId());
            } else {
               model.setBaseEntityVisible(!this.invisible);
            }

            return SkillResult.SUCCESS;
         }
      }
   }

   private SkillResult addModel(SkillMetadata meta, AbstractEntity target) {
      String modelId = MythicUtils.getOrNullLowercase(this.modelId, meta, target);
      ModelBlueprint blueprint = MythicUtils.getBlueprintOrNull(modelId);
      if (blueprint == null) {
         return SkillResult.INVALID_CONFIG;
      } else {
         Entity bukkitTarget = target.getBukkitEntity();
         ModeledEntity model = ModelEngineAPI.getOrCreateModeledEntity(bukkitTarget);
         if (model.isInitialized() && model.getModel(modelId).isPresent()) {
            return SkillResult.CONDITION_FAILED;
         } else {
            model.restore();
            double stepHeight = this.stepHeight.get(meta, target);
            int viewRadius = this.viewRadius.get(meta, target);
            double scale = this.scale.get(meta, target);
            double hitboxScale = this.hitboxScale.get(meta, target);
            model.queuePostInitTask(() -> {
               model.setSaved(this.shouldSave);
               model.setBaseEntityVisible(!this.invisible);
               model.getBase().setMaxStepHeight(stepHeight);
               if (viewRadius > 0) {
                  model.getBase().setRenderRadius(viewRadius);
               }

               if (this.syncBodyYaw) {
                  model.getBase().getBodyRotationController().setYBodyRot(bukkitTarget.getLocation().getYaw());
               }

               if (model.getModel(modelId).isEmpty()) {
                  ActiveModel activeModel = ModelEngineAPI.createActiveModel((String)modelId, (Function)null, (am) -> {
                     return (AnimationHandler)(this.useStateMachine ? new StateMachineHandler(am) : new PriorityHandler(am));
                  });
                  activeModel.setScale(scale);
                  activeModel.setHitboxScale(hitboxScale);
                  activeModel.setCanHurt(this.doDamageTint);
                  activeModel.setLockPitch(this.lockPitch);
                  activeModel.setLockYaw(this.lockYaw);
                  activeModel.setAutoRendererInitialization(this.initRenderer);
                  activeModel.setHitboxVisible(this.showHitbox);
                  activeModel.setShadowVisible(this.showShadow);
                  model.addModel(activeModel, this.hitbox).ifPresent(ActiveModel::destroy);
                  activeModel.getMountManager().ifPresent((mountManager) -> {
                     ((MountManager)mountManager).setCanRide(this.canRide);
                     ((MountManager)mountManager).setCanDrive(this.canDrive);
                     ((MountData)model.getMountData()).setMainMountManager(mountManager);
                  });
                  String nametag = MythicUtils.getOrNullLowercase(this.nametag, meta, target);
                  if (nametag != null) {
                     activeModel.getBone(nametag).flatMap((modelBone) -> {
                        return modelBone.getBoneBehavior(BoneBehaviorTypes.NAMETAG);
                     }).ifPresent((nameTag) -> {
                        ((NameTag)nameTag).setComponentSupplier(() -> {
                           if (ServerInfo.IS_PAPER) {
                              return bukkitTarget.customName();
                           } else {
                              String name = bukkitTarget.getCustomName();
                              return name == null ? null : LegacyComponentSerializer.legacyAmpersand().deserialize(name);
                           }
                        });
                        ((NameTag)nameTag).setVisible(true);
                     });
                  }
               }

            });
            return SkillResult.SUCCESS;
         }
      }
   }
}
