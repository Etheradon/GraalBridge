package io.github.etheradon.graalbridge.scripting

import io.github.etheradon.graalbridge.scripting.utils.GraalUtilities
import io.github.etheradon.graalbridge.scripting.utils.typescript.TypeScriptHelper
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.Engine
import org.junit.platform.launcher.LauncherSession
import org.junit.platform.launcher.LauncherSessionListener
import java.nio.file.Path
import kotlin.io.path.absolutePathString

class GraalContextFactory : LauncherSessionListener {

    companion object {
        init {
            // This is the first line to be executed before any Graal related classes are loaded
            GraalUtilities.install()
        }

        private val engine = Engine.newBuilder()
            .option("engine.WarnInterpreterOnly", "false")
            .build()

        fun createContext(requirePath: Path? = null): Context {
            return Context.newBuilder("js").apply {
                engine(engine)
                allowExperimentalOptions(true)
                allowAllAccess(true)
                requirePath?.let {
                    option("js.commonjs-require", "true")
                    option("js.commonjs-require-cwd", it.absolutePathString())
                }
            }.build()
        }

        fun contextBuilder(): Context.Builder {
            return Context.newBuilder("js")
                .engine(engine)
                .allowAllAccess(true)
        }
    }

    override fun launcherSessionOpened(session: LauncherSession?) {
        // Loads the most important classes so that the times are not that much affected by the first load
        val ctx = createContext()
        ctx.eval("js", "1+1")
        ctx.close()
        TypeScriptHelper.transpileCode("", "test.ts")

        println("Session opened")
    }

    override fun launcherSessionClosed(session: LauncherSession?) {
        println("Session closed")
        engine.close()
    }
}
