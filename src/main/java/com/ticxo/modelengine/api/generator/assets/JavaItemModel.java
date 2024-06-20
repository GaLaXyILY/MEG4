package com.ticxo.modelengine.api.generator.assets;

import com.ticxo.modelengine.api.utils.math.TMath;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class JavaItemModel {
   private static final float DIST_DIVIDER = 0.041666668F;
   private static final Map<String, Map<String, int[]>> DISPLAY = new HashMap<String, Map<String, int[]>>() {
      {
         this.put("gui", new HashMap<String, int[]>() {
            // $FF: synthetic field
            final <undefinedtype> this$0;

            {
               this.this$0 = this$0;
               this.put("rotation", new int[]{30, 225, 0});
            }
         });
      }
   };
   private final Map<String, String> textures = new HashMap();
   private final List<JavaItemModel.JavaElement> elements = new ArrayList();
   private transient String name;
   private transient float maxDistToOrigin = 0.0F;
   private Map<String, Map<String, int[]>> display;

   public JavaItemModel() {
      this.display = DISPLAY;
   }

   public void addElement(JavaItemModel.JavaElement element) {
      this.elements.add(element);

      for(int i = 0; i < 3; ++i) {
         this.maxDistToOrigin = Math.max(Math.max(Math.abs(element.from[i] - 8.0F), Math.abs(element.to[i] - 8.0F)), this.maxDistToOrigin);
      }

   }

   public int scaleToFit() {
      if (this.maxDistToOrigin <= 24.0F) {
         return 1;
      } else {
         int size = (int)Math.ceil((double)(this.maxDistToOrigin * 0.041666668F));
         float scale = 1.0F / (float)size;
         Iterator var3 = this.elements.iterator();

         while(var3.hasNext()) {
            JavaItemModel.JavaElement element = (JavaItemModel.JavaElement)var3.next();
            float[] origin = element.getRotation() == null ? null : element.getRotation().origin;

            for(int i = 0; i < 3; ++i) {
               element.from[i] = TMath.clamp((element.from[i] - 8.0F) * scale + 8.0F, -16.0F, 32.0F);
               element.to[i] = TMath.clamp((element.to[i] - 8.0F) * scale + 8.0F, -16.0F, 32.0F);
               if (origin != null) {
                  origin[i] = (origin[i] - 8.0F) * scale + 8.0F;
               }
            }
         }

         return size;
      }
   }

   public void finalizeModel() {
   }

   public Map<String, String> getTextures() {
      return this.textures;
   }

   public List<JavaItemModel.JavaElement> getElements() {
      return this.elements;
   }

   public String getName() {
      return this.name;
   }

   public float getMaxDistToOrigin() {
      return this.maxDistToOrigin;
   }

   public Map<String, Map<String, int[]>> getDisplay() {
      return this.display;
   }

   public void setName(String name) {
      this.name = name;
   }

   public void setMaxDistToOrigin(float maxDistToOrigin) {
      this.maxDistToOrigin = maxDistToOrigin;
   }

   public void setDisplay(Map<String, Map<String, int[]>> display) {
      this.display = display;
   }

   public static class JavaElement {
      private final float[] from = new float[3];
      private final float[] to = new float[3];
      private final Map<String, JavaItemModel.JavaElement.Face> faces = new HashMap();
      private JavaItemModel.JavaElement.Rotation rotation;

      public void from(float[] origin, float[] globalFrom, float inflate) {
         this.from[0] = globalFrom[0] - origin[0] + 8.0F - inflate;
         this.from[1] = globalFrom[1] - origin[1] + 8.0F - inflate;
         this.from[2] = globalFrom[2] - origin[2] + 8.0F - inflate;
      }

      public void to(float[] origin, float[] globalTo, float inflate) {
         this.to[0] = globalTo[0] - origin[0] + 8.0F + inflate;
         this.to[1] = globalTo[1] - origin[1] + 8.0F + inflate;
         this.to[2] = globalTo[2] - origin[2] + 8.0F + inflate;
      }

      public float[] getFrom() {
         return this.from;
      }

      public float[] getTo() {
         return this.to;
      }

      public Map<String, JavaItemModel.JavaElement.Face> getFaces() {
         return this.faces;
      }

      public JavaItemModel.JavaElement.Rotation getRotation() {
         return this.rotation;
      }

      public void setRotation(JavaItemModel.JavaElement.Rotation rotation) {
         this.rotation = rotation;
      }

      public static class Rotation {
         private final float[] origin = new float[]{8.0F, 8.0F, 8.0F};
         private float angle;
         private String axis = "x";

         public void origin(float[] origin, float[] globalOrigin) {
            this.origin[0] = globalOrigin[0] - origin[0] + 8.0F;
            this.origin[1] = globalOrigin[1] - origin[1] + 8.0F;
            this.origin[2] = globalOrigin[2] - origin[2] + 8.0F;
         }

         public float[] getOrigin() {
            return this.origin;
         }

         public float getAngle() {
            return this.angle;
         }

         public String getAxis() {
            return this.axis;
         }

         public void setAngle(float angle) {
            this.angle = angle;
         }

         public void setAxis(String axis) {
            this.axis = axis;
         }
      }

      public static class Face {
         private final float[] uv = new float[4];
         private final int tintindex = 0;
         private int rotation;
         private String texture = "";

         public void uv(int width, int height, float[] uv) {
            float factorU = 16.0F / (float)width;
            float factorV = 16.0F / (float)height;
            this.uv[0] = uv[0] * factorU;
            this.uv[1] = uv[1] * factorV;
            this.uv[2] = uv[2] * factorU;
            this.uv[3] = uv[3] * factorV;
         }

         public float[] getUv() {
            return this.uv;
         }

         public int getTintindex() {
            Objects.requireNonNull(this);
            return 0;
         }

         public int getRotation() {
            return this.rotation;
         }

         public String getTexture() {
            return this.texture;
         }

         public void setRotation(int rotation) {
            this.rotation = rotation;
         }

         public void setTexture(String texture) {
            this.texture = texture;
         }
      }
   }
}
