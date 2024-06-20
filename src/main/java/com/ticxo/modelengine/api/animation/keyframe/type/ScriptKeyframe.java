package com.ticxo.modelengine.api.animation.keyframe.type;

import com.ticxo.modelengine.api.animation.property.IAnimationProperty;
import java.util.ArrayList;
import java.util.List;

public class ScriptKeyframe extends AbstractKeyframe<List<ScriptKeyframe.Script>> {
   private final List<ScriptKeyframe.Script> script = new ArrayList();

   public List<ScriptKeyframe.Script> getValue(int index, IAnimationProperty property) {
      return this.script;
   }

   public List<ScriptKeyframe.Script> getScript() {
      return this.script;
   }

   public static record Script(String reader, String script) {
      public Script(String reader, String script) {
         this.reader = reader;
         this.script = script;
      }

      public static ScriptKeyframe.Script from(String full) {
         String[] split = full.split(":", 2);
         return split.length <= 1 ? new ScriptKeyframe.Script("meg", split[0]) : new ScriptKeyframe.Script(split[0], split[1]);
      }

      public String reader() {
         return this.reader;
      }

      public String script() {
         return this.script;
      }
   }
}
