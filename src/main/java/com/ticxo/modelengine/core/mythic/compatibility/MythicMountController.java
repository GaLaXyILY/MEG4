package com.ticxo.modelengine.core.mythic.compatibility;

import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.bone.type.Mount;
import com.ticxo.modelengine.api.mount.controller.impl.AbstractMountController;
import com.ticxo.modelengine.api.nms.entity.wrapper.MoveController;
import io.lumine.mythic.api.skills.Skill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.skills.variables.VariableRegistry;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.joml.Vector3f;

public class MythicMountController extends AbstractMountController {
   private final Skill skill;
   private final SkillMetadata metadata;
   private final VariableRegistry variableRegistry;

   public MythicMountController(Entity entity, Mount mount, Skill skill, SkillMetadata data) {
      super(entity, mount);
      this.skill = skill;
      this.metadata = MythicBukkit.inst().getSkillManager().getEventBus().buildSkillMetadata(data.getCause(), data.getCaster(), data.getTrigger(), data.getOrigin(), !data.isAsync());
      this.metadata.setTrigger(BukkitAdapter.adapt(entity));
      this.variableRegistry = this.metadata.getVariables();
   }

   public void updateDriverMovement(MoveController controller, ActiveModel model) {
      this.callSkillAs(controller, model, "driver");
   }

   public void updatePassengerMovement(MoveController controller, ActiveModel model) {
      this.callSkillAs(controller, model, "passenger");
   }

   public void callSkillAs(MoveController controller, ActiveModel model, String mode) {
      if (this.skill.isUsable(this.metadata) && !this.skill.onCooldown(this.metadata.getCaster())) {
         Vector3f location = this.getMount().getGlobalLocation();
         this.metadata.setOrigin(BukkitAdapter.adapt(new Location(this.entity.getWorld(), (double)location.x, (double)location.y, (double)location.z)));
         this.metadata.setMetadata("meg:active_model", model);
         this.metadata.setMetadata("meg:move_controller", controller);
         this.variableRegistry.putString("meg:rider", mode);
         this.variableRegistry.putFloat("meg:front", this.input.getFront());
         this.variableRegistry.putFloat("meg:side", this.input.getSide());
         this.variableRegistry.putInt("meg:jump", this.input.isJump() ? 1 : 0);
         this.variableRegistry.putInt("meg:sneak", this.input.isSneak() ? 1 : 0);
         this.variableRegistry.putInt("meg:on_ground", controller.isOnGround() ? 1 : 0);
         this.variableRegistry.putFloat("meg:speed", controller.getSpeed());
         Vector vector = controller.getVelocity();
         this.variableRegistry.putFloat("meg:vx", (float)vector.getX());
         this.variableRegistry.putFloat("meg:vy", (float)vector.getY());
         this.variableRegistry.putFloat("meg:vz", (float)vector.getZ());
         this.skill.execute(this.metadata);
      }

   }
}
