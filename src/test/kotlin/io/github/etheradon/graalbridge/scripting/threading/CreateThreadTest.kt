package io.github.etheradon.graalbridge.scripting.threading

import io.github.etheradon.graalbridge.scripting.CommonTest
import io.github.etheradon.graalbridge.scripting.utils.GraalUtilities
import org.graalvm.polyglot.Source
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class CreateThreadTest : CommonTest() {

    private val message: String = "Multi threaded access worked"

    private fun createThread(): Pair<Throwable?, String?> {
        val latch = CountDownLatch(1)
        val exceptionRef = AtomicReference<Throwable>()
        val resultRef = AtomicReference<String>()

        bindings.putMember("latch", latch)
        bindings.putMember("res", resultRef)
        bindings.putMember("exceptionHandler", Thread.UncaughtExceptionHandler { _, e ->
            exceptionRef.set(e)
            latch.countDown()
        })

        @Language("js") val code = """
            var Thread = Java.type('java.lang.Thread');
            var t = new Thread(() => res.set("$message"));
            t.setUncaughtExceptionHandler(exceptionHandler);            
            t.start();
            t.join();
            latch.countDown();
        """.trimIndent()

        val src = Source.newBuilder("js", code, "test.js").build()
        context.eval(src)
        latch.await(1, TimeUnit.SECONDS)

        return Pair(exceptionRef.get(), resultRef.get())
    }

    @Test
    fun `Thread creation throws exception`() {
        GraalUtilities.isMultiThreadedAccessAllowed = false
        val (exception, result) = createThread()

        expectThat(exception).isNotNull().and {
            message.isNotNull().and { get { subject } contains "Multi threaded access requested" }
        }
        expectThat(result).isNull()
    }

    @Test
    fun `Thread creation does not throw exception`() {
        GraalUtilities.isMultiThreadedAccessAllowed = true
        val (exception, result) = createThread()

        expectThat(exception).isNull()
        expectThat(result).isNotNull().and { get { subject } isEqualTo message }
    }

}
