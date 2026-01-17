package kimdog.kimdog_smp.zoom.mixin;

import kimdog.kimdog_smp.zoom.ZoomClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseScrollMixin {

    @Inject(method = "onMouseScroll", at = @At("HEAD"))
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        // Handle zoom scroll
        if (ZoomClient.isZooming()) {
            ZoomClient.handleScroll(vertical);
        }
    }
}
