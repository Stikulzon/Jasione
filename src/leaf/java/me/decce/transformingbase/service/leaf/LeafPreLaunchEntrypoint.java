package me.decce.transformingbase.service.leaf;

import dev.aoqia.leaf.loader.api.entrypoint.PreLaunchEntrypoint;
import me.decce.transformingbase.core.Jasione;
import me.decce.transformingbase.service.mixin.MixinTransformerRegistration;
import org.spongepowered.asm.mixin.MixinEnvironment;

public class LeafPreLaunchEntrypoint implements PreLaunchEntrypoint {
    @Override
    public void onPreLaunch() {
        if (!Jasione.getConfig().enabled) {
            return;
        }
        MixinTransformerRegistration.register(MixinEnvironment.getCurrentEnvironment().getActiveTransformer());
    }
}
