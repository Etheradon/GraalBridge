package io.github.etheradon.graalbridge.scripting.remapping.testclasses

import java.io.File

@Suppress("unused")
enum class EnumWithObfuscatedValues {
    A,
    B,
    C;
}

@Suppress("unused")
class OuterObfuscated {

    @JvmField
    val obfuscatedField = "Obfuscated field"
    fun obfuscatedMethod(): String = "Obfuscated method"

    companion object {
        @JvmField
        val staticObfuscatedField = "Static obfuscated field"

        @JvmStatic
        fun staticObfuscatedMethod(): String = "Static obfuscated method"

        @JvmStatic
        fun staticPrimitiveSignatureMethod(arg1: Int, arg2: Long): String = "Static primitive signature method"

        @JvmStatic
        fun staticSignatureMethod(arg1: File, arg2: List<File>): String = "Static signature method"

    }

    class StaticInnerObfuscated {
        class StaticFurtherInnerObfuscated {
            class StaticEvenFurtherInnerObfuscated {
                companion object {
                    @JvmField
                    val deeplyNestedStaticObfuscatedField = "Deeply nested static obfuscated field"
                }
            }
        }
    }

    inner class InnerObfuscated {
    }

}
