package kimdog.kimdog_smp.zoom.mixin;

import kimdog.kimdog_smp.zoom.ZoomClient;
import net.minecraft.client.option.GameOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(GameOptions.class)
public class GameOptionsZoomMixin {

    @ModifyVariable(method = "<init>", at = @At("TAIL"), ordinal = 0)
    public GameOptions onInit(GameOptions options) {
        // This ensures the mixin is applied safely during initialization
        return options;
    }
}
