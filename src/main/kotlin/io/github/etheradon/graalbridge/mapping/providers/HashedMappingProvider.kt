package io.github.etheradon.graalbridge.mapping.providers

import io.github.etheradon.graalbridge.mapping.Namespace

object HashedMappingProvider : MappingProvider<String>(Namespace.HASHED) {

    override suspend fun fetchVersions(): List<String> {
        return downloadAndParseXmlVersions("https://maven.quiltmc.org/repository/release/org/quiltmc/hashed/maven-metadata.xml")
    }

    override suspend fun getLatestVersion(mcVersion: String): String? {
        return getVersions().lastOrNull { it == mcVersion }
    }

    override suspend fun getVersions(): List<String> {
        return versionsInternal.await()
    }

    override suspend fun getMappingsInternal(version: String): String? {
        val downloadURL =
            "https://maven.quiltmc.org/repository/release/org/quiltmc/hashed/$version/hashed-$version.jar"
        val fileName = "mappings/mappings.tiny"
        return downloadAndExtract(downloadURL, fileName)
    }
}
