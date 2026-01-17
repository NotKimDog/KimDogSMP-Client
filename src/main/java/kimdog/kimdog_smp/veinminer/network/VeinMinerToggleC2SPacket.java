package kimdog.kimdog_smp.veinminer.network;

import net.minecraft.network.PacketByteBuf;

public class VeinMinerToggleC2SPacket {
    private final boolean on;

    public VeinMinerToggleC2SPacket(boolean on) {
        this.on = on;
    }

    public void write(PacketByteBuf buf) {
        buf.writeBoolean(on);
    }

    public static VeinMinerToggleC2SPacket read(PacketByteBuf buf) {
        return new VeinMinerToggleC2SPacket(buf.readBoolean());
    }

    public boolean isOn() {
        return on;
    }
}
