package io.github.etheradon.graalbridge.scripting.utils.typescript

import com.caoccao.javet.swc4j.Swc4j
import com.caoccao.javet.swc4j.enums.Swc4jMediaType
import com.caoccao.javet.swc4j.enums.Swc4jParseMode
import com.caoccao.javet.swc4j.enums.Swc4jSourceMapOption
import com.caoccao.javet.swc4j.options.Swc4jTranspileOptions
import com.caoccao.javet.swc4j.plugins.Swc4jPluginHost
import com.caoccao.javet.swc4j.plugins.Swc4jPluginVisitors
import java.util.concurrent.ConcurrentHashMap

object TypeScriptHelper {

    private var cache = ConcurrentHashMap<String, String>()

    private val transpileOptions = Swc4jTranspileOptions().apply {
        mediaType = Swc4jMediaType.TypeScript
        isKeepComments = true
        sourceMap = Swc4jSourceMapOption.None
        pluginHost = Swc4jPluginHost(listOf(Swc4jPluginVisitors(listOf(ImportConverter()))))
        parseMode = Swc4jParseMode.Module
    }

    fun transpileCode(code: String, name: String): String {
        if (!name.endsWith(".ts")) {
            return code
        }
        if (cache.containsKey(code)) {
            return cache[code]!!
        }
        val transpiledResult = Swc4j().transpile(code, transpileOptions)
        cache[code] = transpiledResult.code
        return transpiledResult.code
    }

}
