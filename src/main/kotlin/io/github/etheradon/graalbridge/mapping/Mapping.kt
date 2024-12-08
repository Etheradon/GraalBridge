package io.github.etheradon.graalbridge.mapping

sealed class Mapping(
    val name: String,
    val source: Namespace,
    vararg destinations: Namespace
) {
    val destinations: List<Namespace> = destinations.toList()
    val containedTypes: List<Namespace> get() = listOf(source) + this.destinations

    init {
        if (destinations.isEmpty() && this !is NONE) {
            throw IllegalArgumentException("Mappings must contain at least 1 destination")
        }
    }

    object INTERMEDIARY : Mapping("INTERMEDIARY", Namespace.OFFICIAL, Namespace.INTERMEDIARY)
    object YARN : Mapping("YARN", Namespace.INTERMEDIARY, Namespace.YARN)
    object MOJMAP : Mapping("MOJMAP", Namespace.MOJMAP, Namespace.OFFICIAL)
    object SRG : Mapping("SRG", Namespace.OFFICIAL, Namespace.SRG)
    object HASHED : Mapping("HASHED", Namespace.OFFICIAL, Namespace.HASHED)
    object QUILT : Mapping("QUILT", Namespace.HASHED, Namespace.QUILT)
    object NONE : Mapping("NONE", Namespace.Custom("NONE"))

    class Custom(name: String, source: Namespace, vararg destinations: Namespace) :
        Mapping(name, source, *destinations)

    companion object {
        val values = listOf(INTERMEDIARY, YARN, MOJMAP, SRG, HASHED, QUILT, NONE)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Mapping) return false

        return name == other.name &&
                source == other.source &&
                destinations == other.destinations
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + source.hashCode()
        result = 31 * result + destinations.hashCode()
        return result
    }

    override fun toString(): String {
        return "Mapping(name='$name', source=$source, destinations=$destinations)"
    }
}
