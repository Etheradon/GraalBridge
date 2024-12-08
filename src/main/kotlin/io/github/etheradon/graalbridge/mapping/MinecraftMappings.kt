package io.github.etheradon.graalbridge.mapping

import io.github.etheradon.graalbridge.scripting.utils.mapping.Mappings
import net.fabricmc.mappingio.tree.MemoryMappingTree

class MinecraftMappings(private val tree: MemoryMappingTree, destinationNamespace: String) : Mappings {
    private var dstNs = tree.getNamespaceId(destinationNamespace)

    private fun escapeClassName(className: String): String {
        return className.replace('.', '/')
    }

    private fun unescapeClassName(className: String): String {
        return className.replace('/', '.')
    }

    override fun unmapClass(className: String): String? {
        val escapedName = escapeClassName(className)
        for (ns in tree.dstNamespaces) {
            tree.getClass(
                escapedName,
                tree.getNamespaceId(ns)
            )?.srcName?.let { return unescapeClassName(it) }
        }
        return null
    }

    override fun remapClass(className: String): String? {
        val escapedName = escapeClassName(className)
        return tree.getClass(escapedName)?.getDstName(dstNs)?.let { unescapeClassName(it) }
    }

    override fun remapField(clazz: Class<*>, fieldName: String, descriptor: String?): String? {
        unmapClass(clazz.name)?.let {
            val escapedName = escapeClassName(it)
            return tree.getClass(escapedName)?.getField(fieldName, descriptor)?.getName(dstNs)
        }
        return null
    }

    override fun remapMethod(clazz: Class<*>, methodName: String, descriptor: String?): String? {
        unmapClass(clazz.name)?.let {
            val escapedName = escapeClassName(it)
            return tree.getClass(escapedName)?.getMethod(methodName, descriptor)?.getName(dstNs)
        }
        return null
    }
}
