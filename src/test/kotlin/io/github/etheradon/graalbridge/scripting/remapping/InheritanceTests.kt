package io.github.etheradon.graalbridge.scripting.remapping

import io.github.etheradon.graalbridge.scripting.remapping.testclasses.DerivedObfuscatedClass
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import strikt.api.expectThat
import strikt.assertions.isEqualTo

object InheritanceTests : MappingTest() {

    val instance = DerivedObfuscatedClass()

    data class TestCase(
        val name: String,
        val expected: String
    ) : Arguments {
        override fun get(): Array<out Any?>? = arrayOf(name, expected)
    }

    @JvmStatic
    fun fieldCases() = listOf(
        TestCase("baseField", instance.obfuscatedBaseField),
    )

    @ParameterizedTest(name = "Inheritance field {0}")
    @MethodSource("fieldCases")
    fun `Inheritance fields -`(fieldName: String, expected: String) {
        val code = """
            const instance = new (Java.type("$DEFAULT_PACKAGE.DerivedObfuscatedClass"))()
            instance.$fieldName
        """.trimIndent()
        expectThat(evalJsCode(code).asString()).isEqualTo(expected)
    }

    @JvmStatic
    fun methodCases() = listOf(
        TestCase("defaultMethod", instance.defaultMethod()),
        TestCase("defaultMethod", instance.defaultMethod()),
        TestCase("interfaceMethod", instance.obfuscatedInterfaceMethod()),
        TestCase("baseMethod", instance.obfuscatedBaseMethod()),
    )

    @ParameterizedTest(name = "Inheritance method {0}")
    @MethodSource("methodCases")
    fun `Inheritance methods -`(methodName: String, expected: String) {
        val code = """
            const instance = new (Java.type("$DEFAULT_PACKAGE.DerivedObfuscatedClass"))()
            instance.$methodName()
        """.trimIndent()
        expectThat(evalJsCode(code).asString()).isEqualTo(expected)
    }

}
