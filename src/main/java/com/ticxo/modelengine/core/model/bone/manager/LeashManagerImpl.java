package com.ticxo.modelengine.core.model.bone.manager;

import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehavior;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehaviorType;
import com.ticxo.modelengine.api.model.bone.manager.AbstractBehaviorManager;
import com.ticxo.modelengine.api.model.bone.manager.LeashManager;
import com.ticxo.modelengine.api.model.bone.type.Leash;
import com.ticxo.modelengine.core.model.bone.behavior.LeashImpl;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.bukkit.entity.Entity;

public class LeashManagerImpl extends AbstractBehaviorManager<LeashImpl> implements LeashManager {
   private final Map<String, ?> main = new LinkedHashMap();
   private final Map<String, ?> leashes = new LinkedHashMap();

   public LeashManagerImpl(ActiveModel activeModel, BoneBehaviorType<LeashImpl> type) {
      super(activeModel, type);
   }

   public <T extends Leash & BoneBehavior> void registerLeash(T leash) {
      this.getLeashes().put(((BoneBehavior)leash).getBone().getUniqueBoneId(), (BoneBehavior)leash);
      if (leash.isMainLeash()) {
         this.getMainLeashes().put(((BoneBehavior)leash).getBone().getUniqueBoneId(), (BoneBehavior)leash);
      }

   }

   public <T extends Leash & BoneBehavior> Map<String, T> getMainLeashes() {
      return this.main;
   }

   public <T extends Leash & BoneBehavior> Map<String, T> getLeashes() {
      return this.leashes;
   }

   public <T extends Leash & BoneBehavior> Optional<T> getLeash(String boneId) {
      return Optional.ofNullable((Leash)this.getLeashes().get(boneId));
   }

   public void connectMainLeashes(Entity leashHolder) {
      this.getMainLeashes().values().forEach((leash) -> {
         ((Leash)leash).connect(leashHolder);
      });
   }

   public void connectMainLeashes(String from) {
      this.getLeash(from).ifPresent((leash) -> {
         this.getMainLeashes().values().forEach((mainLeash) -> {
            ((Leash)mainLeash).connect((Leash)leash);
         });
      });
   }

   public void disconnectMainLeashes() {
      this.getMainLeashes().values().forEach((rec$) -> {
         ((Leash)rec$).disconnect();
      });
   }

   public void connectLeash(Entity leashHolder, String to) {
      this.getLeash(to).ifPresent((leash) -> {
         ((Leash)leash).connect(leashHolder);
      });
   }

   public void connectLeash(String from, String to) {
      this.getLeash(to).ifPresent((toLeash) -> {
         this.getLeash(from).ifPresent((fromLeash) -> {
            ((Leash)toLeash).connect((Leash)fromLeash);
         });
      });
   }

   public void disconnect(String bone) {
      this.getLeash(bone).ifPresent((rec$) -> {
         ((Leash)rec$).disconnect();
      });
   }

   public Entity getLeashHolder(String bone) {
      return (Entity)this.getLeash(bone).map((rec$) -> {
         return ((Leash)rec$).getConnectedEntity();
      }).orElse((Object)null);
   }

   public <T extends Leash & BoneBehavior> T getLeashConnection(String bone) {
      return (Leash)this.getLeash(bone).map((rec$) -> {
         return (BoneBehavior)((Leash)rec$).getConnectedLeash();
      }).orElse((Object)null);
   }
}
