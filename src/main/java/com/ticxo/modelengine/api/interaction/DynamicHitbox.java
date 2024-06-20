package com.ticxo.modelengine.api.interaction;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.utils.data.tracker.DataTracker;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class DynamicHitbox {
   private static final int pivotId = ModelEngineAPI.getEntityHandler().getNextEntityId();
   private static final UUID pivotUUID = UUID.randomUUID();
   private static final int hitboxId = ModelEngineAPI.getEntityHandler().getNextEntityId();
   private static final UUID hitboxUUID = UUID.randomUUID();
   private final Player player;
   private final DataTracker<Vector> positionTracker = new DataTracker();
   private int target;

   public DynamicHitbox(Player player, Vector position) {
      this.player = player;
      this.positionTracker.set(position);
      ModelEngineAPI.getEntityHandler().spawnDynamicHitbox(this);
      this.positionTracker.clearDirty();
   }

   public void update(Vector position) {
      this.positionTracker.set(position.setY(position.getY()));
      if (this.positionTracker.isDirty()) {
         ModelEngineAPI.getEntityHandler().updateDynamicHitbox(this);
         this.positionTracker.clearDirty();
      }

   }

   public void destroy() {
      ModelEngineAPI.getEntityHandler().destroyDynamicHitbox(this);
   }

   public static int getPivotId() {
      return pivotId;
   }

   public static UUID getPivotUUID() {
      return pivotUUID;
   }

   public static int getHitboxId() {
      return hitboxId;
   }

   public static UUID getHitboxUUID() {
      return hitboxUUID;
   }

   public Player getPlayer() {
      return this.player;
   }

   public DataTracker<Vector> getPositionTracker() {
      return this.positionTracker;
   }

   public int getTarget() {
      return this.target;
   }

   public void setTarget(int target) {
      this.target = target;
   }
}
