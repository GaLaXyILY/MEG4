package com.ticxo.modelengine.api.utils.logger;

public enum LogColor {
   BLACK("\u001b[30m"),
   RED("\u001b[31m"),
   GREEN("\u001b[32m"),
   YELLOW("\u001b[33m"),
   BLUE("\u001b[34m"),
   PURPLE("\u001b[35m"),
   CYAN("\u001b[36m"),
   WHITE("\u001b[37m"),
   BRIGHT_GREEN("\u001b[38;5;46m"),
   RESET("\u001b[0m"),
   BOLD("\u001b[1m"),
   ITALICS("\u001b[2m"),
   UNDERLINE("\u001b[4m");

   private final String ansiColor;

   private LogColor(String ansiColor) {
      this.ansiColor = ansiColor;
   }

   public String toString() {
      return this.ansiColor;
   }

   public String getAnsiColor() {
      return this.ansiColor;
   }

   // $FF: synthetic method
   private static LogColor[] $values() {
      return new LogColor[]{BLACK, RED, GREEN, YELLOW, BLUE, PURPLE, CYAN, WHITE, BRIGHT_GREEN, RESET, BOLD, ITALICS, UNDERLINE};
   }
}
