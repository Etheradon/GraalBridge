package io.github.etheradon.graalbridge.scripting.patches

import net.lenni0451.classtransform.annotations.CShadow
import net.lenni0451.classtransform.annotations.CTarget
import net.lenni0451.classtransform.annotations.CTransformer
import net.lenni0451.classtransform.annotations.injection.CInject
import java.io.File

// Mixin canÄt find the class in production
@CTransformer(name = ["org.graalvm.polyglot.Source\$Builder"])
class SourceBuilderPatch {

    @CShadow
    private val origin: Any? = null

    @CShadow
    private var mimeType: String? = null

    @CInject(
        method = ["build"],
        target = [CTarget("HEAD")],
        cancellable = true
    )
    fun build() {
        if (origin is File && origin.name.endsWith(".ts")) {
            this.mimeType = "application/javascript+module"
        }
    }

}
