package io.github.etheradon.graalbridge.mapping.providers

import io.github.etheradon.graalbridge.mapping.Namespace


abstract class FabricMappingProvider<V : Any>(namespace: Namespace) : MappingProvider<V>(namespace) {

    abstract fun getMetadataDownloadUrl(): String

    override suspend fun fetchVersions(): List<V> {
        val manifestUrl = getMetadataDownloadUrl()
        val data: ByteArray = try {
            downloadFileFromUrlToMemory(manifestUrl)
        } catch (e: Exception) {
            println("Error fetching version manifest: ${e.localizedMessage}")
            e.printStackTrace()
            return emptyList()
        }
        return parseJson(String(data))
    }

    abstract fun parseJson(data: String): List<V>

    abstract fun getMappingDownloadURL(version: String): String

    override suspend fun getMappingsInternal(version: String): String? {
        return downloadAndExtract(getMappingDownloadURL(version), "mappings/mappings.tiny")
    }

}
