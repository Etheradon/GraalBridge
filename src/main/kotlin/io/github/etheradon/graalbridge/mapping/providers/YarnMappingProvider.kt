package io.github.etheradon.graalbridge.mapping.providers

import com.google.gson.reflect.TypeToken
import io.github.etheradon.graalbridge.mapping.Namespace

object YarnMappingProvider : FabricMappingProvider<YarnMappingProvider.YarnVersion>(Namespace.YARN) {

    suspend fun getBuilds(version: String): List<String> {
        return versionsInternal.await().filter { it.gameVersion == version }.map { "${it.separator}${it.build}" }
    }

    override suspend fun getLatestVersion(mcVersion: String): String? {
        return versionsInternal.await().firstOrNull { it.gameVersion == mcVersion }?.version
    }

    override suspend fun getVersions(): List<String> {
        return versionsInternal.await().map { it.version }
    }

    override fun getMetadataDownloadUrl(): String {
        return "https://meta.fabricmc.net/v2/versions/yarn"
    }

    override fun getMappingDownloadURL(version: String): String {
        return "https://maven.fabricmc.net/net/fabricmc/yarn/$version/yarn-$version-v2.jar"
    }

    override fun parseJson(data: String): List<YarnVersion> {
        val token = object : TypeToken<List<YarnVersion>>() {}.type
        return gson.fromJson(data, token)
    }

    data class YarnVersion(val version: String, val gameVersion: String, val separator: String, val build: Int)

}
