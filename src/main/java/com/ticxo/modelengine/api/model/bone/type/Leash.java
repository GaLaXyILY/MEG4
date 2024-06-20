package com.ticxo.modelengine.api.model.bone.type;

import com.ticxo.modelengine.api.model.bone.behavior.BoneBehavior;
import org.bukkit.entity.Entity;
import org.joml.Vector3f;

public interface Leash {
   boolean isMainLeash();

   int getId();

   Vector3f getLocation();

   void connect(Entity var1);

   <T extends Leash & BoneBehavior> void connect(T var1);

   void disconnect();

   Entity getConnectedEntity();

   <T extends Leash & BoneBehavior> T getConnectedLeash();
}
