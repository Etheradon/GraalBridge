package io.github.etheradon.graalbridge.mixin;

import io.github.etheradon.graalbridge.jsmacros.FGraal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.wagyourtail.jsmacros.core.Core;
import xyz.wagyourtail.jsmacros.core.config.BaseProfile;

@Mixin(value = BaseProfile.class, remap = false)
public class BaseProfileMixin {

    @Shadow
    @Final
    protected Core runner;

    @Inject(method = "initRegistries", at = @At("TAIL"))
    public void initRegistries(CallbackInfo ci) {
        runner.libraryRegistry.addLibrary(FGraal.class);
    }

}
