package io.github.etheradon.graalbridge.scripting.typescript

import io.github.etheradon.graalbridge.scripting.CommonTest
import io.github.etheradon.graalbridge.scripting.GraalContextFactory
import org.graalvm.polyglot.Source
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import kotlin.io.path.Path

class TypeScriptTest : CommonTest() {

    @Test
    fun `Simple TypeScript`() {
        @Language("TypeScript")
        val code = """
            function add(a: number, b: number): number {
                return a + b;
            }
            let sum: number = add(-5, 8);
        """.trimIndent()
        val source = Source.newBuilder("js", code, "test.ts").build()
        context.eval(source)
        expectThat(context.eval("js", "sum").asInt()).isEqualTo(3)
    }

    @Test
    fun `TypeScript Imports`() {
        val workingDir = Path("src/test/resources/typescript/import")
        val source = Source.newBuilder("js", workingDir.resolve("index.ts").toFile())
            .mimeType("application/javascript+module")
            .build()

        val context = GraalContextFactory.createContext(workingDir)

        val results = context.eval(source)
        expectThat(results.getMember("loadInternalVal").asDouble()).isEqualTo(2.8284271247461903)
        expectThat(results.getMember("loadExternalVal").asDouble()).isEqualTo(0.6180300000000001)
        expectThat(results.getMember("importVal").asDouble()).isEqualTo(9.869587728099999)
        expectThat(results.getMember("requireVal").asDouble()).isEqualTo(1.35914)
        expectThat(results.getMember("jsonVal").asString()).isEqualTo("Worked")
    }
}
