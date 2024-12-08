package io.github.etheradon.graalbridge.scripting.remapping.testclasses

@Suppress("unused")
interface NormalInterface {
    fun defaultMethod(): String {
        return "Default Method"
    }
}

@Suppress("unused")
interface ObfuscatedInterface : NormalInterface {
    fun obfuscatedInterfaceMethod(): String
}

@Suppress("unused")
open class BaseObfuscatedClass : ObfuscatedInterface {
    @JvmField
    val obfuscatedBaseField = "Base Field"
    fun obfuscatedBaseMethod(): String = "Base Method"

    fun normalMethod(): String = "Normal Method"

    override fun obfuscatedInterfaceMethod(): String = "Interface Method"
}

@Suppress("unused")
open class DerivedObfuscatedClass : BaseObfuscatedClass() {
}
