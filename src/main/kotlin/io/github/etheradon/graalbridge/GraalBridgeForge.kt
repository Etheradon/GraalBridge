package io.github.etheradon.graalbridge

import net.minecraftforge.fml.common.Mod

@Mod("graalbridge")
class GraalBridgeForge {

    init {
        GraalBridge.init(GraalBridge.ModLoader.FORGE)
    }

}
