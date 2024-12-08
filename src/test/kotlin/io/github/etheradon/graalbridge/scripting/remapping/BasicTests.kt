package io.github.etheradon.graalbridge.scripting.remapping

import io.github.etheradon.graalbridge.scripting.remapping.testclasses.EnumWithObfuscatedValues
import io.github.etheradon.graalbridge.scripting.remapping.testclasses.OuterObfuscated
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class BasicTests : MappingTest() {

    @Test
    fun `Enum with obfuscated values`() {
        val code = "Java.type(\"$DEFAULT_PACKAGE.EnumWithObfuscatedValues\").FIRST"
        expectThat(evalJsCode(code).asHostObject<EnumWithObfuscatedValues>())
            .describedAs("Should map 'FIRST' to 'A'")
            .isEqualTo(EnumWithObfuscatedValues.A)
    }

    @Test
    fun `Obfuscated field`() {
        val code = "new (Java.type(\"$DEFAULT_PACKAGE.OuterObfuscated\"))().field"
        expectThat(evalJsCode(code).asString())
            .describedAs("Should map 'field' to 'obfuscatedField'")
            .isEqualTo(OuterObfuscated().obfuscatedField)
    }

    @Test
    fun `Obfuscated method`() {
        val code = "new (Java.type(\"$DEFAULT_PACKAGE.OuterObfuscated\"))().method()"
        expectThat(evalJsCode(code).asString())
            .describedAs("Should map 'method' to 'obfuscatedMethod'")
            .isEqualTo(OuterObfuscated().obfuscatedMethod())
    }

    @Test
    fun `Static obfuscated field`() {
        val code = "Java.type(\"$DEFAULT_PACKAGE.OuterObfuscated\").staticField"
        expectThat(evalJsCode(code).asString())
            .describedAs("Should map 'staticField' to 'staticObfuscatedField'")
            .isEqualTo(OuterObfuscated.staticObfuscatedField)
    }

    @Test
    fun `Static obfuscated method`() {
        val code = "Java.type(\"$DEFAULT_PACKAGE.OuterObfuscated\").staticMethod()"
        expectThat(evalJsCode(code).asString())
            .describedAs("Should map 'staticMethod' to 'staticObfuscatedMethod'")
            .isEqualTo(OuterObfuscated.staticObfuscatedMethod())
    }

}
