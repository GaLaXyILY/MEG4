package com.ticxo.modelengine.core.model.bone.behavior;

import com.ticxo.modelengine.api.model.bone.ModelBone;
import com.ticxo.modelengine.api.model.bone.behavior.AbstractBoneBehavior;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehaviorData;
import com.ticxo.modelengine.api.model.bone.behavior.BoneBehaviorType;
import com.ticxo.modelengine.api.model.bone.type.NameTag;
import java.util.function.Supplier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class NameTagImpl extends AbstractBoneBehavior<NameTagImpl> implements NameTag {
   private static final String EMPTY_STRING = "{\"text\":\"\"}";
   private final Vector3f location = new Vector3f();
   private String jsonString = "{\"text\":\"\"}";
   private Supplier<String> jsonStringSupplier;
   private boolean visible;

   public NameTagImpl(ModelBone bone, BoneBehaviorType<NameTagImpl> type, BoneBehaviorData data) {
      super(bone, type, data);
   }

   public void onFinalize() {
      if (this.jsonStringSupplier != null) {
         this.setJsonString((String)this.jsonStringSupplier.get());
      }

      Location baseLocation = this.bone.getActiveModel().getModeledEntity().getBase().getLocation();
      this.bone.getGlobalPosition().rotateY((180.0F - this.bone.getYaw()) * 0.017453292F, this.location).add((float)baseLocation.getX(), (float)baseLocation.getY(), (float)baseLocation.getZ());
   }

   public void setString(String name) {
      this.setComponent(Component.text(name));
   }

   public void setComponent(Component component) {
      this.setJsonString((String)GsonComponentSerializer.gson().serialize(component));
   }

   public String getJsonString() {
      return this.jsonString == null ? "{\"text\":\"\"}" : this.jsonString;
   }

   public void setJsonString(String json) {
      this.jsonString = json == null ? "{\"text\":\"\"}" : json;
   }

   public void setComponentSupplier(@Nullable Supplier<Component> component) {
      this.jsonStringSupplier = component == null ? null : () -> {
         return (String)GsonComponentSerializer.gson().serialize((Component)component.get());
      };
   }

   public Vector3f getLocation() {
      return this.location;
   }

   public Supplier<String> getJsonStringSupplier() {
      return this.jsonStringSupplier;
   }

   public void setJsonStringSupplier(Supplier<String> jsonStringSupplier) {
      this.jsonStringSupplier = jsonStringSupplier;
   }

   public boolean isVisible() {
      return this.visible;
   }

   public void setVisible(boolean visible) {
      this.visible = visible;
   }
}
