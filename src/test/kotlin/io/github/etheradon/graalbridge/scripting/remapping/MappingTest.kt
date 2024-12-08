package io.github.etheradon.graalbridge.scripting.remapping

import io.github.etheradon.graalbridge.scripting.CommonTest
import io.github.etheradon.graalbridge.scripting.utils.mapping.MappingManager
import io.github.etheradon.graalbridge.scripting.utils.mapping.Mappings
import net.fabricmc.mappingio.MappingReader
import net.fabricmc.mappingio.adapter.MappingSourceNsSwitch
import net.fabricmc.mappingio.tree.MemoryMappingTree
import org.junit.jupiter.api.BeforeAll
import kotlin.io.path.Path
import kotlin.properties.Delegates

open class MappingTest : CommonTest() {

    companion object {
        const val DEFAULT_PACKAGE = "io.github.etheradon.graalbridge.scripting.remapping.testclasses"

        @BeforeAll
        @JvmStatic
        fun loadMappings() {
            val mappings: Mappings = TestMappings("src/test/resources/mappings/remapping.tiny")
            MappingManager.setMappingTree(mappings)
        }
    }

}

class TestMappings(path: String) : Mappings {

    private val tree = MemoryMappingTree()
    private var dstNs by Delegates.notNull<Int>()

    init {
        val mappingPath = Path(path)
        MappingReader.read(mappingPath, MappingSourceNsSwitch(tree, "named"))
        dstNs = tree.getNamespaceId("intermediary")
    }

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
