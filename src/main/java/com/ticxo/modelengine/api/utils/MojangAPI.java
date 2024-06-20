package com.ticxo.modelengine.api.utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.utils.data.GSONUtils;
import com.ticxo.modelengine.api.utils.math.TMath;
import com.ticxo.modelengine.api.utils.promise.Promise;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.profile.PlayerTextures;
import org.bukkit.profile.PlayerTextures.SkinModel;
import org.jetbrains.annotations.NotNull;

public class MojangAPI {
   private static final HttpClient CLIENT = HttpClient.newHttpClient();
   private static final Map<String, UUID> UUID_CACHE = new ConcurrentHashMap();
   private static final Map<UUID, String> DATA_CACHE = new ConcurrentHashMap();

   private MojangAPI() {
      throw new IllegalStateException();
   }

   public static PlayerProfile fromBase64(@NotNull String data) {
      PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
      String decoded = new String(Base64.getDecoder().decode(data));
      JsonObject root = (JsonObject)ModelEngineAPI.getAPI().getGson().fromJson(decoded, JsonObject.class);
      GSONUtils.ifPresent(root, "textures", (element) -> {
         GSONUtils.ifPresent(element, "SKIN", (element1) -> {
            String url = (String)GSONUtils.get(element1, "url", JsonElement::getAsString);
            if (url == null) {
               throw new NullPointerException("Skin URL cannot be null.");
            } else {
               JsonObject metadata = (JsonObject)GSONUtils.get(element1, "metadata", JsonElement::getAsJsonObject);
               boolean slim = (Boolean)GSONUtils.get(metadata, "model", (element2) -> {
                  return element2.getAsString().equals("slim");
               }, false);

               try {
                  PlayerTextures texture = profile.getTextures();
                  texture.setSkin(new URL(url), slim ? SkinModel.SLIM : SkinModel.CLASSIC);
                  profile.setTextures(texture);
               } catch (MalformedURLException var6) {
                  throw new RuntimeException(var6);
               }
            }
         });
      });
      return profile;
   }

   public static PlayerProfile fromUUID(UUID uuid) {
      String data = (String)DATA_CACHE.computeIfAbsent(uuid, (uuid1) -> {
         try {
            HttpRequest request = HttpRequest.newBuilder().uri(new URI("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid)).timeout(Duration.of(30L, ChronoUnit.SECONDS)).GET().build();
            HttpClient client = HttpClient.newHttpClient();
            String result = (String)client.sendAsync(request, BodyHandlers.ofString()).thenApply(HttpResponse::body).get(30L, TimeUnit.SECONDS);
            JsonObject root = (JsonObject)ModelEngineAPI.getAPI().getGson().fromJson(result, JsonObject.class);
            return (String)GSONUtils.get(root, "properties", (element) -> {
               JsonArray array = element.getAsJsonArray();
               return array.isEmpty() ? null : (String)GSONUtils.get(array.get(0), "value", JsonElement::getAsString);
            });
         } catch (Throwable var6) {
            throw new RuntimeException(var6);
         }
      });
      return data == null ? null : fromBase64(data);
   }

   public static UUID getUUIDFromUsername(String username) {
      return (UUID)UUID_CACHE.computeIfAbsent(username, (s) -> {
         try {
            long time = System.currentTimeMillis() / 1000L;
            HttpRequest request = HttpRequest.newBuilder().uri(new URI("https://api.mojang.com/users/profiles/minecraft/" + username + "?at=" + time)).timeout(Duration.of(30L, ChronoUnit.SECONDS)).GET().build();
            String result = (String)CLIENT.sendAsync(request, BodyHandlers.ofString()).thenApply(HttpResponse::body).get(30L, TimeUnit.SECONDS);
            JsonObject root = (JsonObject)ModelEngineAPI.getAPI().getGson().fromJson(result, JsonObject.class);
            return (UUID)GSONUtils.get(root, "id", (element) -> {
               return TMath.parseUUID(element.getAsString());
            });
         } catch (Throwable var7) {
            throw new RuntimeException(var7);
         }
      });
   }

   public static Promise<PlayerProfile> fromUUIDPromise(UUID uuid) {
      return Promise.supplyingAsync(() -> {
         return fromUUID(uuid);
      });
   }

   public static Promise<UUID> getUUIDFromUsernamePromise(String name) {
      return Promise.supplyingAsync(() -> {
         return getUUIDFromUsername(name);
      });
   }
}
