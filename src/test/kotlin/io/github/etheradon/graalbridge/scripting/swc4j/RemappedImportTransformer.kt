package io.github.etheradon.graalbridge.scripting.swc4j

import io.github.etheradon.graalbridge.scripting.remapping.MappingTest
import org.junit.jupiter.api.Test

class RemappedImportTransformer : MappingTest() {

    @Test
    fun `Import transformer named import`() {
        val code = """
            import {Outer as O} from "$DEFAULT_PACKAGE";
            console.log(O);
        """.trimIndent()
        val expected = """
            const O = Java.type("$DEFAULT_PACKAGE.Outer");
            console.log(O);
        """.trimIndent()
        runTranspileTest(code, expected)
    }

    @Test
    fun `Import transformer named value`() {
        val code = """
                import { staticField as sF } from "$DEFAULT_PACKAGE.Outer";
                console.log(sF);
            """.trimIndent()
        val expected = """
                const sF = Java.type("$DEFAULT_PACKAGE.Outer").staticField;
                console.log(sF);
            """.trimIndent()
        runTranspileTest(code, expected)
    }

    @Test
    fun `Import transformer nested named import`() {
        val code = $$"""
            import {StaticEvenFurtherInner as SEFI} from "$$DEFAULT_PACKAGE.Outer$StaticInner$StaticFurtherInner";
            console.log(SEFI);
        """.trimIndent()
        val expected = $$"""
            const SEFI = Java.type("$$DEFAULT_PACKAGE.Outer$StaticInner$StaticFurtherInner$StaticEvenFurtherInner");
            console.log(SEFI);
        """.trimIndent()
        runTranspileTest(code, expected)
    }

    @Test
    fun `Import transformer nested named value`() {
        val code = $$"""
                import { deeplyNestedStaticObfuscatedField as dF } from "$$DEFAULT_PACKAGE.Outer$StaticInner$StaticFurtherInner$StaticEvenFurtherInner";
                console.log(dF);
            """.trimIndent()
        val expected = $$"""
                const dF = Java.type("$$DEFAULT_PACKAGE.Outer$StaticInner$StaticFurtherInner$StaticEvenFurtherInner").deeplyNestedStaticObfuscatedField;
                console.log(dF);
            """.trimIndent()
        runTranspileTest(code, expected)
    }

}
