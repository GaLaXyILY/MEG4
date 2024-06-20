package com.ticxo.modelengine.api.utils;

import com.google.common.collect.Maps;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.Nullable;

public class ReflectionUtils {
   private static final Map<Class<?>, Class<?>> PRIMITIVE_EQUIV = new HashMap<Class<?>, Class<?>>() {
      {
         this.put(Byte.TYPE, Byte.class);
         this.put(Short.TYPE, Short.class);
         this.put(Integer.TYPE, Integer.class);
         this.put(Long.TYPE, Long.class);
         this.put(Float.TYPE, Float.class);
         this.put(Double.TYPE, Double.class);
         this.put(Boolean.TYPE, Boolean.class);
         this.put(Character.TYPE, Character.class);
      }
   };
   private static final Map<Class<?>, ConcurrentHashMap<String, ReflectionUtils.ReflectionEnum>> DYNAMIC_FIELDS = Maps.newConcurrentMap();
   private static final Map<Class<?>, ConcurrentHashMap<ReflectionUtils.ReflectionEnum, Field>> FIELD_MAP = Maps.newConcurrentMap();
   private static final Map<Class<?>, ConcurrentHashMap<ReflectionUtils.MethodEnum, Method>> METHOD_MAP = Maps.newConcurrentMap();
   private static final boolean IS_MAPPED = Boolean.getBoolean("modelengine.mapped");

   public static Field unlockField(ReflectionUtils.ReflectionEnum field) {
      try {
         Field f = field.getTarget().getDeclaredField(field.get(IS_MAPPED));
         f.setAccessible(true);
         return f;
      } catch (SecurityException | IllegalArgumentException | NoSuchFieldException var2) {
         throw new RuntimeException("An error occurred while unlocking field: " + field.getMapped(), var2);
      }
   }

   public static Method unlockMethod(ReflectionUtils.MethodEnum method) {
      try {
         Method m = method.getTarget().getDeclaredMethod(method.get(IS_MAPPED), method.getParameterClasses());
         m.setAccessible(true);
         return m;
      } catch (SecurityException | IllegalArgumentException | NoSuchMethodException var2) {
         throw new RuntimeException("An error occurred while unlocking method: " + method.getMapped(), var2);
      }
   }

   public static Field getField(Class<?> clazz, String field) {
      return getField((ReflectionUtils.ReflectionEnum)((ConcurrentHashMap)DYNAMIC_FIELDS.computeIfAbsent(clazz, (aClass) -> {
         return new ConcurrentHashMap();
      })).computeIfAbsent(field, (s) -> {
         return new ReflectionUtils.RuntimeReflection(clazz, field);
      }));
   }

   public static Field getField(ReflectionUtils.ReflectionEnum field) {
      return (Field)((ConcurrentHashMap)FIELD_MAP.computeIfAbsent(field.getTarget(), (aClass) -> {
         return new ConcurrentHashMap();
      })).computeIfAbsent(field, ReflectionUtils::unlockField);
   }

   public static Method getMethod(ReflectionUtils.MethodEnum method) {
      return (Method)((ConcurrentHashMap)METHOD_MAP.computeIfAbsent(method.getTarget(), (aClass) -> {
         return new ConcurrentHashMap();
      })).computeIfAbsent(method, ReflectionUtils::unlockMethod);
   }

   @Nullable
   public static <T> T get(Object object, ReflectionUtils.ReflectionEnum field) {
      try {
         return getField(field).get(object);
      } catch (IllegalAccessException var3) {
         var3.printStackTrace();
         return null;
      }
   }

   @Nullable
   public static <T> T get(Object object, ReflectionUtils.ReflectionEnum field, T def) {
      try {
         return getField(field).get(object);
      } catch (IllegalAccessException var4) {
         var4.printStackTrace();
         return def;
      }
   }

   @Nullable
   public static <T> T get(ReflectionUtils.ReflectionEnum field) {
      return get((Object)null, field);
   }

   public static boolean set(Object object, ReflectionUtils.ReflectionEnum field, Object val) {
      try {
         getField(field).set(object, val);
         return true;
      } catch (IllegalAccessException var4) {
         var4.printStackTrace();
         return false;
      }
   }

   public static boolean set(ReflectionUtils.ReflectionEnum field, Object val) {
      return set((Object)null, field, val);
   }

   public static <T> T call(Object object, ReflectionUtils.MethodEnum method, Object... parameters) {
      try {
         Class<?>[] paramClasses = method.getParameterClasses();
         if (paramClasses.length > parameters.length) {
            throw new RuntimeException(String.format("Invalid method call: Missing parameters. Expected %s, got %s.", paramClasses.length, parameters.length));
         } else {
            for(int i = 0; i < paramClasses.length; ++i) {
               Class<?> givenClass = parameters[i].getClass();
               Class<?> expectClass = paramClasses[i];
               if (!expectClass.isAssignableFrom(givenClass) && isDifferentPrimitive(givenClass, expectClass)) {
                  throw new RuntimeException(String.format("Invalid method call: Invalid parameter at position %s. Expected %s, got %s.", i, expectClass.getSimpleName(), givenClass.getSimpleName()));
               }
            }

            Method m = getMethod(method);
            return m.invoke(object, parameters);
         }
      } catch (InvocationTargetException | IllegalAccessException var7) {
         var7.printStackTrace();
         return null;
      }
   }

   public static <T> T call(ReflectionUtils.MethodEnum method, Object... parameters) {
      return call((Object)null, method, parameters);
   }

   private static boolean isDifferentPrimitive(Class<?> givenClass, Class<?> expectClass) {
      if (!expectClass.isPrimitive()) {
         return true;
      } else {
         Class<?> boxed = (Class)PRIMITIVE_EQUIV.get(expectClass);
         return !boxed.isAssignableFrom(givenClass);
      }
   }

   public interface ReflectionEnum {
      Class<?> getTarget();

      String getObfuscated();

      String getMapped();

      default String get(boolean mapped) {
         return mapped ? this.getMapped() : this.getObfuscated();
      }
   }

   public interface MethodEnum extends ReflectionUtils.ReflectionEnum {
      Class<?>[] getParameterClasses();
   }

   static class RuntimeReflection implements ReflectionUtils.ReflectionEnum {
      private final Class<?> target;
      private final String field;

      public String getObfuscated() {
         return this.field;
      }

      public String getMapped() {
         return this.field;
      }

      public RuntimeReflection(Class<?> target, String field) {
         this.target = target;
         this.field = field;
      }

      public Class<?> getTarget() {
         return this.target;
      }

      public String getField() {
         return this.field;
      }
   }
}
