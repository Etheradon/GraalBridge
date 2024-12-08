package io.github.etheradon.graalbridge

import net.neoforged.fml.common.Mod

@Mod("graalbridge")
class GraalbridgeNeoForge {

    init {
        GraalBridge.init(GraalBridge.ModLoader.NEO_FORGE)
    }

}
