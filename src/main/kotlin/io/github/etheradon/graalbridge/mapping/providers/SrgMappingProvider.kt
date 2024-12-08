package io.github.etheradon.graalbridge.mapping.providers

import com.google.gson.Gson
import com.google.gson.JsonObject
import io.github.etheradon.graalbridge.mapping.Namespace
import io.github.z4kn4fein.semver.Version
import net.fabricmc.mappingio.MappingReader
import net.fabricmc.mappingio.MappingWriter
import net.fabricmc.mappingio.adapter.MappingNsRenamer
import net.fabricmc.mappingio.adapter.MissingDescFilter
import net.fabricmc.mappingio.format.MappingFormat
import net.fabricmc.mappingio.tree.MemoryMappingTree
import java.io.StringWriter

object SrgMappingProvider : MappingProvider<String>(Namespace.SRG) {

    private val VERSION_1_12_2 = Version.parse("1.12.2")

    override suspend fun fetchVersions(): List<String> {

        val oldEntries = try {
            val data: ByteArray =
                downloadFileFromUrlToMemory("https://maven.minecraftforge.net/de/oceanlabs/mcp/versions.json")

            Gson().fromJson(data.inputStream().reader(), JsonObject::class.java).keySet().toList()
        } catch (e: Exception) {
            listOf()
        }
        val newEntries =
            downloadAndParseXmlVersions("https://files.minecraftforge.net/maven/de/oceanlabs/mcp/mcp_config/maven-metadata.xml")
        return (newEntries + oldEntries).distinct()
    }

    override suspend fun getLatestVersion(mcVersion: String): String? {
        return getVersions().firstOrNull { it.split("-")[0] == mcVersion }
    }

    override suspend fun getVersions(): List<String> {
        return versionsInternal.await()
    }

    override suspend fun getMappingsInternal(version: String): String? {
        if (Version.parse(version) < VERSION_1_12_2) {
            return downloadOldVersion(version)
        }
        val downloadURL =
            "https://files.minecraftforge.net/maven/de/oceanlabs/mcp/mcp_config/$version/mcp_config-$version.zip"
        val fileName = "config/joined.tsrg"
        return downloadAndExtract(downloadURL, fileName)
    }

    private suspend fun downloadOldVersion(version: String): String? {
        val downloadURL = "https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp/$version/mcp-$version-srg.zip"
        val fileName = "joined.srg"
        val data = downloadAndExtract(downloadURL, fileName) ?: return null

        val tree = MemoryMappingTree()
        val visitor = MappingNsRenamer(MissingDescFilter(tree), mapOf("source" to "obf", "target" to "srg"))
        MappingReader.read(data.reader(), visitor)
        val writer = StringWriter()
        val writerVisitor = MappingWriter.create(writer, MappingFormat.TSRG_2_FILE)
        tree.accept(writerVisitor)
        return writer.toString()
    }

}
