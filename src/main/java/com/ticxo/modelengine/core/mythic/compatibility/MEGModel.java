package com.ticxo.modelengine.core.mythic.compatibility;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.ServerInfo;
import com.ticxo.modelengine.api.entity.BukkitEntity;
import com.ticxo.modelengine.api.generator.blueprint.ModelBlueprint;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.model.bone.BoneBehaviorTypes;
import com.ticxo.modelengine.api.model.bone.manager.MountManager;
import com.ticxo.modelengine.api.model.bone.type.NameTag;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicConfig;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.core.logging.MythicLogger;
import io.lumine.mythic.core.mobs.model.MobModel;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Entity;

public class MEGModel extends MobModel {
   private final String id;
   private String nameplate = "";
   private double stepHeight = 0.5D;
   private int viewRadius = 0;
   private double scale = 1.0D;
   private double hitboxScale = 1.0D;
   private boolean useHitbox = true;
   private boolean invisible = true;
   private boolean doDamageTint = true;
   private boolean canDrive = false;
   private boolean canRide = false;
   private boolean lockPitch = false;
   private boolean lockYaw = false;

   public MEGModel(MythicMob baseMob, MythicConfig config) {
      super(baseMob, config);
      if (config.isConfigurationSection("Model")) {
         this.id = config.getString("Model.Id", (String)null);
         this.nameplate = config.getString("Model.Nameplate", (String)null);
         this.stepHeight = config.getDouble("Model.Step", 0.5D);
         this.viewRadius = config.getInteger("Model.ViewRadius", 0);
         this.useHitbox = config.getBoolean("Model.Hitbox", true);
         this.invisible = config.getBoolean("Model.Invisible", true);
         this.doDamageTint = config.getBoolean("Model.DamageTint", true);
         this.canDrive = config.getBoolean("Model.Drive", false);
         this.canRide = config.getBoolean("Model.Ride", false);
         this.lockPitch = config.getBoolean("Model.LockPitch", false);
         this.lockYaw = config.getBoolean("Model.LockYaw", false);
         this.scale = config.getDouble("Model.Scale", 1.0D);
         this.hitboxScale = config.getDouble("Model.HitboxScale", this.scale);
      } else {
         this.id = config.getString("Model");
      }

   }

   public void apply(AbstractEntity entity) {
      if (this.id == null) {
         MythicLogger.error("ModelEngine ID not specified");
      } else {
         ModelBlueprint blueprint = ModelEngineAPI.getBlueprint(this.id);
         if (blueprint == null) {
            MythicLogger.error("Unable to find model with ID {}", new Object[]{this.id});
         } else {
            Entity bukkitTarget = BukkitAdapter.adapt(entity);
            ModeledEntity model = ModelEngineAPI.getOrCreateModeledEntity(bukkitTarget.getUniqueId(), () -> {
               BukkitEntity baseEntity = new BukkitEntity(bukkitTarget);
               baseEntity.setMaxStepHeight(this.stepHeight);
               if (this.viewRadius > 0) {
                  baseEntity.setRenderRadius(this.viewRadius);
               }

               return baseEntity;
            });
            if (model.getModel(this.id).isPresent()) {
               MythicLogger.error("Entity already contains model with ID {}", new Object[]{this.id});
            } else {
               model.setBaseEntityVisible(!this.invisible);
               ActiveModel activeModel = ModelEngineAPI.createActiveModel(blueprint);
               activeModel.setScale(this.scale);
               activeModel.setHitboxScale(this.hitboxScale);
               activeModel.getMountManager().ifPresent((mountManager) -> {
                  ((MountManager)mountManager).setCanDrive(this.canDrive);
                  ((MountManager)mountManager).setCanRide(this.canRide);
               });
               activeModel.setCanHurt(this.doDamageTint);
               activeModel.setLockPitch(this.lockPitch);
               activeModel.setLockYaw(this.lockYaw);
               model.addModel(activeModel, this.useHitbox).ifPresent(ActiveModel::destroy);
               activeModel.getBone(this.nameplate).flatMap((modelBone) -> {
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
               });
            }
         }
      }
   }
}
