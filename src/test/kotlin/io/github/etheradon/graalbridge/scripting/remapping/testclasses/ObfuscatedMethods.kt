package io.github.etheradon.graalbridge.scripting.remapping.testclasses

@Suppress("unused")
class ObfuscatedMethods {

    fun obfuscatedOverloadedMethod(value: Int): String = "Int overload"
    fun obfuscatedOverloadedMethod(value: Double): String = "Double overload"
    fun obfuscatedOverloadedMethod(value1: Int, value2: Float): String = "Int, Float overload"
    fun obfuscatedOverloadedMethod(value1: Int, value2: Double): String = "Int, Double overload"

    fun obfuscatedMethodSignature(
        innerObfuscatedArg: OuterObfuscated.InnerObfuscated?,
        enumArg: OuterObfuscated.StaticInnerObfuscated?,
        normalArg: String?
    ): String = "Signature"

    fun conflict(arg: String): String = "No conflict with argument"

    fun obfuscatedConflict(differentArgType: Any): String =
        "No obfuscated conflict with argument"

    fun obfuscatedConflict(arg: String): String =
        "Obfuscated conflict with argument"

}
