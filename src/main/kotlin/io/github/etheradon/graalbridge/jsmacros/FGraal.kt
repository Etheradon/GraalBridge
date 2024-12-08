package io.github.etheradon.graalbridge.jsmacros

import com.google.gson.Gson
import com.google.gson.JsonObject
import io.github.etheradon.graalbridge.GraalBridge
import io.github.etheradon.graalbridge.mapping.*
import io.github.etheradon.graalbridge.mapping.providers.*
import io.github.etheradon.graalbridge.scripting.utils.GraalUtilities
import io.github.etheradon.graalbridge.scripting.utils.mapping.MappingManager
import io.github.etheradon.graalbridge.scripting.utils.mapping.Mappings
import kotlinx.coroutines.runBlocking
import net.minecraft.client.main.Main
import xyz.wagyourtail.jsmacros.client.JsMacros
import xyz.wagyourtail.jsmacros.core.library.BaseLibrary
import xyz.wagyourtail.jsmacros.core.library.Library
import java.io.File
import java.io.Reader

@Library("GraalBridge")
@SuppressWarnings("unused")
class FGraal : BaseLibrary() {

    companion object {
        private var mcVersion: String? = null

        init {
            val versionFile = Main::class.java.getResourceAsStream("/version.json");
            if (versionFile != null) {
                val version = Gson().fromJson(versionFile.reader(), JsonObject::class.java)["name"].asString
                setMcVersion(version)
            }
        }

        fun setMcVersion(version: String) {
            mcVersion = version
        }

    }

    fun setMcVersion(version: String) {
        FGraal.setMcVersion(version)
    }

    fun setMultithreading(allow: Boolean) {
        GraalUtilities.isMultiThreadedAccessAllowed = allow
    }

    fun addImportPackage(pkg: String) {
        GraalUtilities.importPackagesToConvert.add(pkg)
    }

    fun removeImportPackage(pkg: String) {
        GraalUtilities.importPackagesToConvert.remove(pkg)
    }

    fun namespace(name: String): Namespace {
        return Namespace.fromName(name)
    }

    fun getMappingPath(source: Namespace, target: Namespace): List<Mapping> {
        val resolver = MappingResolver.default()
        return resolver.findShortestPath(source, target) ?: emptyList()
    }

    fun getLoadedMappings(): Mappings = MappingManager.getMappings()

    fun detectNamespace(fallback: String = "mojmap"): Namespace {
        val isDev = JsMacros.getModLoader().isDevEnv
        if (isDev) {
            return Namespace.fromName(fallback)
        }
        return when (GraalBridge.modLoader) {
            GraalBridge.ModLoader.FABRIC -> Namespace.INTERMEDIARY
            GraalBridge.ModLoader.QUILT -> Namespace.INTERMEDIARY
            GraalBridge.ModLoader.FORGE -> Namespace.SRG
            GraalBridge.ModLoader.NEO_FORGE -> Namespace.SRG
        }
    }

    @JvmOverloads
    fun loadMappings(target: String, autoDownload: Boolean = true) {
        loadMappings(detectNamespace(), Namespace.fromName(target), autoDownload)
    }

    /**
     * Loads mappings from the source namespace (like intermediary) to the target namespace (like yarn)
     */
    @JvmOverloads
    fun loadMappings(source: Namespace, target: Namespace, autoDownload: Boolean = true) {
        mcVersion?.let { mcVersion ->
            val resolver = MappingResolver.default()
            val mappingLoader = MappingLoaderImpl(autoDownload, mcVersion)

            val treeLoader = MappingTreeLoader(mappingLoader)
            val result = treeLoader.loadMappingTree(resolver, source, target)
            handleMappingResult(result, source)

        } ?: throw IllegalStateException("Minecraft version not set")
    }

    /**
     * Handles the result of loading the mapping tree.
     */
    private fun handleMappingResult(result: MappingTreeLoader.MappingResult, source: Namespace) {
        when (result) {
            is MappingTreeLoader.MappingResult.Success -> {
                println("Successfully loaded mappings")
                MappingManager.setMappingTree(MinecraftMappings(result.tree, source.name))
            }

            is MappingTreeLoader.MappingResult.SAME -> {
                println("Source and target are already the same")
            }

            is MappingTreeLoader.MappingResult.Failure -> {
                println("Mapping failed: ${result.error}")
            }
        }
    }

}

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

    private fun getMappingProviderFor(mapping: Mapping): MappingProvider<*> {
        return when (mapping) {
            Mapping.INTERMEDIARY -> IntermediaryMappingProvider
            Mapping.YARN -> YarnMappingProvider
            Mapping.MOJMAP -> MojangMappingProvider
            Mapping.SRG -> SrgMappingProvider
            Mapping.HASHED -> HashedMappingProvider
            Mapping.QUILT -> QuiltMappingProvider
            else -> throw IllegalArgumentException("No download provider for mapping ${mapping.name}")
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
