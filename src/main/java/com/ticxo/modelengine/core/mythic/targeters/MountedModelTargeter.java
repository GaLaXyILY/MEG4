package com.ticxo.modelengine.core.mythic.targeters;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.entity.BaseEntity;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.core.mythic.utils.MythicTargeter;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.skills.SkillCaster;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.targeters.IEntityTargeter;
import io.lumine.mythic.bukkit.BukkitAdapter;
import java.util.Collection;
import java.util.HashSet;
import org.bukkit.entity.Entity;

@MythicTargeter(
   name = "mountedmodel",
   aliases = {}
)
public class MountedModelTargeter implements IEntityTargeter {
   public Collection<AbstractEntity> getEntities(SkillMetadata skillMetadata) {
      HashSet<AbstractEntity> targets = new HashSet();
      SkillCaster caster = skillMetadata.getCaster();
      ActiveModel model = ModelEngineAPI.getMountPairManager().getMountedPair(caster.getEntity().getUniqueId());
      if (model != null) {
         BaseEntity<?> base = model.getModeledEntity().getBase();
         Object var7 = base.getOriginal();
         if (var7 instanceof Entity) {
            Entity entity = (Entity)var7;
            targets.add(BukkitAdapter.adapt(entity));
         }
      }

      return targets;
   }
}
