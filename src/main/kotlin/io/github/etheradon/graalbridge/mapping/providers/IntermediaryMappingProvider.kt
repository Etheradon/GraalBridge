package io.github.etheradon.graalbridge.mapping.providers

import com.google.gson.reflect.TypeToken
import io.github.etheradon.graalbridge.mapping.Namespace

object IntermediaryMappingProvider :
    FabricMappingProvider<IntermediaryMappingProvider.IntermediaryVersion>(Namespace.INTERMEDIARY) {

    override fun getMetadataDownloadUrl(): String {
        return "https://meta.fabricmc.net/v2/versions/intermediary"
    }

    override fun getMappingDownloadURL(version: String): String {
        return "https://maven.fabricmc.net/net/fabricmc/intermediary/$version/intermediary-$version-v2.jar"
    }

    override suspend fun getLatestVersion(mcVersion: String): String? {
        return versionsInternal.await().firstOrNull { it.version == mcVersion }?.version
    }

    override suspend fun getVersions(): List<String> {
        return versionsInternal.await().map { it.version }
    }

    override fun parseJson(data: String): List<IntermediaryVersion> {
        val token = object : TypeToken<List<IntermediaryVersion>>() {}.type
        return gson.fromJson(data, token)
    }

    data class IntermediaryVersion(val version: String)

}
