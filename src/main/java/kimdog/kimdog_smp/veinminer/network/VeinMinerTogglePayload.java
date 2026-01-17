package kimdog.kimdog_smp.veinminer.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public class VeinMinerTogglePayload implements CustomPayload {
    public static final CustomPayload.Id<VeinMinerTogglePayload> ID = new CustomPayload.Id<>(
            Identifier.of("kimdog", "veinminer_toggle")
    );

    public static final PacketCodec<PacketByteBuf, VeinMinerTogglePayload> CODEC =
            PacketCodec.of(VeinMinerTogglePayload::write, VeinMinerTogglePayload::new);

    private final boolean enabled;

    public VeinMinerTogglePayload(boolean enabled) {
        this.enabled = enabled;
    }

    public VeinMinerTogglePayload(PacketByteBuf buf) {
        this.enabled = buf.readBoolean();
    }

    public void write(PacketByteBuf buf) {
        buf.writeBoolean(enabled);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
