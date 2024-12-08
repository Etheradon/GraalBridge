package io.github.etheradon.graalbridge

import io.github.etheradon.graalbridge.scripting.utils.GraalUtilities

object GraalBridge {

    enum class ModLoader {
        FABRIC,
        FORGE,
        NEO_FORGE,
        QUILT
    }

    lateinit var modLoader: ModLoader

    fun init(modLoader: ModLoader) {
        this.modLoader = modLoader
        GraalUtilities.install()
    }

}
