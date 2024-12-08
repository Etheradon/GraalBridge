package io.github.etheradon.graalbridge.mapping.providers

import io.github.etheradon.graalbridge.mapping.Namespace

object QuiltMappingProvider : MappingProvider<String>(Namespace.QUILT) {

    override suspend fun fetchVersions(): List<String> {
        return downloadAndParseXmlVersions("https://maven.quiltmc.org/repository/release/org/quiltmc/quilt-mappings/maven-metadata.xml")
    }

    override suspend fun getLatestVersion(mcVersion: String): String? {
        return getVersions().lastOrNull { it.split("-")[0] == mcVersion || it.split("+")[0] == mcVersion }
    }

    override suspend fun getVersions(): List<String> {
        return versionsInternal.await()
    }

    override suspend fun getMappingsInternal(version: String): String? {
        val downloadURL =
            "https://maven.quiltmc.org/repository/release/org/quiltmc/quilt-mappings/$version/quilt-mappings-$version-v2.jar"
        val fileName = "mappings/mappings.tiny"
        return downloadAndExtract(downloadURL, fileName)
    }

}
