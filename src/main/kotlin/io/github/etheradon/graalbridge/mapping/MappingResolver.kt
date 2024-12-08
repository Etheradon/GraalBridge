package io.github.etheradon.graalbridge.mapping

class MappingResolver private constructor(private val mappings: List<Mapping>) {

    fun findShortestPath(source: Namespace, target: Namespace): List<Mapping>? {
        if (source == target) {
            return emptyList()
        }

        val visitedMappings = mutableSetOf<Mapping>()
        val visitedNamespaces = mutableSetOf<Namespace>()
        val queue = ArrayDeque<Pair<Mapping, List<Mapping>>>()

        visitedNamespaces.add(source)
        mappings.filter { it.containedTypes.contains(source) }.forEach { mapping ->
            visitedMappings.add(mapping)
            queue.add(mapping to listOf(mapping))
        }

        while (queue.isNotEmpty()) {
            val (currentMapping, currentPath) = queue.removeFirst()

            if (currentMapping.containedTypes.contains(target)) {
                return currentPath
            }

            currentMapping.containedTypes
                .filterNot { it in visitedNamespaces }
                .forEach { mappingType ->
                    visitedNamespaces.add(mappingType)

                    mappings.filter { it.containedTypes.contains(mappingType) }
                        .filterNot { it in visitedMappings }
                        .forEach { nextMapping ->
                            visitedMappings.add(nextMapping)
                            queue.add(nextMapping to currentPath + nextMapping)
                        }
                }
        }

        return null
    }

    companion object {
        fun default(): MappingResolver {
            return build { default() }
        }

        fun build(action: Builder.() -> Unit): MappingResolver {
            return Builder().apply(action).build()
        }
    }

    class Builder {
        private val entries = mutableListOf<Mapping>()

        fun default() {
            add(Mapping.MOJMAP)
            add(Mapping.SRG)
            add(Mapping.INTERMEDIARY)
            add(Mapping.YARN)
            add(Mapping.HASHED)
            add(Mapping.QUILT)
        }

        fun add(mapping: Mapping) {
            when {
                mapping == Mapping.NONE -> {
                    println("Cannot add NONE mapping")
                    return
                }

                entries.any { it::class == mapping::class } -> {
                    if (mapping !is Mapping.Custom) {
                        println("Duplicate mapping: $mapping")
                        return
                    }
                }

                mapping is Mapping.Custom && entries.any { it.name == mapping.name } -> {
                    println("Duplicate custom mapping name: ${mapping.name}")
                    return
                }
            }
            entries.add(mapping)
        }

        fun build(): MappingResolver {
            return MappingResolver(entries)
        }
    }

}
