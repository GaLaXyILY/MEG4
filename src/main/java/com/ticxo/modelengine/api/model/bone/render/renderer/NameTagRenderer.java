package com.ticxo.modelengine.api.model.bone.render.renderer;

import com.ticxo.modelengine.api.model.bone.render.BehaviorRenderer;
import com.ticxo.modelengine.api.utils.data.tracker.DataTracker;
import java.util.UUID;
import org.joml.Vector3f;

public interface NameTagRenderer extends BehaviorRenderer, RenderQueues<NameTagRenderer.NameTag> {
   public interface NameTag {
      int getPivotId();

      UUID getPivotUuid();

      int getTagId();

      UUID getTagUuid();

      DataTracker<Vector3f> getPosition();

      DataTracker<String> getJsonString();

      DataTracker<Boolean> getVisibility();

      default boolean isDirty() {
         return this.getJsonString().isDirty() || this.getVisibility().isDirty();
      }

      default void clearDirty() {
         this.getJsonString().clearDirty();
         this.getVisibility().clearDirty();
      }
   }
}
