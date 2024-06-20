package com.ticxo.modelengine.api.model.bone.manager;

import com.ticxo.modelengine.api.model.bone.behavior.BoneBehavior;
import com.ticxo.modelengine.api.model.bone.type.Leash;
import java.util.Map;
import java.util.Optional;
import org.bukkit.entity.Entity;

public interface LeashManager {
   <T extends Leash & BoneBehavior> void registerLeash(T var1);

   <T extends Leash & BoneBehavior> Map<String, T> getMainLeashes();

   <T extends Leash & BoneBehavior> Map<String, T> getLeashes();

   <T extends Leash & BoneBehavior> Optional<T> getLeash(String var1);

   void connectMainLeashes(Entity var1);

   void connectMainLeashes(String var1);

   void disconnectMainLeashes();

   void connectLeash(Entity var1, String var2);

   void connectLeash(String var1, String var2);

   void disconnect(String var1);

   Entity getLeashHolder(String var1);

   <T extends Leash & BoneBehavior> T getLeashConnection(String var1);
}
