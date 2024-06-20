package com.ticxo.modelengine.core.mythic.targeters;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.model.bone.ModelBone;
import com.ticxo.modelengine.api.utils.OffsetMode;
import com.ticxo.modelengine.api.utils.logger.TLogger;
import com.ticxo.modelengine.core.mythic.MythicUtils;
import com.ticxo.modelengine.core.mythic.utils.MythicTargeter;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.SkillCaster;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import io.lumine.mythic.api.skills.targeters.ILocationTargeter;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.core.skills.placeholders.PlaceholderMeta;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map.Entry;
import org.joml.Vector3f;

@MythicTargeter(
   name = "modelpart",
   aliases = {}
)
public class ModelPartTargeter implements ILocationTargeter {
   private final PlaceholderString modelId;
   private final PlaceholderString partId;
   private final PlaceholderString offset;
   private final boolean exactMatch;
   private final boolean scale;
   private PlaceholderDouble x;
   private PlaceholderDouble y;
   private PlaceholderDouble z;

   public ModelPartTargeter(MythicLineConfig mlc) {
      this.modelId = mlc.getPlaceholderString(new String[]{"m", "mid", "model", "modelid"}, (String)null, new String[0]);
      this.partId = mlc.getPlaceholderString(new String[]{"p", "pid", "part", "partid"}, (String)null, new String[0]);
      this.offset = mlc.getPlaceholderString(new String[]{"o", "off", "offset"}, "LOCAL", new String[0]);
      String coords = mlc.getString(new String[]{"location", "loc", "l", "coordinates", "c"}, (String)null, new String[0]);
      if (coords != null) {
         String[] split = coords.split(",");

         try {
            this.x = PlaceholderDouble.of(split[0]);
            this.y = PlaceholderDouble.of(split[1]);
            this.z = PlaceholderDouble.of(split[2]);
         } catch (Exception var5) {
            TLogger.error("The 'coordinates' attribute must be in the format c=x,y,z.");
            this.x = PlaceholderDouble.of("0");
            this.y = PlaceholderDouble.of("0");
            this.z = PlaceholderDouble.of("0");
         }
      } else {
         this.x = mlc.getPlaceholderDouble("x", 0.0D);
         this.y = mlc.getPlaceholderDouble("y", 0.0D);
         this.z = mlc.getPlaceholderDouble("z", 0.0D);
      }

      this.exactMatch = mlc.getBoolean(new String[]{"em", "exact", "match", "exactmatch"}, true);
      this.scale = mlc.getBoolean(new String[]{"s", "sc", "scale"}, true);
   }

   public Collection<AbstractLocation> getLocations(SkillMetadata skillMetadata) {
      HashSet<AbstractLocation> targets = new HashSet();
      SkillCaster caster = skillMetadata.getCaster();
      ModeledEntity model = ModelEngineAPI.getModeledEntity(caster.getEntity().getUniqueId());
      if (model == null) {
         return targets;
      } else {
         String modelId = MythicUtils.getOrNullLowercase(this.modelId, (PlaceholderMeta)skillMetadata);
         OffsetMode offsetMode = OffsetMode.get(this.offset.get(skillMetadata).toUpperCase(Locale.ENGLISH));
         Vector3f offset = (new Vector3f()).set(this.x.get(skillMetadata), this.y.get(skillMetadata), this.z.get(skillMetadata));
         model.getModel(modelId).ifPresentOrElse((activeModel) -> {
            String partId = MythicUtils.getOrNullLowercase(this.partId, (PlaceholderMeta)skillMetadata);
            if (this.exactMatch) {
               activeModel.getBone(partId).ifPresent((modelBone) -> {
                  targets.add(BukkitAdapter.adapt(modelBone.getLocation(offsetMode, offset, this.scale)));
               });
            } else {
               Iterator var7 = activeModel.getBones().entrySet().iterator();

               while(var7.hasNext()) {
                  Entry<String, ModelBone> entry = (Entry)var7.next();
                  if (((String)entry.getKey()).contains(partId)) {
                     targets.add(BukkitAdapter.adapt(((ModelBone)entry.getValue()).getLocation(offsetMode, offset, this.scale)));
                  }
               }
            }

         }, () -> {
            model.getModels().values().forEach((activeModel) -> {
               String partId = MythicUtils.getOrNullLowercase(this.partId, (PlaceholderMeta)skillMetadata);
               if (this.exactMatch) {
                  activeModel.getBone(partId).ifPresent((modelBone) -> {
                     targets.add(BukkitAdapter.adapt(modelBone.getLocation(offsetMode, offset, this.scale)));
                  });
               } else {
                  Iterator var7 = activeModel.getBones().entrySet().iterator();

                  while(var7.hasNext()) {
                     Entry<String, ModelBone> entry = (Entry)var7.next();
                     if (((String)entry.getKey()).contains(partId)) {
                        targets.add(BukkitAdapter.adapt(((ModelBone)entry.getValue()).getLocation(offsetMode, offset, this.scale)));
                     }
                  }
               }

            });
         });
         return targets;
      }
   }
}
