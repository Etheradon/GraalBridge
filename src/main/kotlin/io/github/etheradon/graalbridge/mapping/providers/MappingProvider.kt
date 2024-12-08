package io.github.etheradon.graalbridge.mapping.providers

import com.google.gson.Gson
import io.github.etheradon.graalbridge.mapping.Namespace
import kotlinx.coroutines.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.Reader
import java.io.StringReader
import java.net.URI
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import javax.xml.parsers.DocumentBuilderFactory

// TODO: Clean up
abstract class MappingProvider<V : Any>(val namespace: Namespace) {

    protected val gson = Gson()
    private var versionCache: Map<String, String> = mutableMapOf()

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    protected val versionsInternal: Deferred<List<V>> by lazy {
        coroutineScope.async {
            fetchVersions()
        }
    }

    protected abstract suspend fun fetchVersions(): List<V>

    protected suspend fun downloadFileFromUrlToMemory(url: String, maxRetries: Int = 1): ByteArray {
        var retries = 0
        var lastException: Exception? = null

        while (retries < maxRetries) {
            try {
                return withContext(Dispatchers.IO) {
                    URI(url).toURL().readBytes()
                }
            } catch (e: Exception) {
                lastException = e
                retries++
                if (retries < maxRetries) {
                    val maxBackOff = 10_000L
                    val delayMs = (1000L * (1 shl retries)).coerceAtMost(maxBackOff) // Exponential backoff
                    println("Error downloading from $url (attempt $retries): ${e.message}. Retrying in ${delayMs}ms.")
                    delay(delayMs)
                }
            }
        }

        throw lastException ?: IllegalStateException("Failed to download from $url after $maxRetries attempts")
    }

    abstract suspend fun getLatestVersion(mcVersion: String): String?

    abstract suspend fun getVersions(): List<String>

    protected abstract suspend fun getMappingsInternal(version: String): String?

    suspend fun getMappings(version: String): Reader? {
        versionCache[version]?.let { return StringReader(it) }

        val mappings = getMappingsInternal(version)
        if (mappings != null) {
            versionCache += version to mappings
            return StringReader(mappings)
        }
        return null
    }

    protected suspend fun downloadAndExtract(downloadUrl: String, fileName: String): String? {
        val data: ByteArray = try {
            downloadFileFromUrlToMemory(downloadUrl)
        } catch (e: Exception) {
            println("Error downloading mappings: ${e.localizedMessage}")
            e.printStackTrace()
            return null
        }
        val zip = ZipInputStream(ByteArrayInputStream(data))
        var entry: ZipEntry?
        while (zip.nextEntry.also { entry = it } != null) {
            if (entry!!.name == fileName) {
                val buffer = ByteArrayOutputStream()
                zip.copyTo(buffer)
                return buffer.toString()
            }
        }
        return null
    }

    protected suspend fun downloadAndParseXmlVersions(manifestUrl: String): List<String> {
        val data: ByteArray = try {
            downloadFileFromUrlToMemory(manifestUrl)
        } catch (e: Exception) {
            println("Error fetching version manifest: ${e.localizedMessage}")
            e.printStackTrace()
            return emptyList()
        }
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val doc = builder.parse(data.inputStream())
        doc.normalize()
        val nodes = doc.getElementsByTagName("version")
        return List(nodes.length) {
            nodes.item(it).textContent
        }
    }

}
