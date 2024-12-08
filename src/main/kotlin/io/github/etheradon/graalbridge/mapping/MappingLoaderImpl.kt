package io.github.etheradon.graalbridge.mapping

import io.github.etheradon.graalbridge.mapping.Mapping.Companion.getMappingProviderFor
import kotlinx.coroutines.runBlocking
import xyz.wagyourtail.jsmacros.client.JsMacros
import java.io.File
import java.io.Reader

class MappingLoaderImpl(
    private val autoDownload: Boolean,
    private val mcVersion: String
) : MappingLoader {

    override fun load(mapping: Mapping): Reader {
        val file = findMappingFile(mapping)
        if (file != null) {
            return file.reader()
        }

        if (autoDownload) {
            downloadMapping(mapping)
            return load(mapping)
        }

        throw IllegalStateException("No mappings found for version $mcVersion")
    }

    private fun findMappingFile(mapping: Mapping): File? {
        return getMappingsFolder().listFiles()?.firstOrNull { it.nameWithoutExtension == mapping.name }
    }

    private fun downloadMapping(mapping: Mapping) {
        val mappingProvider = getMappingProviderFor(mapping)

        println("Downloading ${mapping.name} mappings for version $mcVersion")
        return runBlocking {
            val latestVersion = mappingProvider.getLatestVersion(mcVersion)
                ?: throw IllegalStateException("No mappings found for version $mcVersion")
            val reader = mappingProvider.getMappings(latestVersion)
                ?: throw IllegalStateException("Failed to download ${mapping.name} mappings for version $mcVersion")
            saveMappingToFile(mapping, reader)
        }
    }

    private fun saveMappingToFile(mapping: Mapping, reader: Reader) {
        val file = getMappingsFolder().resolve("${mapping.name}.txt")
        file.writeText(reader.readText())
    }

    private fun getMappingsFolder(): File {
        val mappingsFolder = JsMacros.core.config.configFolder.resolve("mappings/${mcVersion}")
        mappingsFolder.mkdirs()
        return mappingsFolder
    }

}
