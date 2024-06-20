package com.ticxo.modelengine.core.citizens;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.nms.entity.EntityHandler;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

@TraitName("bug")
public class BugTrait extends Trait {
   private int tick;

   public BugTrait() {
      super("bug");
   }

   public void run() {
      Entity entity = this.npc.getEntity();
      if (entity != null) {
         EntityHandler handler = ModelEngineAPI.getEntityHandler();
         Location location = entity.getLocation();
         World world = location.getWorld();
         Vector yRot = (new Vector(0.0D, 0.0D, 0.5D)).rotateAroundY((double)(-handler.getYRot(entity) * 0.017453292F));
         Vector yBodyRot = (new Vector(0, 0, 1)).rotateAroundY((double)(-handler.getYBodyRot(entity) * 0.017453292F));
         Vector yHeadRot = (new Vector(0.0D, 0.0D, 1.5D)).rotateAroundY((double)(-handler.getYHeadRot(entity) * 0.017453292F));
         world.spawnParticle(Particle.REDSTONE, location.clone().add(yRot), 1, new DustOptions(Color.RED, 0.25F));
         world.spawnParticle(Particle.REDSTONE, location.clone().add(yBodyRot), 1, new DustOptions(Color.GREEN, 0.25F));
         world.spawnParticle(Particle.REDSTONE, location.clone().add(yHeadRot), 1, new DustOptions(Color.BLUE, 0.25F));
      }
   }
}
