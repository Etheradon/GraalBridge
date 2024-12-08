package io.github.etheradon.graalbridge.scripting.utils.mapping

import java.util.concurrent.ConcurrentHashMap

object MappingManager {

    // References
    // getName(): java.lang.String - getCanonicalName(): java.lang.String
    // getName(): [Ljava.lang.String; - getCanonicalName(): java.lang.String[]
    // getName(): com.example.TestClass$InnerClass - getCanonicalName(): com.example.TestClass.InnerClass

    private var mappings: Mappings = EmptyMappings()
    private val classCache: MutableMap<String, String> = ConcurrentHashMap()
    private val unmappedClassCache: MutableSet<String> = ConcurrentHashMap.newKeySet()

    fun setMappingTree(mappings: Mappings) {
        clearCache()
        this.mappings = mappings
    }

    fun getMappings(): Mappings {
        return mappings
    }

    private fun clearCache() {
        classCache.clear()
        unmappedClassCache.clear()
    }

    /**
     * Is called when Graal JS tries to find a static inner class or interface inside on a class.
     * E.g. 'Java.type("test.Test").InnerTest' or 'Packages.test.Test.InnerTest'
     * Does not work with 'java.lang.Class.forName("test.Test").InnerTest'
     * or 'Java.type("test.Test.InnerTest")'.
     */
    fun hasInnerClass(clazz: Class<*>, unmappedInnerClassName: String, mappedInnerClassName: String): Boolean {
        mappings.unmapClass(clazz.canonicalName)?.let {
            val mappedName = "$it$$mappedInnerClassName"
            remapClass(mappedName)?.let {
                if (it.substringAfterLast('$') == unmappedInnerClassName) {
                    return true
                }
            }
        }
        return false
    }

    private fun lastSeparatorIndex(className: String): Int {
        for (i in className.length - 1 downTo 0) {
            if (className[i] == '.' || className[i] == '$') {
                return i
            }
        }
        return -1
    }

    /**
     * 1. Check if the class is already cached.
     * 2. Check if the class is contained in the mappings.
     * 3. Check if it's an inner class and try to remap the outer class.
     */
    fun remapClass(className: String): String? {
        // Should not be called for obfuscated$obfuscated since they can be loaded directly
        classCache[className]?.let { return it }
        if (unmappedClassCache.contains(className)) {
            return null
        }

        // Finds mapped and mapped$mapped
        mappings.remapClass(className)?.let {
            classCache[className] = it
            return it
        }

        val lastSeparator = lastSeparatorIndex(className)
        if (lastSeparator == -1) {
            unmappedClassCache.add(className)
            return null
        }

        val separatorSymbol = className[lastSeparator]
        val outerName = className.substring(0, lastSeparator)
        val innerName = className.substring(lastSeparator + 1)
        // Finds obfuscated$mapped
        mappings.unmapClass(outerName)?.let {
            if (it == outerName) {
                return@let
            }
            remapClass("$it$$innerName")?.let {
                classCache[className] = it
                return it
            }
        }

        // Finds mapped$obfuscated
        remapClass(outerName)?.let {
            val obfuscatedName = "$it$$innerName"
            mappings.unmapClass(obfuscatedName)?.let {
                classCache[className] = obfuscatedName
                return obfuscatedName
            }

            // For deeper nested classes, the outer name can be part mapped and obfuscated.
            // Check if there are multiple nested inner classes by counting the '$' symbol.
            // TODO: Fix alternating patterns for deeper nested classes.

            /*            if (it.count { it == '$' } > 0) {
                            remapClass(obfuscatedName)?.let {
                                classCache[className] = it
                                return it

                            }
                        }*/
        }

        unmappedClassCache.add(className)
        return null
    }

    fun remapField(clazz: Class<*>, fieldName: String, descriptor: String? = null): String? {
        var current = clazz
        while (current != Object::class.java) {
            mappings.remapField(current, fieldName, descriptor)?.let {
                return it
            }
            current = current.superclass
        }
        return null
    }

    fun remapMethodNameAndSignature(clazz: Class<*>, nameAndSignature: String): String? {
        val index = nameAndSignature.indexOf('(')
        val name = nameAndSignature.substring(0, index)
        remapMethod(clazz, name)?.let {
            val signature = nameAndSignature.substring(index + 1, nameAndSignature.length - 1)
            val params = signature.split(",")
            val mappedSignature = params.map { remapClass(it) ?: it }
            return "$it(${mappedSignature.joinToString(",")})"
        }
        return null
    }

    fun remapMethod(clazz: Class<*>, methodName: String, descriptor: String? = null): String? {
        var current = clazz
        while (current != Object::class.java) {
            mappings.remapMethod(current, methodName, descriptor)?.let {
                return it
            }
            current.interfaces.forEach { interfaceClass ->
                findMethodInInterfaces(interfaceClass, methodName, descriptor)?.let {
                    return it
                }
            }
            current = current.superclass
        }
        return null
    }

    private fun findMethodInInterfaces(interfaceClass: Class<*>, methodName: String, descriptor: String?): String? {
        mappings.remapMethod(interfaceClass, methodName, descriptor)?.let {
            return it
        }
        interfaceClass.interfaces.forEach { parentInterface ->
            findMethodInInterfaces(parentInterface, methodName, descriptor)?.let {
                return it
            }
        }
        return null
    }

    private class EmptyMappings : Mappings {
        override fun unmapClass(className: String): String? = null

        override fun remapClass(className: String): String? = null

        override fun remapField(clazz: Class<*>, fieldName: String, descriptor: String?): String? = null

        override fun remapMethod(clazz: Class<*>, methodName: String, descriptor: String?): String? = null
    }

}
