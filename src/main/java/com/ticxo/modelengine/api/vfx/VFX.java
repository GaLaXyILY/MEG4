package com.ticxo.modelengine.api.vfx;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.entity.BaseEntity;
import com.ticxo.modelengine.api.generator.blueprint.BlueprintBone;
import com.ticxo.modelengine.api.generator.blueprint.ModelBlueprint;
import com.ticxo.modelengine.api.utils.config.ConfigProperty;
import com.ticxo.modelengine.api.utils.data.tracker.DataTracker;
import com.ticxo.modelengine.api.vfx.render.VFXRenderer;
import org.bukkit.Color;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.joml.Vector3f;

public interface VFX {
   BaseEntity<?> getBase();

   VFXRenderer getRenderer();

   boolean tick();

   void destroy();

   boolean isInitialized();

   boolean isDestroyed();

   void markRemoved();

   void queuePostInitTask(Runnable var1);

   boolean isBaseEntityVisible();

   void setBaseEntityVisible(boolean var1);

   void setModelScale(int var1);

   float getYaw();

   void setYaw(float var1);

   float getPitch();

   void setPitch(float var1);

   Vector getOrigin();

   void setOrigin(Vector var1);

   Vector3f getPosition();

   void setPosition(Vector3f var1);

   Vector3f getRotation();

   void setRotation(Vector3f var1);

   Vector3f getScale();

   void setScale(Vector3f var1);

   ItemStack getModel();

   void setModel(ItemStack var1);

   DataTracker<ItemStack> getModelTracker();

   Color getColor();

   void setColor(Color var1);

   boolean isEnchanted();

   void setEnchanted(boolean var1);

   boolean isVisible();

   void setVisible(boolean var1);

   default void useModel(String modelId, String boneId) {
      ModelBlueprint blueprint = ModelEngineAPI.getBlueprint(modelId);
      if (blueprint != null) {
         BlueprintBone bone = (BlueprintBone)blueprint.getFlatMap().get(boneId);
         if (bone != null && bone.isRenderer()) {
            this.setModel(ConfigProperty.ITEM_MODEL.getBaseItem().create(Color.WHITE, bone.getDataId()));
         }
      }
   }

   default void registerSelf() {
      ModelEngineAPI.getAPI().getVFXUpdater().registerVFX(this.getBase(), this);
   }
}
