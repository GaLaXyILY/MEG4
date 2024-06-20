package com.ticxo.modelengine.api.generator.assets;

import com.ticxo.modelengine.api.utils.data.ResourceLocation;
import java.util.ArrayList;
import java.util.List;

public class BlueprintTexture {
   private int id;
   private int frameWidth;
   private int frameHeight;
   private ResourceLocation path;
   private BlueprintTexture.MCMeta mcMeta;
   private String source;

   public int getId() {
      return this.id;
   }

   public int getFrameWidth() {
      return this.frameWidth;
   }

   public int getFrameHeight() {
      return this.frameHeight;
   }

   public ResourceLocation getPath() {
      return this.path;
   }

   public BlueprintTexture.MCMeta getMcMeta() {
      return this.mcMeta;
   }

   public String getSource() {
      return this.source;
   }

   public void setId(int id) {
      this.id = id;
   }

   public void setFrameWidth(int frameWidth) {
      this.frameWidth = frameWidth;
   }

   public void setFrameHeight(int frameHeight) {
      this.frameHeight = frameHeight;
   }

   public void setPath(ResourceLocation path) {
      this.path = path;
   }

   public void setMcMeta(BlueprintTexture.MCMeta mcMeta) {
      this.mcMeta = mcMeta;
   }

   public void setSource(String source) {
      this.source = source;
   }

   public static class MCMeta {
      private transient boolean mustGenerate;
      private Boolean interpolate;
      private Integer width;
      private Integer height;
      private Integer frametime;
      private List<Object> frames;

      public void addFrame(int frame) {
         if (this.frames == null) {
            this.frames = new ArrayList();
         }

         this.frames.add(frame);
      }

      public void addFrame(int index, int time) {
         if (this.frames == null) {
            this.frames = new ArrayList();
         }

         this.frames.add(new BlueprintTexture.MCMeta.Frame(index, time));
      }

      public boolean isMustGenerate() {
         return this.mustGenerate;
      }

      public Boolean getInterpolate() {
         return this.interpolate;
      }

      public Integer getWidth() {
         return this.width;
      }

      public Integer getHeight() {
         return this.height;
      }

      public Integer getFrametime() {
         return this.frametime;
      }

      public List<Object> getFrames() {
         return this.frames;
      }

      public void setMustGenerate(boolean mustGenerate) {
         this.mustGenerate = mustGenerate;
      }

      public void setInterpolate(Boolean interpolate) {
         this.interpolate = interpolate;
      }

      public void setWidth(Integer width) {
         this.width = width;
      }

      public void setHeight(Integer height) {
         this.height = height;
      }

      public void setFrametime(Integer frametime) {
         this.frametime = frametime;
      }

      public static record Frame(int index, int time) {
         public Frame(int index, int time) {
            this.index = index;
            this.time = time;
         }

         public int index() {
            return this.index;
         }

         public int time() {
            return this.time;
         }
      }
   }
}
