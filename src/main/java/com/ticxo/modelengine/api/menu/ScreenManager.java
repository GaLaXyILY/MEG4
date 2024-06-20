package com.ticxo.modelengine.api.menu;

import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.Map;
import org.bukkit.inventory.Inventory;

public class ScreenManager {
   private final Map<Inventory, AbstractScreen> screens = Maps.newConcurrentMap();

   public void registerScreen(AbstractScreen screen) {
      this.screens.put(screen.inventory, screen);
   }

   public void unregisterScreen(Inventory inventory) {
      this.screens.remove(inventory);
   }

   public void unregisterScreen(AbstractScreen screen) {
      this.screens.remove(screen.inventory);
   }

   public AbstractScreen getScreen(Inventory inventory) {
      return (AbstractScreen)this.screens.get(inventory);
   }

   public boolean isScreen(Inventory inventory) {
      return this.screens.containsKey(inventory);
   }

   public void updateAllScreens() {
      Iterator var1 = this.screens.values().iterator();

      while(var1.hasNext()) {
         AbstractScreen screen = (AbstractScreen)var1.next();
         screen.onTick();
      }

   }

   public Map<Inventory, AbstractScreen> getScreens() {
      return this.screens;
   }
}
