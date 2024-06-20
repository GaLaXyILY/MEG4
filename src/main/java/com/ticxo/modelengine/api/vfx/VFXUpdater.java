package com.ticxo.modelengine.api.vfx;

import com.google.common.collect.Maps;
import com.ticxo.modelengine.api.entity.BaseEntity;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

public class VFXUpdater {
   private final Map<Integer, UUID> entityIdLookup = Maps.newConcurrentMap();
   private final Map<UUID, VFX> uuidLookup = Maps.newConcurrentMap();

   public void updateAllVFXs() {
      Iterator var1 = this.uuidLookup.entrySet().iterator();

      while(var1.hasNext()) {
         Entry<UUID, VFX> entry = (Entry)var1.next();
         VFX entity = (VFX)entry.getValue();
         if (entity.isInitialized()) {
            if (!entity.tick()) {
               this.uuidLookup.remove(entry.getKey());
               this.entityIdLookup.remove(entity.getBase().getEntityId());
               entity.getRenderer().destroy();
               entity.getBase().getData().cleanup();
               entity.getBase().setForcedAlive(false);
               entity.destroy();
            } else {
               entity.getRenderer().sendToClient();
               entity.getBase().getData().cleanup();
            }
         }
      }

   }

   public void registerVFX(BaseEntity<?> base, VFX vfx) {
      this.entityIdLookup.put(base.getEntityId(), base.getUUID());
      this.uuidLookup.put(base.getUUID(), vfx);
   }

   public VFX getVFX(int id) {
      return this.getVFX((UUID)this.entityIdLookup.get(id));
   }

   public VFX getVFX(UUID uuid) {
      return uuid == null ? null : (VFX)this.uuidLookup.get(uuid);
   }

   public VFX removeVFX(int id) {
      return this.removeVFX((UUID)this.entityIdLookup.get(id));
   }

   public VFX removeVFX(UUID uuid) {
      if (uuid == null) {
         return null;
      } else {
         VFX vfx = (VFX)this.uuidLookup.get(uuid);
         if (vfx != null) {
            vfx.markRemoved();
         }

         return vfx;
      }
   }
}
