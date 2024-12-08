package io.github.etheradon.graalbridge.scripting.remapping

import io.github.etheradon.graalbridge.scripting.remapping.testclasses.OuterObfuscated
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import strikt.api.expectDoesNotThrow
import strikt.api.expectThat
import strikt.assertions.isEqualTo

object PackageAndImportTests : MappingTest() {

    data class TestCase(
        val packageName: String,
        val className: String,
        val expectedClass: Class<*>,
        val description: String,
        val name: String
    ) : Arguments {
        override fun get(): Array<out Any?>? = arrayOf(packageName, className, expectedClass, description, name)
    }

    @JvmStatic
    fun testCases() = listOf<Arguments>(
        TestCase(
            "testclasses",
            "Outer",
            OuterObfuscated::class.java,
            "Should map 'testclasses.Outer' to '$DEFAULT_PACKAGE.OuterObfuscated'",
            "obfuscated package path"
        ),
        TestCase(
            DEFAULT_PACKAGE,
            "Outer",
            OuterObfuscated::class.java,
            "Should map 'Outer' to 'OuterObfuscated'",
            "obfuscated class name"
        ),

        TestCase(
            DEFAULT_PACKAGE,
            "OuterObfuscated.StaticInnerObfuscated",
            OuterObfuscated.StaticInnerObfuscated::class.java,
            "Should find the original class",
            "obfuscated.obfuscated"
        ),
        TestCase(
            DEFAULT_PACKAGE,
            "OuterObfuscated\$StaticInnerObfuscated",
            OuterObfuscated.StaticInnerObfuscated::class.java,
            "Should find the original class",
            "obfuscated\$obfuscated"
        ),
        TestCase(
            DEFAULT_PACKAGE,
            "OuterObfuscated.StaticInner",
            OuterObfuscated.StaticInnerObfuscated::class.java,
            "Should map 'StaticInner' to 'StaticInnerObfuscated'",
            "obfuscated.mapped"
        ),
        TestCase(
            DEFAULT_PACKAGE,
            "OuterObfuscated\$StaticInner",
            OuterObfuscated.StaticInnerObfuscated::class.java,
            "Should map 'StaticInner' to 'StaticInnerObfuscated'",
            "obfuscated\$mapped"
        ),
        TestCase(
            DEFAULT_PACKAGE,
            "Outer.StaticInnerObfuscated",
            OuterObfuscated.StaticInnerObfuscated::class.java,
            "Should map 'Outer' to 'OuterObfuscated'",
            "mapped.obfuscated"
        ),
        TestCase(
            DEFAULT_PACKAGE,
            "Outer\$StaticInnerObfuscated",
            OuterObfuscated.StaticInnerObfuscated::class.java,
            "Should map 'Outer' to 'OuterObfuscated'",
            "mapped\$obfuscated"
        ),
        TestCase(
            DEFAULT_PACKAGE,
            "Outer.StaticInner",
            OuterObfuscated.StaticInnerObfuscated::class.java,
            "Should map 'Outer.Inner' to 'OuterObfuscated.StaticInnerObfuscated'",
            "mapped.mapped"
        ),
        TestCase(
            DEFAULT_PACKAGE,
            "Outer\$StaticInner",
            OuterObfuscated.StaticInnerObfuscated::class.java,
            "Should map 'Outer.Inner' to 'OuterObfuscated.StaticInnerObfuscated'",
            "mapped\$mapped"
        ),
    )

    @ParameterizedTest(name = "Java type with {4}")
    @MethodSource("testCases")
    fun `Java Type -`(
        packageName: String,
        inputCode: String,
        expectedClass: Class<*>,
        description: String,
        name: String
    ) {
        expectDoesNotThrow { evalJsCode("Java.type(\"$packageName.$inputCode\")") }.get { asHostObject<Class<*>>() }
            .describedAs(description)
            .isEqualTo(expectedClass)
    }

    @ParameterizedTest(name = "Packages with {4}")
    @MethodSource("testCases")
    fun `Packages -`(
        packageName: String,
        inputCode: String,
        expectedClass: Class<*>,
        description: String,
        name: String
    ) {
        expectDoesNotThrow { evalJsCode("Packages.$packageName.$inputCode") }.get { asHostObject<Class<*>>() }
            .describedAs(description)
            .isEqualTo(expectedClass)
    }

    @Test
    fun `Packages - Undefined for non-static inner classes`() {
        val packagesCode = "Packages.$DEFAULT_PACKAGE.OuterObfuscated.Inner"
        expectThat(evalJsCode(packagesCode).toString())
            .describedAs("Packages should return undefined for non-static inner classes")
            .isEqualTo("undefined")
    }

    @Test
    fun `Java Type - Undefined for non-static inner classes`() {
        val packagesCode = "Java.type(\"$DEFAULT_PACKAGE.OuterObfuscated\").Inner"
        expectThat(evalJsCode(packagesCode).toString())
            .describedAs("Packages should return undefined for non-static inner classes")
            .isEqualTo("undefined")
    }
    /*
        @Test
        fun `Java Type - Nested Complex 1`() {
            val nestedPackage =
                "Java.type(\"$DEFAULT_PACKAGE.OuterObfuscated\$StaticInner\$StaticFurtherInner\$StaticEvenFurtherInner\")"
            expectThat(evalJsCode(nestedPackage).asHostObject<Class<*>>())
                .isEqualTo(OuterObfuscated.StaticInnerObfuscated.StaticFurtherInnerObfuscated.StaticEvenFurtherInnerObfuscated::class.java)
        }

        @Test
        fun `Java Type - Nested Complex 2`() {
            val nestedPackage =
                "Java.type(\"$DEFAULT_PACKAGE.OuterObfuscated\$StaticInner\$StaticFurtherInnerObfuscated\$StaticEvenFurtherInner\")"
            expectThat(evalJsCode(nestedPackage).asHostObject<Class<*>>())
                .isEqualTo(OuterObfuscated.StaticInnerObfuscated.StaticFurtherInnerObfuscated.StaticEvenFurtherInnerObfuscated::class.java)
        }
    */

}
