package com.ticxo.modelengine.core.generator.atlas;

import java.util.ArrayList;
import java.util.List;

public class Atlas {
   private final List<Atlas.Source> sources = new ArrayList();

   public List<Atlas.Source> getSources() {
      return this.sources;
   }

   public static class Directory extends Atlas.Source {
      public final String source;
      public final String prefix;

      public Directory(String location) {
         super("directory");
         this.source = location;
         this.prefix = location + "/";
      }
   }

   public static class Single extends Atlas.Source {
      public final String resource;
      public final String sprite;

      public Single(String location) {
         super("single");
         this.resource = location;
         this.sprite = location;
      }
   }

   public static class Source {
      public final String type;

      public Source(String type) {
         this.type = type;
      }
   }
}
