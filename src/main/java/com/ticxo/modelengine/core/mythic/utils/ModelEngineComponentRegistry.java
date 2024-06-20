package com.ticxo.modelengine.core.mythic.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ISkillMechanic;
import io.lumine.mythic.api.skills.conditions.ISkillCondition;
import io.lumine.mythic.api.skills.targeters.ISkillTargeter;
import io.lumine.mythic.bukkit.events.MythicConditionLoadEvent;
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent;
import io.lumine.mythic.bukkit.events.MythicTargeterLoadEvent;
import io.lumine.mythic.core.logging.MythicLogger;
import io.lumine.mythic.core.utils.annotations.AnnotationUtil;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class ModelEngineComponentRegistry implements Listener {
   private final Map<String, Constructor<? extends ISkillMechanic>> mechanics;
   private final Map<String, Constructor<? extends ISkillCondition>> conditions;
   private final Map<String, Constructor<? extends ISkillTargeter>> targeters;

   public ModelEngineComponentRegistry(JavaPlugin plugin, String packagePath) {
      this(plugin, (Collection)Lists.newArrayList(new String[]{packagePath}));
   }

   public ModelEngineComponentRegistry(JavaPlugin plugin, Collection<String> packages) {
      this.mechanics = Maps.newConcurrentMap();
      this.conditions = Maps.newConcurrentMap();
      this.targeters = Maps.newConcurrentMap();
      Iterator var3 = packages.iterator();

      while(var3.hasNext()) {
         String packagePath = (String)var3.next();
         Collection<Class<?>> mechanicsClasses = AnnotationUtil.getAnnotatedClasses(plugin, packagePath, MythicMechanic.class);
         Iterator var6 = mechanicsClasses.iterator();

         String namespace;
         String[] aliases;
         int var15;
         while(var6.hasNext()) {
            Class clazz = (Class)var6.next();

            try {
               MythicMechanic annotation = (MythicMechanic)clazz.getAnnotation(MythicMechanic.class);
               String namespace = annotation.namespace();
               namespace = annotation.name();
               String[] aliases = annotation.aliases();
               if (ISkillMechanic.class.isAssignableFrom(clazz)) {
                  Constructor constructor;
                  try {
                     constructor = clazz.getConstructor(MythicLineConfig.class);
                  } catch (NoSuchMethodException var20) {
                     try {
                        constructor = clazz.getConstructor(MythicMechanicLoadEvent.class);
                     } catch (NoSuchMethodException var19) {
                        constructor = clazz.getConstructor();
                     }
                  }

                  this.mechanics.put(namespace.toUpperCase(), constructor);
                  this.mechanics.put(namespace.toUpperCase() + ":" + namespace.toUpperCase(), constructor);
                  aliases = aliases;
                  int var14 = aliases.length;

                  for(var15 = 0; var15 < var14; ++var15) {
                     String alias = aliases[var15];
                     this.mechanics.put(alias.toUpperCase(), constructor);
                     this.mechanics.put(namespace.toUpperCase() + ":" + alias.toUpperCase(), constructor);
                  }
               }
            } catch (Throwable var23) {
               MythicLogger.error("Failed to register custom mechanic {0}", new Object[]{clazz.getCanonicalName()});
               var23.printStackTrace();
            }
         }

         Collection<Class<?>> conditionClasses = AnnotationUtil.getAnnotatedClasses(plugin, packagePath, MythicCondition.class);
         Iterator var27 = conditionClasses.iterator();

         String namespace;
         int var41;
         while(var27.hasNext()) {
            Class clazz = (Class)var27.next();

            try {
               MythicCondition annotation = (MythicCondition)clazz.getAnnotation(MythicCondition.class);
               namespace = annotation.namespace();
               namespace = annotation.name();
               String[] aliases = annotation.aliases();
               if (ISkillCondition.class.isAssignableFrom(clazz)) {
                  Constructor constructor;
                  try {
                     constructor = clazz.getConstructor(MythicLineConfig.class);
                  } catch (NoSuchMethodException var22) {
                     constructor = clazz.getConstructor();
                  }

                  this.conditions.put(namespace.toUpperCase(), constructor);
                  this.conditions.put(namespace.toUpperCase() + ":" + namespace.toUpperCase(), constructor);
                  String[] var38 = aliases;
                  var15 = aliases.length;

                  for(var41 = 0; var41 < var15; ++var41) {
                     String alias = var38[var41];
                     this.conditions.put(alias.toUpperCase(), constructor);
                     this.conditions.put(namespace.toUpperCase() + ":" + alias.toUpperCase(), constructor);
                  }
               }
            } catch (Throwable var24) {
               MythicLogger.error("Failed to register custom condition {0}", new Object[]{clazz.getCanonicalName()});
               var24.printStackTrace();
            }
         }

         Collection<Class<?>> targeterClasses = AnnotationUtil.getAnnotatedClasses(plugin, packagePath, MythicTargeter.class);
         Iterator var30 = targeterClasses.iterator();

         while(var30.hasNext()) {
            Class clazz = (Class)var30.next();

            try {
               MythicTargeter annotation = (MythicTargeter)clazz.getAnnotation(MythicTargeter.class);
               namespace = annotation.namespace();
               String name = annotation.name();
               aliases = annotation.aliases();
               if (ISkillTargeter.class.isAssignableFrom(clazz)) {
                  Constructor constructor;
                  try {
                     constructor = clazz.getConstructor(MythicLineConfig.class);
                  } catch (NoSuchMethodException var21) {
                     constructor = clazz.getConstructor();
                  }

                  this.targeters.put(name.toUpperCase(), constructor);
                  this.targeters.put(namespace.toUpperCase() + ":" + name.toUpperCase(), constructor);
                  String[] var40 = aliases;
                  var41 = aliases.length;

                  for(int var42 = 0; var42 < var41; ++var42) {
                     String alias = var40[var42];
                     this.targeters.put(alias.toUpperCase(), constructor);
                     this.targeters.put(namespace.toUpperCase() + ":" + alias.toUpperCase(), constructor);
                  }
               }
            } catch (Throwable var25) {
               MythicLogger.error("Failed to register custom targeter {0}", new Object[]{clazz.getCanonicalName()});
               var25.printStackTrace();
            }
         }
      }

   }

   @EventHandler
   public void onMythicMechanicLoad(MythicMechanicLoadEvent event) {
      String name = event.getMechanicName().toUpperCase();
      if (this.mechanics.containsKey(name)) {
         Constructor constructor = (Constructor)this.mechanics.get(name);

         try {
            if (constructor.getParameterCount() == 1) {
               if (constructor.getParameterTypes()[0] == MythicLineConfig.class) {
                  event.register((ISkillMechanic)constructor.newInstance(event.getConfig()));
               } else {
                  event.register((ISkillMechanic)constructor.newInstance(event));
               }
            } else {
               event.register((ISkillMechanic)constructor.newInstance());
            }
         } catch (Exception var5) {
            MythicLogger.error("Failed to construct mechanic {0}", new Object[]{name});
            var5.printStackTrace();
         }
      }

   }

   @EventHandler
   public void onMythicConditionLoad(MythicConditionLoadEvent event) {
      String name = event.getConditionName().toUpperCase();
      if (this.conditions.containsKey(name)) {
         Constructor constructor = (Constructor)this.conditions.get(name);

         try {
            if (constructor.getParameterCount() == 1) {
               event.register((ISkillCondition)constructor.newInstance(event.getConfig()));
            } else {
               event.register((ISkillCondition)constructor.newInstance());
            }
         } catch (Exception var5) {
            MythicLogger.error("Failed to construct condition {0}", new Object[]{name});
            var5.printStackTrace();
         }
      }

   }

   @EventHandler
   public void onMythicTargeterLoad(MythicTargeterLoadEvent event) {
      String name = event.getTargeterName().toUpperCase();
      if (this.targeters.containsKey(name)) {
         Constructor constructor = (Constructor)this.targeters.get(name);

         try {
            if (constructor.getParameterCount() == 1) {
               event.register((ISkillTargeter)constructor.newInstance(event.getConfig()));
            } else {
               event.register((ISkillTargeter)constructor.newInstance());
            }
         } catch (Exception var5) {
            MythicLogger.error("Failed to construct targeter {0}", new Object[]{name});
            var5.printStackTrace();
         }
      }

   }
}
