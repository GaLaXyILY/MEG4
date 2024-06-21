package com.ticxo.modelengine.v1_20_R4.network.utils;

import com.ticxo.modelengine.api.utils.data.NullableHashSet;
import java.util.Collection;
import java.util.LinkedHashSet;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketListenerPlayOut;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Packets
extends LinkedHashSet<PacketSupplier> {
    public Packets(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public Packets(int initialCapacity) {
        super(initialCapacity);
    }

    public Packets() {
    }

    public Packets(@NotNull Collection<? extends PacketSupplier> c) {
        super(c);
    }

    @Override
    public boolean add(PacketSupplier supplier) {
        if (supplier == null) {
            return false;
        }
        return super.add(supplier);
    }

    @Override
    public boolean add(Packet<PacketListenerPlayOut> packet) {
        return this.add((Player player) -> packet);
    }

    public Collection<Packet<PacketListenerPlayOut>> compile(Player player) {
        NullableHashSet<Packet<PacketListenerPlayOut>> set = new NullableHashSet<Packet<PacketListenerPlayOut>>();
        for (PacketSupplier supplier : this) {
            set.add(supplier.supply(player));
        }
        return set;
    }

    public static interface PacketSupplier {
        public Packet<PacketListenerPlayOut> supply(Player var1);
    }
}
