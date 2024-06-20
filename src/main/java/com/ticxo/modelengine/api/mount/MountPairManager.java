package com.ticxo.modelengine.api.mount;

import com.google.common.collect.Maps;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.bone.manager.MountManager;
import com.ticxo.modelengine.api.mount.controller.MountController;
import com.ticxo.modelengine.api.nms.entity.wrapper.MoveController;
import it.unimi.dsi.fastutil.Pair;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

public class MountPairManager {
   private final Map<UUID, Pair<ActiveModel, MountController>> mountedPair = Maps.newConcurrentMap();

   public void updatePassengerPosition() {
      Iterator var1 = this.mountedPair.values().iterator();

      while(var1.hasNext()) {
         Pair<ActiveModel, MountController> pair = (Pair)var1.next();
         MoveController moveController = ((ActiveModel)pair.left()).getModeledEntity().getBase().getMoveController();
         ((MountController)pair.right()).updateRiderPosition(moveController);
      }

   }

   public void registerMountedPair(Entity entity, ActiveModel model, MountController controller) {
      this.mountedPair.put(entity.getUniqueId(), Pair.of(model, controller));
   }

   public void unregisterMountedPair(UUID uuid) {
      this.mountedPair.remove(uuid);
   }

   @Nullable
   public Pair<ActiveModel, MountController> get(UUID uuid) {
      return (Pair)this.mountedPair.get(ModelEngineAPI.getAPI().getDisguiseRelayOrDefault(uuid));
   }

   public ActiveModel getMountedPair(UUID uuid) {
      Pair<ActiveModel, MountController> pair = this.get(uuid);
      return pair == null ? null : (ActiveModel)pair.left();
   }

   public MountController getController(UUID uuid) {
      Pair<ActiveModel, MountController> pair = this.get(uuid);
      return pair == null ? null : (MountController)pair.right();
   }

   public void tryDismount(Entity entity) {
      ActiveModel model = this.getMountedPair(entity.getUniqueId());
      if (model != null) {
         model.getMountManager().ifPresent((mountManager) -> {
            ((MountManager)mountManager).dismountRider(entity);
         });
      }
   }
}
