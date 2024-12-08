package io.github.etheradon.graalbridge.mapping

import java.io.Reader

fun interface MappingLoader {
    fun load(mapping: Mapping): Reader
}
