package com.ticxo.modelengine.api.nms.entity;

import com.ticxo.modelengine.api.model.bone.ModelBone;
import com.ticxo.modelengine.api.model.bone.type.SubHitbox;
import com.ticxo.modelengine.api.utils.math.OrientedBoundingBox;
import java.util.UUID;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public interface HitboxEntity {
   ModelBone getBone();

   SubHitbox getSubHitbox();

   int getEntityId();

   UUID getUniqueId();

   void queueLocation(Vector3f var1);

   Location getLocation();

   @Nullable
   OrientedBoundingBox getOrientedBoundingBox();

   void markRemoved();
}
