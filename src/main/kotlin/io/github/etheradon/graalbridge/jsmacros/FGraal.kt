package io.github.etheradon.graalbridge.jsmacros

import com.google.gson.Gson
import com.google.gson.JsonObject
import io.github.etheradon.graalbridge.GraalBridge
import io.github.etheradon.graalbridge.mapping.*
import io.github.etheradon.graalbridge.scripting.utils.GraalUtilities
import io.github.etheradon.graalbridge.scripting.utils.mapping.MappingManager
import io.github.etheradon.graalbridge.scripting.utils.mapping.Mappings
import net.minecraft.client.main.Main
import xyz.wagyourtail.jsmacros.client.JsMacros
import xyz.wagyourtail.jsmacros.client.api.library.impl.FChat
import xyz.wagyourtail.jsmacros.core.library.BaseLibrary
import xyz.wagyourtail.jsmacros.core.library.Library

@Library("GraalBridge")
@SuppressWarnings("unused")
class FGraal : BaseLibrary() {

    companion object {
        private var mcVersion: String? = null
        private val CHAT = FChat()

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

    /**
     * Sets the Minecraft version to use for loading mappings.
     * Will most likely be automatically set when the mod is loaded.
     */
    fun setMcVersion(version: String) {
        FGraal.setMcVersion(version)
    }

    /**
     * Sets whether multithreading is allowed for the GraalJS engine.
     * This doesn't seem to have a noticeable effect on performance.
     */
    fun setMultithreading(allow: Boolean) {
        GraalUtilities.isMultiThreadedAccessAllowed = allow
    }

    /**
     * Adds a package prefix (like net or com) to the list of packages that enhanced imports can be used on.
     */
    fun addImportPackagePrefix(pkg: String) {
        GraalUtilities.importPackagePrefixes.add(pkg)
    }

    /**
     * Removes a package prefix (like net or com) from the list of packages that enhanced imports can be used on.
     */
    fun removeImportPackagePrefix(pkg: String) {
        GraalUtilities.importPackagePrefixes.remove(pkg)
    }

    /**
     * Returns a namespace object for the given name.
     * Will return a predefined instance for common mappings, otherwise a custom namespace.
     */
    fun namespace(name: String): Namespace {
        return Namespace.fromName(name)
    }

    /**
     * Returns a list of mappings that can be used to get from one namespace to another.
     */
    fun getMappingPath(source: Namespace, target: Namespace): List<Mapping> {
        val resolver = MappingResolver.default()
        return resolver.findShortestPath(source, target) ?: emptyList()
    }

    /**
     * Returns the current mappings that are loaded.
     */
    fun getLoadedMappings(): Mappings = MappingManager.getMappings()

    /**
     * Detects the current namespace based on the mod loader or returns a fallback.
     */
    @JvmOverloads
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

    /**
     * Returns a list of versions that are available for the given mapping.
     */
    suspend fun getVersions(mapping: Mapping): List<String> {
        return Mapping.getMappingProviderFor(mapping).getVersions()
    }

    /**
     * Downloads the specific version of the given mapping and returns the content as a string.
     * Returns a cached version if it was already downloaded.
     */
    suspend fun getMappings(mapping: Mapping, version: String): String? {
        return Mapping.getMappingProviderFor(mapping).getMappings(version)?.readText()
    }

    /**
     * A shortcut for loading mappings from the specified namespace (like fabric, mojmap)
     * to the active namespace (like intermediary, srg).
     */
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
            handleMappingResult(result, source, target)

        } ?: throw IllegalStateException("Minecraft version not set")
    }

    private fun handleMappingResult(result: MappingTreeLoader.MappingResult, source: Namespace, target: Namespace) {
        when (result) {
            is MappingTreeLoader.MappingResult.Success -> {
                CHAT.log("Successfully loaded mappings from ${source.name} to ${target.name}")
                MappingManager.setMappingTree(MinecraftMappings(result.tree, source.name))
            }

            is MappingTreeLoader.MappingResult.SAME -> {
                CHAT.log("Source and target are already the same")
            }

            is MappingTreeLoader.MappingResult.Failure -> {
                CHAT.log("Mapping failed: ${result.error}")
            }
        }
    }

}
