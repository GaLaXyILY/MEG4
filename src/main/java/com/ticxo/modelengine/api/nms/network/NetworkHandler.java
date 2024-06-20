package com.ticxo.modelengine.api.nms.network;

import io.netty.channel.ChannelPipeline;
import java.util.Optional;
import org.bukkit.entity.Player;

public interface NetworkHandler {
   int getProtocolVersion();

   Optional<ChannelPipeline> getPipeline(Player var1);

   void removePipeline(Player var1);

   void injectChannel(Player var1);

   void ejectChannel(Player var1);

   void startBatch();

   boolean isBatching();

   void endBatch();
}
