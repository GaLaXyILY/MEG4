package com.ticxo.modelengine.api.model.bone.type;

import java.util.function.Supplier;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public interface NameTag {
   Vector3f getLocation();

   void setString(String var1);

   void setComponent(Component var1);

   String getJsonString();

   void setJsonString(String var1);

   void setComponentSupplier(@Nullable Supplier<Component> var1);

   @Nullable
   Supplier<String> getJsonStringSupplier();

   void setJsonStringSupplier(@Nullable Supplier<String> var1);

   boolean isVisible();

   void setVisible(boolean var1);
}
