package io.github.etheradon.graalbridge.scripting.patches

import io.github.etheradon.graalbridge.scripting.utils.GraalUtilities
import net.lenni0451.classtransform.annotations.CTransformer

@CTransformer(name = ["com.oracle.truffle.js.lang.JavaScriptLanguage"])
class ThreadingPatch {

    fun isThreadAccessAllowed(thread: Thread, singleThreaded: Boolean): Boolean {
        return singleThreaded || GraalUtilities.isMultiThreadedAccessAllowed
    }

}
