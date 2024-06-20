package com.ticxo.modelengine.api.model;

import com.google.common.collect.Maps;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.entity.BaseEntity;
import com.ticxo.modelengine.api.entity.data.IEntityData;
import com.ticxo.modelengine.api.model.render.ModelRenderer;
import com.ticxo.modelengine.api.nms.RenderParsers;
import com.ticxo.modelengine.api.utils.config.ConfigProperty;
import com.ticxo.modelengine.api.utils.data.io.SavedData;
import com.ticxo.modelengine.api.utils.logger.TLogger;
import com.ticxo.modelengine.api.utils.scheduling.PlatformScheduler;
import com.ticxo.modelengine.api.utils.ticker.AbstractLoadBalancer;
import com.ticxo.modelengine.api.utils.ticker.LoadBalancer;
import com.ticxo.modelengine.api.utils.ticker.PseudoThread;
import com.ticxo.modelengine.api.utils.ticker.Task;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Consumer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class ModelUpdaters extends AbstractLoadBalancer<UUID, ModelUpdaters.Updater> {
   private final Map<ModelUpdaters.Updater, PseudoThread> threads = new HashMap();
   private final JavaPlugin plugin;
   private final PlatformScheduler scheduler;
   private final int maximumThreads;
   private final Map<Integer, UUID> entityIdLookup = Maps.newConcurrentMap();
   private int trackerCount = 0;
   private boolean started;

   public ModelUpdaters(JavaPlugin plugin, PlatformScheduler scheduler) {
      super(ConfigProperty.ENGINE_THREADS.getInt());
      this.plugin = plugin;
      this.scheduler = scheduler;
      this.maximumThreads = Math.max(ConfigProperty.MAX_ENGINE_THREADS.getInt(), this.available.size());
      this.available.forEach(this::setup);
   }

   public ModelUpdaters.Updater supply() {
      return new ModelUpdaters.Updater();
   }

   public void start() {
      this.started = true;
      Iterator var1 = this.threads.entrySet().iterator();

      while(var1.hasNext()) {
         Entry<ModelUpdaters.Updater, PseudoThread> entry = (Entry)var1.next();
         ((PseudoThread)entry.getValue()).start();
      }

   }

   public void end() {
      Iterator var1 = this.threads.entrySet().iterator();

      while(var1.hasNext()) {
         Entry<ModelUpdaters.Updater, PseudoThread> entry = (Entry)var1.next();
         ((PseudoThread)entry.getValue()).end();
      }

   }

   public Optional<ModeledEntity> registerModeledEntity(BaseEntity<?> base, ModeledEntity modeledEntity) {
      return ((ModelUpdaters.Updater)this.getOrRegister(base.getUUID())).registerModeledEntity(base, modeledEntity);
   }

   public ModeledEntity getModeledEntity(int id) {
      UUID uuid = (UUID)this.entityIdLookup.get(id);
      return this.getModeledEntity(uuid);
   }

   public ModeledEntity getModeledEntity(UUID uuid) {
      ModelUpdaters.Updater updater = (ModelUpdaters.Updater)this.get(uuid);
      return updater == null ? null : updater.getModeledEntity(uuid);
   }

   public ModeledEntity removeModeledEntity(int id) {
      UUID uuid = (UUID)this.entityIdLookup.get(id);
      return this.removeModeledEntity(uuid);
   }

   public ModeledEntity removeModeledEntity(UUID uuid) {
      ModelUpdaters.Updater updater = (ModelUpdaters.Updater)this.get(uuid);
      return updater == null ? null : updater.removeModeledEntity(uuid);
   }

   public void forceRemoveModeledEntity(ModeledEntity model) {
      ((ModelUpdaters.Updater)this.get(model.getBase().getUUID())).forceRemoveModeledEntity(model);
   }

   public void saveAllModels() {
      this.reference.forEach((uuid, updater) -> {
         updater.saveAllModels();
      });
   }

   private void setup(ModelUpdaters.Updater updater) {
      updater.setId("model_updater_" + this.trackerCount);
      PseudoThread thread = new PseudoThread(updater.getId(), this.scheduler, this.plugin, true, 0, 0, false, false);
      thread.registerOverloadCallback((skipped) -> {
         String var10000 = updater.getId();
         TLogger.debug(var10000 + " is overloaded with " + updater.getLoad() + " targets.");
         if (this.available.size() < this.maximumThreads) {
            this.growAndBalance(updater, skipped);
         }

      });
      thread.queueTask(new Task((task) -> {
         updater.updateAllModels();
      }, 0, 0, true));
      this.threads.put(updater, thread);
      if (this.started) {
         thread.start();
      }

      ++this.trackerCount;
   }

   private void growAndBalance(ModelUpdaters.Updater request, int skipped) {
      TLogger.debug(request.id + " has skipped " + skipped + " ticks. Requested a new server.");
      ModelUpdaters.Updater handle = this.supply();
      int moved = request.uuidLookup.size() / 2;

      for(Iterator var5 = request.uuidLookup.entrySet().iterator(); var5.hasNext(); --moved) {
         Entry<UUID, ModeledEntity> entry = (Entry)var5.next();
         if (moved <= 0) {
            break;
         }

         UUID uuid = (UUID)entry.getKey();
         ModeledEntity modeledEntity = (ModeledEntity)entry.getValue();
         handle.registerModeledEntity(modeledEntity.getBase(), modeledEntity);
         this.reference.put(uuid, handle);
      }

      handle.uuidLookup.forEach((uuidx, modeledEntityx) -> {
         request.uuidLookup.remove(uuidx);
      });
      this.available.add(handle);
      this.setup(handle);
      TLogger.debug("- Created " + handle.id + ".");
   }

   public class Updater implements LoadBalancer.Server {
      private final Map<UUID, ModeledEntity> uuidLookup = Maps.newConcurrentMap();
      private final RenderParsers parsers = ModelEngineAPI.getNMSHandler().createParsers();
      private String id;
      private int lastSize = 0;
      private long timings;

      public void updateAllModels() {
         long time = System.currentTimeMillis();
         Iterator var3 = this.uuidLookup.entrySet().iterator();

         while(var3.hasNext()) {
            Entry<UUID, ModeledEntity> entry = (Entry)var3.next();
            ModeledEntity entity = (ModeledEntity)entry.getValue();
            entity.getBase().getData().asyncUpdate();
            if (!entity.tick()) {
               this.forceRemoveModeledEntity(entity);
            } else {
               this.forRenderers(entity, (modelRenderer) -> {
                  modelRenderer.sendToClient(this.parsers);
               });
            }
         }

         this.timings = System.currentTimeMillis() - time;
         if (this.lastSize != this.uuidLookup.size()) {
            this.lastSize = this.uuidLookup.size();
            TLogger.debug(this.id + ": " + this.lastSize + " - " + this.timings);
         }

      }

      private void forRenderers(ModeledEntity modeledEntity, Consumer<ModelRenderer> rendererConsumer) {
         IEntityData data = modeledEntity.getBase().getData();
         Map<String, ActiveModel> models = modeledEntity.getModels();
         if (!models.isEmpty()) {
            Iterator var5 = models.values().iterator();

            ActiveModel model;
            while(var5.hasNext()) {
               model = (ActiveModel)var5.next();
               if (!model.getModelRenderer().isInitialized()) {
                  return;
               }
            }

            var5 = models.values().iterator();

            while(var5.hasNext()) {
               model = (ActiveModel)var5.next();
               rendererConsumer.accept(model.getModelRenderer());
            }

            data.cleanup();
         }
      }

      public Optional<ModeledEntity> registerModeledEntity(BaseEntity<?> base, ModeledEntity modeledEntity) {
         ModelUpdaters.this.entityIdLookup.put(base.getEntityId(), base.getUUID());
         return Optional.ofNullable((ModeledEntity)this.uuidLookup.put(base.getUUID(), modeledEntity));
      }

      public ModeledEntity getModeledEntity(int id) {
         return this.getModeledEntity((UUID)ModelUpdaters.this.entityIdLookup.get(id));
      }

      public ModeledEntity getModeledEntity(UUID uuid) {
         return uuid == null ? null : (ModeledEntity)this.uuidLookup.get(uuid);
      }

      public ModeledEntity removeModeledEntity(int id) {
         return this.removeModeledEntity((UUID)ModelUpdaters.this.entityIdLookup.get(id));
      }

      public ModeledEntity removeModeledEntity(UUID uuid) {
         if (uuid == null) {
            return null;
         } else {
            ModeledEntity model = (ModeledEntity)this.uuidLookup.get(uuid);
            if (model != null) {
               model.markRemoved();
            }

            return model;
         }
      }

      public void forceRemoveModeledEntity(ModeledEntity model) {
         this.uuidLookup.remove(model.getBase().getUUID());
         ModelUpdaters.this.entityIdLookup.remove(model.getBase().getEntityId());
         model.getBase().setForcedAlive(false);
         model.destroy();
         ModelUpdaters.this.unregister(model.getBase().getUUID());
      }

      public Set<UUID> getAllModeledEntityUUID() {
         return this.uuidLookup.keySet();
      }

      public void saveAllModels() {
         Iterator var1 = this.uuidLookup.values().iterator();

         while(var1.hasNext()) {
            ModeledEntity model = (ModeledEntity)var1.next();
            Object var4 = model.getBase().getOriginal();
            if (var4 instanceof Entity) {
               Entity entity = (Entity)var4;
               if (!(entity instanceof Player)) {
                  model.save().ifPresent((data) -> {
                     entity.getPersistentDataContainer().set(SavedData.DATA_KEY, PersistentDataType.STRING, data.toString());
                  });
               }
            }
         }

      }

      public int getLoad() {
         return this.uuidLookup.size();
      }

      public String getId() {
         return this.id;
      }

      public void setId(String id) {
         this.id = id;
      }

      public long getTimings() {
         return this.timings;
      }
   }
}
