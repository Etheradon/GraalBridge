package io.github.etheradon.graalbridge.scripting.utils.mapping

interface Mappings {

    fun unmapClass(className: String): String?
    fun remapClass(className: String): String?
    fun remapField(clazz: Class<*>, fieldName: String, descriptor: String? = null): String?
    fun remapMethod(clazz: Class<*>, methodName: String, descriptor: String? = null): String?

}
