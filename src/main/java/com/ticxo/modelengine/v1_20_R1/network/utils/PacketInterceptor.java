package com.ticxo.modelengine.v1_20_R1.network.utils;

import com.google.common.collect.Maps;
import com.ticxo.modelengine.api.utils.logger.TLogger;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;

public class PacketInterceptor<P extends PacketListener> {
   private final Map<Class<? extends Packet<P>>, PacketInterceptor<P>.Modifier<? extends Packet<P>>> registry = Maps.newConcurrentMap();
   private final Map<Class<? extends Packet<P>>, PacketInterceptor<P>.Listener<? extends Packet<P>>> postRegistry = Maps.newConcurrentMap();

   public <T extends Packet<P>> PacketInterceptor<P> register(Class<T> clazz, Function<T, Packet<P>> function) {
      this.registry.put(clazz, new PacketInterceptor.Modifier(clazz, function));
      return this;
   }

   public <T extends Packet<P>> PacketInterceptor<P> registerPost(Class<T> clazz, Function<T, Collection<Packet<P>>> consumer) {
      this.postRegistry.put(clazz, new PacketInterceptor.Listener(clazz, consumer));
      return this;
   }

   public Packet<P> accept(Packet<P> original) {
      if (original == null) {
         return null;
      } else {
         PacketInterceptor<P>.Modifier<?> modifier = (PacketInterceptor.Modifier)this.registry.get(original.getClass());
         return modifier == null ? original : modifier.modify(original);
      }
   }

   public Collection<Packet<P>> acceptPost(Packet<P> original) {
      if (original == null) {
         return List.of();
      } else {
         PacketInterceptor<P>.Listener<?> listener = (PacketInterceptor.Listener)this.postRegistry.get(original.getClass());
         return (Collection)(listener != null ? listener.listen(original) : List.of());
      }
   }

   class Modifier<T extends Packet<P>> {
      private final Class<T> clazz;
      private final Function<T, Packet<P>> function;

      public Packet<P> modify(Packet<P> original) {
         try {
            return (Packet)this.function.apply((Packet)this.clazz.cast(original));
         } catch (Throwable var3) {
            TLogger.error("An error had occurred while modifying the packet " + this.clazz.getSimpleName());
            var3.printStackTrace();
            return original;
         }
      }

      public Modifier(Class<T> clazz, Function<T, Packet<P>> function) {
         this.clazz = clazz;
         this.function = function;
      }
   }

   class Listener<T extends Packet<P>> {
      private final Class<T> clazz;
      private final Function<T, Collection<Packet<P>>> function;

      public Collection<Packet<P>> listen(Packet<?> original) {
         try {
            Collection<Packet<P>> collection = (Collection)this.function.apply((Packet)this.clazz.cast(original));
            return (Collection)(collection == null ? List.of() : collection);
         } catch (Throwable var3) {
            TLogger.error("An error had occurred while intercepting the packet " + this.clazz.getSimpleName());
            var3.printStackTrace();
            return List.of();
         }
      }

      public Listener(Class<T> clazz, Function<T, Collection<Packet<P>>> function) {
         this.clazz = clazz;
         this.function = function;
      }
   }
}
