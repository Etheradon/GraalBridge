package io.github.etheradon.graalbridge

import net.fabricmc.api.ModInitializer

object GraalBridgeFabric : ModInitializer {

    override fun onInitialize() {
        GraalBridge.init(GraalBridge.ModLoader.FABRIC)
    }

}
