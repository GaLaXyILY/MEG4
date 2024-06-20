package com.ticxo.modelengine.api.model.bone.render.renderer;

import com.ticxo.modelengine.api.model.bone.render.BehaviorRenderer;
import com.ticxo.modelengine.api.utils.data.tracker.DataTracker;
import java.util.UUID;
import org.joml.Vector3f;

public interface LeashRenderer extends BehaviorRenderer, RenderQueues<LeashRenderer.Leash> {
   public interface Leash {
      int getId();

      UUID getUuid();

      DataTracker<Vector3f> getPosition();

      DataTracker<Integer> getConnected();
   }
}
