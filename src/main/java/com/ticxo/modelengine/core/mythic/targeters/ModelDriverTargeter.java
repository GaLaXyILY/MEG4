package com.ticxo.modelengine.core.mythic.targeters;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.model.bone.manager.BehaviorManager;
import com.ticxo.modelengine.api.model.bone.manager.MountData;
import com.ticxo.modelengine.api.model.bone.manager.MountManager;
import com.ticxo.modelengine.core.mythic.utils.MythicTargeter;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.skills.SkillCaster;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.targeters.IEntityTargeter;
import io.lumine.mythic.bukkit.BukkitAdapter;
import java.util.Collection;
import java.util.HashSet;

@MythicTargeter(
   name = "modeldriver",
   aliases = {}
)
public class ModelDriverTargeter implements IEntityTargeter {
   public Collection<AbstractEntity> getEntities(SkillMetadata skillMetadata) {
      HashSet<AbstractEntity> targets = new HashSet();
      SkillCaster caster = skillMetadata.getCaster();
      ModeledEntity model = ModelEngineAPI.getModeledEntity(caster.getEntity().getUniqueId());
      if (model == null) {
         return targets;
      } else {
         BehaviorManager main = ((MountData)model.getMountData()).getMainMountManager();
         if (((MountManager)main).isControlled()) {
            targets.add(BukkitAdapter.adapt(((MountManager)main).getDriver()));
         }

         return targets;
      }
   }
}
