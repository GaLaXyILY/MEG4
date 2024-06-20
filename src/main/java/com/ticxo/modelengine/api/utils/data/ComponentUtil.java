package com.ticxo.modelengine.api.utils.data;

import java.util.Set;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.Style.Builder;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;

public class ComponentUtil {
   public static Style reset() {
      return ((Builder)Style.style().decorations(Set.of(TextDecoration.BOLD, TextDecoration.ITALIC, TextDecoration.OBFUSCATED, TextDecoration.STRIKETHROUGH, TextDecoration.UNDERLINED), false)).color(NamedTextColor.WHITE).build();
   }

   public static Style color(int color) {
      return color(TextColor.color(color));
   }

   public static Style color(TextColor color) {
      return ((Builder)Style.style().decorations(Set.of(TextDecoration.BOLD, TextDecoration.ITALIC, TextDecoration.OBFUSCATED, TextDecoration.STRIKETHROUGH, TextDecoration.UNDERLINED), false)).color(color).build();
   }

   public static String string(Component component) {
      return LegacyComponentSerializer.legacySection().serialize(component);
   }

   public static BaseComponent[] base(Component component) {
      return BungeeComponentSerializer.legacy().serialize(component);
   }

   public static void sendMessage(Player player, Component component) {
      player.sendMessage(base(component));
   }
}
