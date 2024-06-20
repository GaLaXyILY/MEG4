package com.ticxo.modelengine.core.mythic.mechanics.controller;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.nms.entity.EntityHandler;
import com.ticxo.modelengine.core.mythic.utils.MythicMechanic;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import java.util.Locale;
import org.bukkit.entity.Entity;

@MythicMechanic(
   name = "syncyaw",
   aliases = {}
)
public class SyncYawMechanic implements ITargetedEntitySkill {
   private final String target;
   private final boolean body;

   public SyncYawMechanic(MythicLineConfig mlc) {
      this.target = mlc.getString(new String[]{"target", "t"}, "yaw", new String[0]);
      this.body = mlc.getBoolean(new String[]{"body", "b"}, false);
   }

   public SkillResult castAtEntity(SkillMetadata meta, AbstractEntity target) {
      Entity entity = target.getBukkitEntity();
      EntityHandler handler = ModelEngineAPI.getEntityHandler();
      String var5 = this.target.toLowerCase(Locale.ENGLISH);
      byte var6 = -1;
      switch(var5.hashCode()) {
      case 119407:
         if (var5.equals("yaw")) {
            var6 = 0;
         }
         break;
      case 3029410:
         if (var5.equals("body")) {
            var6 = 1;
         }
         break;
      case 3198432:
         if (var5.equals("head")) {
            var6 = 2;
         }
      }

      switch(var6) {
      case 0:
         handler.setYRot(entity, this.body ? handler.getYBodyRot(entity) : handler.getYHeadRot(entity));
         break;
      case 1:
         handler.setYBodyRot(entity, handler.getYHeadRot(entity));
         break;
      case 2:
         handler.setYHeadRot(entity, handler.getYBodyRot(entity));
      }

      return SkillResult.SUCCESS;
   }
}
