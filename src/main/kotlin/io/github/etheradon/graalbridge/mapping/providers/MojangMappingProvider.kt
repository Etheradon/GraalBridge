package io.github.etheradon.graalbridge.mapping.providers

import com.google.gson.JsonObject
import io.github.etheradon.graalbridge.mapping.Namespace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object MojangMappingProvider : MappingProvider<MojangMappingProvider.Version>(Namespace.MOJMAP) {

    override suspend fun fetchVersions(): List<Version> {
        val manifestUrl = "https://launchermeta.mojang.com/mc/game/version_manifest.json"
        val data: ByteArray = try {
            downloadFileFromUrlToMemory(manifestUrl)
        } catch (e: Exception) {
            println("Error fetching version manifest: ${e.localizedMessage}")
            e.printStackTrace()
            return emptyList()
        }
        return gson.fromJson(String(data), VersionData::class.java).versions
    }

    override suspend fun getLatestVersion(mcVersion: String): String? {
        return versionsInternal.await().firstOrNull { it.id == mcVersion }?.id
    }

    override suspend fun getVersions(): List<String> = versionsInternal.await().map { it.id }

    override suspend fun getMappingsInternal(version: String): String? {
        return withContext(Dispatchers.IO) {
            val versionData = versionsInternal.await().find { it.id == version }
                ?: throw IllegalArgumentException("Version $version not found")
            val metadata: ByteArray = try {
                downloadFileFromUrlToMemory(versionData.url)
            } catch (e: Exception) {
                println("Error fetching version metadata for version $version: ${e.localizedMessage}")
                e.printStackTrace()
                return@withContext null
            }
            var mappingUrl = gson.fromJson(String(metadata), JsonObject::class.java).getAsJsonObject("downloads")
                .getAsJsonObject("client_mappings").getAsJsonPrimitive("url").asString

            val mappingData: ByteArray = try {
                downloadFileFromUrlToMemory(mappingUrl)
            } catch (e: Exception) {
                println("Error fetching mappings for version $version: ${e.localizedMessage}")
                e.printStackTrace()
                return@withContext null
            }
            String(mappingData)
        }
    }

    data class Latest(val release: String, val snapshot: String)

    data class Version(val id: String, val url: String)

    data class VersionData(val latest: Latest, val versions: List<Version>)

}
