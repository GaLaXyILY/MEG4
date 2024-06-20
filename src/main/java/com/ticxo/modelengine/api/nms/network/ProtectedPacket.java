package com.ticxo.modelengine.api.nms.network;

public record ProtectedPacket(Object packet) {
   public ProtectedPacket(Object packet) {
      this.packet = packet;
   }

   public Object packet() {
      return this.packet;
   }
}
