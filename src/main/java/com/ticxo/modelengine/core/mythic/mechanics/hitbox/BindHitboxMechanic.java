package com.ticxo.modelengine.core.mythic.mechanics.hitbox;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.model.bone.BoneBehaviorTypes;
import com.ticxo.modelengine.api.model.bone.ModelBone;
import com.ticxo.modelengine.api.model.bone.type.SubHitbox;
import com.ticxo.modelengine.core.mythic.MythicUtils;
import com.ticxo.modelengine.core.mythic.utils.MythicMechanic;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.api.mobs.entities.SpawnReason;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillCaster;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.ThreadSafetyLevel;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import io.lumine.mythic.bukkit.adapters.BukkitEntityType;
import io.lumine.mythic.core.logging.MythicLogger;
import io.lumine.mythic.core.mobs.ActiveMob;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.joml.Vector3f;

@MythicMechanic(
   name = "bindhitbox",
   aliases = {}
)
public class BindHitboxMechanic implements ITargetedEntitySkill {
   private final PlaceholderString modelId;
   private final PlaceholderString partId;
   private final String strType;
   private MythicMob mm;
   private BukkitEntityType me;

   public BindHitboxMechanic(MythicLineConfig mlc) {
      this.modelId = mlc.getPlaceholderString(new String[]{"m", "mid", "model", "modelid"}, (String)null, new String[0]);
      this.partId = mlc.getPlaceholderString(new String[]{"p", "pid", "part", "partid"}, (String)null, new String[0]);
      this.strType = mlc.getString(new String[]{"type", "t", "mob", "m"}, "SKELETON", new String[0]);
      this.getPlugin().getSkillManager().queueSecondPass(() -> {
         this.mm = (MythicMob)this.getPlugin().getMobManager().getMythicMob(this.strType).orElse((Object)null);
         if (this.mm == null) {
            this.me = BukkitEntityType.getMythicEntity(this.strType);
            if (this.me == null) {
               MythicLogger.errorGenericConfig(mlc, "The 'type' attribute must be a valid MythicMob or MythicEntity type.");
            }
         }

      });
   }

   public SkillResult castAtEntity(SkillMetadata meta, AbstractEntity target) {
      ModeledEntity model = ModelEngineAPI.getModeledEntity(target.getUniqueId());
      if (model == null) {
         return SkillResult.CONDITION_FAILED;
      } else {
         String modelId = MythicUtils.getOrNullLowercase(this.modelId, meta, target);
         model.getModel(modelId).ifPresent((activeModel) -> {
            String pbone = MythicUtils.getOrNullLowercase(this.partId, meta, target);
            if (pbone != null) {
               List<String> hitboxes = List.of(pbone.split(","));
               ArrayList<SubHitbox> subHitboxes = new ArrayList();
               Iterator var7 = hitboxes.iterator();

               while(var7.hasNext()) {
                  String hitboxId = (String)var7.next();
                  Optional<ModelBone> maybeBone = activeModel.getBone(hitboxId);
                  if (!maybeBone.isEmpty()) {
                     ModelBone bone = (ModelBone)maybeBone.get();
                     Optional maybeHitbox = bone.getBoneBehavior(BoneBehaviorTypes.SUB_HITBOX);
                     if (!maybeHitbox.isEmpty()) {
                        subHitboxes.add((SubHitbox)maybeHitbox.get());
                     }
                  }
               }

               this.createBoundEntity(meta.getCaster(), subHitboxes);
            }
         });
         return SkillResult.SUCCESS;
      }
   }

   public ThreadSafetyLevel getThreadSafetyLevel() {
      return ThreadSafetyLevel.SYNC_ONLY;
   }

   private void createBoundEntity(SkillCaster caster, List<SubHitbox> subHitboxes) {
      if (!subHitboxes.isEmpty()) {
         AbstractEntity abstractCaster = caster.getEntity();
         Vector3f vec = ((SubHitbox)subHitboxes.get(0)).getLocation();
         AbstractLocation l = new AbstractLocation(abstractCaster.getWorld(), (double)vec.x, (double)vec.y, (double)vec.z);
         if (this.mm != null) {
            ActiveMob ams = this.mm.spawn(l, caster.getLevel(), SpawnReason.SUMMON, (entity) -> {
               Iterator var2 = subHitboxes.iterator();

               while(var2.hasNext()) {
                  SubHitbox hitbox = (SubHitbox)var2.next();
                  hitbox.addBoundEntity(entity);
               }

            });
            ams.setParent(caster);
            ams.setOwner(abstractCaster.getUniqueId());
            if (caster instanceof ActiveMob) {
               ActiveMob am = (ActiveMob)caster;
               ams.setFaction(am.getFaction());
            }
         } else if (this.me != null) {
            this.me.spawn(l, SpawnReason.SUMMON, (entity) -> {
               Iterator var2 = subHitboxes.iterator();

               while(var2.hasNext()) {
                  SubHitbox hitbox = (SubHitbox)var2.next();
                  hitbox.addBoundEntity(entity);
               }

            });
         }

      }
   }
}
