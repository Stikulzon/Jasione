package me.decce.transformingbase.service.fabric;

//? if fabric {
import me.decce.transformingbase.core.Jasione;
import me.decce.transformingbase.service.mixin.MixinTransformerRegistration;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import org.spongepowered.asm.mixin.MixinEnvironment;

public class FabricPreLaunchEntrypoint implements PreLaunchEntrypoint {
    @Override
    public void onPreLaunch() {
        if (!Jasione.getConfig().enabled) {
            return;
        }
        var transformer = MixinEnvironment.getCurrentEnvironment().getActiveTransformer();
        if ("dev.jfronny.libjf.unsafe.asm.AsmTransformer".equals(transformer.getClass().getName())) {
            // LibJF is in use - skip, we'll use the ASM functionalities provided by it (see LibJFEntrypoint)
            return;
        }
        MixinTransformerRegistration.register(transformer);
    }
}
//?}
