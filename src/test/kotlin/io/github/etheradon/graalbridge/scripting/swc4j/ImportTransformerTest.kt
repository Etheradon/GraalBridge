package io.github.etheradon.graalbridge.scripting.swc4j

import io.github.etheradon.graalbridge.scripting.utils.typescript.TypeScriptHelper
import org.junit.jupiter.api.Test
import strikt.api.expectDoesNotThrow
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo

fun runTranspileTest(inputCode: String, expectedCode: String) {
    val transpiled = TypeScriptHelper.transpileCode(inputCode, "test.ts")
    expectThat(transpiled.trimEnd()).isEqualTo(expectedCode)
}

class ImportTransformerTest {

    @Test
    fun `Convert default import`() {
        val code = """
            import System from "java.lang";
            console.log(System);
        """.trimIndent()
        val expected = """
            const System = Java.type("java.lang.System");
            console.log(System);
        """.trimIndent()
        runTranspileTest(code, expected)
    }

    @Test
    fun `Convert multiple default imports`() {
        val code = """
                import { System, Exception, Math } from "java.lang";
                console.log(System, Exception, Math);
            """.trimIndent()
        val expected = """
                const System = Java.type("java.lang.System");
                const Exception = Java.type("java.lang.Exception");
                const Math = Java.type("java.lang.Math");
                console.log(System, Exception, Math);
            """.trimIndent()
        runTranspileTest(code, expected)
    }

    @Test
    fun `Convert named import`() {
        val code = """
                import {System as Sys} from "java.lang";
                console.log(Sys);
            """.trimIndent()
        val expected = """
                const Sys = Java.type("java.lang.System");
                console.log(Sys);
            """.trimIndent()
        runTranspileTest(code, expected)
    }

    @Test
    fun `Convert multiple named imports`() {
        val code = """
                import { System as Sys, Exception as Ex, Math as M } from "java.lang";
                console.log(Sys, Ex, M);
            """.trimIndent()
        val expected = """
                const Sys = Java.type("java.lang.System");
                const Ex = Java.type("java.lang.Exception");
                const M = Java.type("java.lang.Math");
                console.log(Sys, Ex, M);
            """.trimIndent()
        runTranspileTest(code, expected)
    }

    @Test
    fun `Namespace Java import throws exception`() {
        val code = """
                import * as A from "java.lang";
                console.log(A);
            """.trimIndent()

        expectThrows<Exception> { TypeScriptHelper.transpileCode(code, "test.ts") }
    }

    @Test
    fun `Namespace JavaScript import does not throw exception`() {
        val code = """
                import * as A from "color";
                console.log(A);
            """.trimIndent()

        expectDoesNotThrow { TypeScriptHelper.transpileCode(code, "test.ts") }
    }

    @Test
    fun `Convert value import`() {
        val code = """
                import out from "java.lang.System";
                console.log(out);
            """.trimIndent()
        val expected = """
                const out = Java.type("java.lang.System").out;
                console.log(out);
            """.trimIndent()
        runTranspileTest(code, expected)
    }

    @Test
    fun `Convert multiple value imports`() {
        val code = """
                import { out, err, gc } from "java.lang.System";
                console.log(out, err, gc);
            """.trimIndent()
        val expected = """
                const out = Java.type("java.lang.System").out;
                const err = Java.type("java.lang.System").err;
                const gc = Java.type("java.lang.System").gc;
                console.log(out, err, gc);
            """.trimIndent()
        runTranspileTest(code, expected)
    }

    @Test
    fun `Convert named value import`() {
        val code = """
                import { out as o } from "java.lang.System";
                console.log(o);
            """.trimIndent()
        val expected = """
                const o = Java.type("java.lang.System").out;
                console.log(o);
            """.trimIndent()
        runTranspileTest(code, expected)
    }

    @Test
    fun `Convert multiple named value imports`() {
        val code = """
                import { out as o, err as e, gc as g } from "java.lang.System";
                console.log(o, e, g);
            """.trimIndent()
        val expected = """
                const o = Java.type("java.lang.System").out;
                const e = Java.type("java.lang.System").err;
                const g = Java.type("java.lang.System").gc;
                console.log(o, e, g);
            """.trimIndent()
        runTranspileTest(code, expected)
    }

    @Test
    fun `Does not convert JavaScript imports`() {
        val code = """
            import { sum, subtract } from "./math";
            import test from "test";
            console.log(sum, subtract, test);
        """.trimIndent()
        runTranspileTest(code, code)
    }

    @Test
    fun `Does not convert TypeScript imports`() {
        val code = """
            import test = require("test");
            console.log(test);
        """.trimIndent()
        runTranspileTest(code, code)
    }

}
