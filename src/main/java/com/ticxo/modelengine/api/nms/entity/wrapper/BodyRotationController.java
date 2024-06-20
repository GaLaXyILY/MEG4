package com.ticxo.modelengine.api.nms.entity.wrapper;

import com.ticxo.modelengine.api.utils.data.io.DataIO;
import com.ticxo.modelengine.api.utils.data.io.SavedData;

public interface BodyRotationController extends DataIO {
   default void tick() {
   }

   float getYHeadRot();

   void setYHeadRot(float var1);

   float getXHeadRot();

   void setXHeadRot(float var1);

   float getYBodyRot();

   void setYBodyRot(float var1);

   boolean isHeadClampUneven();

   void setHeadClampUneven(boolean var1);

   boolean isBodyClampUneven();

   void setBodyClampUneven(boolean var1);

   float getMaxHeadAngle();

   void setMaxHeadAngle(float var1);

   float getMaxBodyAngle();

   void setMaxBodyAngle(float var1);

   float getMinHeadAngle();

   void setMinHeadAngle(float var1);

   float getMinBodyAngle();

   void setMinBodyAngle(float var1);

   float getStableAngle();

   void setStableAngle(float var1);

   boolean isPlayerMode();

   void setPlayerMode(boolean var1);

   int getRotationDelay();

   void setRotationDelay(int var1);

   int getRotationDuration();

   void setRotationDuration(int var1);

   default void save(SavedData data) {
      data.putBoolean("head", this.isHeadClampUneven());
      data.putBoolean("body", this.isBodyClampUneven());
      data.putFloat("max_head", this.getMaxHeadAngle());
      data.putFloat("max_body", this.getMaxBodyAngle());
      data.putFloat("min_head", this.getMinHeadAngle());
      data.putFloat("min_body", this.getMinBodyAngle());
      data.putFloat("stable", this.getStableAngle());
      data.putBoolean("player", this.isPlayerMode());
      data.putInt("delay", this.getRotationDelay());
      data.putInt("duration", this.getRotationDuration());
   }

   default void load(SavedData data) {
      this.setHeadClampUneven(data.getBoolean("head"));
      this.setBodyClampUneven(data.getBoolean("body"));
      this.setMaxHeadAngle(data.getFloat("max_head"));
      this.setMaxBodyAngle(data.getFloat("max_body"));
      this.setMinHeadAngle(data.getFloat("min_head"));
      this.setMinBodyAngle(data.getFloat("min_body"));
      this.setStableAngle(data.getFloat("stable"));
      this.setPlayerMode(data.getBoolean("player"));
      this.setRotationDelay(data.getInt("delay"));
      this.setRotationDuration(data.getInt("duration"));
   }
}
