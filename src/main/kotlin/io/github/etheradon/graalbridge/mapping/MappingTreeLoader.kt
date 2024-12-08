package io.github.etheradon.graalbridge.mapping

import net.fabricmc.mappingio.MappingReader
import net.fabricmc.mappingio.adapter.MappingNsRenamer
import net.fabricmc.mappingio.adapter.MappingSourceNsSwitch
import net.fabricmc.mappingio.tree.MemoryMappingTree

private class CommonNamespaceIterator(
    private val mappingPath: List<Mapping>,
    private val targetEntry: Namespace
) : Iterator<Namespace> {
    private var index = 0
    private var mergedNamespaces = mappingPath.firstOrNull()?.containedTypes?.toMutableSet() ?: mutableSetOf()

    override fun hasNext(): Boolean = index < mappingPath.size

    override fun next(): Namespace {
        if (index == mappingPath.lastIndex) {
            index++
            return targetEntry
        }
        val nextMapping = mappingPath[++index]
        val commonNamespace = mergedNamespaces.intersect(nextMapping.containedTypes).firstOrNull()
            ?: throw NoSuchElementException("No common namespace found")
        mergedNamespaces += nextMapping.containedTypes
        return commonNamespace
    }
}

class MappingTreeLoader(private val mappingLoader: MappingLoader) {

    fun loadMappingTree(
        mapping: MappingResolver,
        sourceEntry: Namespace,
        targetEntry: Namespace
    ): MappingResult {
        val mappingPath = mapping.findShortestPath(sourceEntry, targetEntry)
        println("Mapping path: ${mappingPath?.joinToString(" -> ") { it.name }}")
        return when {
            mappingPath == null -> MappingResult.Failure("No path found between $sourceEntry and $targetEntry")
            mappingPath.isEmpty() -> MappingResult.SAME
            mappingPath.size == 1 -> loadSingleMapping(mappingPath.first(), targetEntry)
            else -> loadMultipleMappings(mappingPath, targetEntry)
        }
    }

    private fun loadSingleMapping(mapping: Mapping, targetNamespace: Namespace): MappingResult {
        val tree = MemoryMappingTree()
        try {
            mappingLoader.load(mapping).use { reader ->
                val visitor = if (mapping.source == targetNamespace) tree
                else MappingSourceNsSwitch(tree, targetNamespace.name)

                val replacements = mapping.containedTypes.flatMap { ns -> ns.otherNames.map { it to ns.name } }.toMap()
                val renamingVisitor = MappingNsRenamer(visitor, replacements)

                MappingReader.read(reader, renamingVisitor)
            }

            return MappingResult.Success(tree)
        } catch (e: Exception) {
            return MappingResult.Failure("Error reading mapping file: ${e.message}")
        }
    }

    private fun loadMultipleMappings(mappingPath: List<Mapping>, targetEntry: Namespace): MappingResult {
        val commonNamespaceIterator = CommonNamespaceIterator(mappingPath, targetEntry)
        var lastTree = MemoryMappingTree()
        var commonNamespace = commonNamespaceIterator.next()

        try {
            mappingPath.forEachIndexed { index, mapping ->
                val result = loadSingleMapping(mapping, commonNamespace)
                val currentTree = when (result) {
                    is MappingResult.Success -> result.tree
                    else -> throw IllegalStateException("Failed to load mapping")
                }

                if (index == 0) {
                    lastTree = currentTree
                } else {
                    currentTree.accept(lastTree)
                    commonNamespace = commonNamespaceIterator.next()
                    // Switches source namespace for the next merge if needed
                    if (commonNamespace.name != lastTree.srcNamespace) {
                        lastTree = MemoryMappingTree().apply {
                            lastTree.accept(MappingSourceNsSwitch(this, commonNamespace.name))
                        }
                    }
                }
            }
            return MappingResult.Success(lastTree)
        } catch (e: Exception) {
            return MappingResult.Failure("Error processing mapping path: ${e.message}")
        }
    }

    sealed class MappingResult {
        data class Success(val tree: MemoryMappingTree) : MappingResult()
        object SAME : MappingResult()
        data class Failure(val error: String) : MappingResult()
    }

}
