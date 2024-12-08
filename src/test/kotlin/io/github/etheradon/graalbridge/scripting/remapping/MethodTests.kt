package io.github.etheradon.graalbridge.scripting.remapping

import io.github.etheradon.graalbridge.scripting.remapping.testclasses.ObfuscatedMethods
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class MethodTests : MappingTest() {

    @Test
    fun `Overloaded method`() {
        val code = """
            const instance = new (Java.type("$DEFAULT_PACKAGE.ObfuscatedMethods"))()
            const intOverload = instance.overloadedMethod(1)
            const doubleOverload = instance.overloadedMethod(1.5)
            const intFloatOverload = instance.overloadedMethod(1, 1.5)
            const intDoubleOverload = instance.overloadedMethod(1, 1.50000000001)
            let results = {
                instance,
                intOverload,
                doubleOverload,
                intFloatOverload,
                intDoubleOverload,
            };
            results;
        """.trimIndent()
        val result = evalJsCode(code)
        expectThat(result).and {
            val instance = result.getMember("instance").asHostObject<ObfuscatedMethods>()
            get {
                result.getMember("intOverload").asString()
            }.isEqualTo(instance.obfuscatedOverloadedMethod(1))
            get {
                result.getMember("doubleOverload").asString()
            }.isEqualTo(instance.obfuscatedOverloadedMethod(1.5))
            get {
                result.getMember("intFloatOverload").asString()
            }.isEqualTo(instance.obfuscatedOverloadedMethod(1, 1.5f))
            get {
                result.getMember("intDoubleOverload").asString()
            }.isEqualTo(instance.obfuscatedOverloadedMethod(1, 1.5))
        }
    }

    @Test
    fun `Method signature`() {
        val code = """
            let instance = new (Java.type("$DEFAULT_PACKAGE.ObfuscatedMethods"))()
            let signature = instance["methodSignature($DEFAULT_PACKAGE.OuterObfuscated${'$'}Inner,$DEFAULT_PACKAGE.OuterObfuscated${'$'}StaticInner,java.lang.String)"](null, null, null)
            let results = {
                instance,
                signature,
            };
            results;
        """.trimIndent()
        val result = evalJsCode(code)
        expectThat(result).and {
            val instance = result.getMember("instance").asHostObject<ObfuscatedMethods>()
            get {
                result.getMember("signature").asString()
            }.isEqualTo(instance.obfuscatedMethodSignature(null, null, null))
        }
    }

    /*    @Test
        fun `Conflicting method`() {
            val code = """
                const instance = new (Java.type("$DEFAULT_PACKAGE.ObfuscatedMethods"))()
                const conflict = instance.conflict("arg")
                const obfuscatedNoConflict = instance.conflict(1)
                let results = {
                    instance,
                    conflict,
                    obfuscatedNoConflict,
                };
                results;
            """.trimIndent()
            val result = evalJsCode(code)
            expectThat(result).and {
                val instance = result.getMember("instance").asHostObject<ObfuscatedMethods>()
                get {
                    result.getMember("conflict").asString()
                }.isEqualTo(instance.conflict("arg"))
                get {
                    result.getMember("obfuscatedNoConflict").asString()
                }.isEqualTo(instance.obfuscatedConflict(1))
            }
        }*/

}
