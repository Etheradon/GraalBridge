package io.github.etheradon.graalbridge.scripting

import org.graalvm.polyglot.Context
import org.graalvm.polyglot.Source
import org.graalvm.polyglot.Value
import org.junit.jupiter.api.AfterEach

open class CommonTest {

    private val contextDelegate = lazy { GraalContextFactory.createContext() }
    protected val context: Context by contextDelegate
    protected val bindings: Value by lazy { context.getBindings("js") }

    protected fun evalJsCode(jsCode: String): Value {
        val source = Source.newBuilder("js", jsCode, "test.js").build()
        return context.eval(source)
    }

    @AfterEach
    fun tearDown() {
        if (contextDelegate.isInitialized()) {
            context.close(true)
        }
    }

}
