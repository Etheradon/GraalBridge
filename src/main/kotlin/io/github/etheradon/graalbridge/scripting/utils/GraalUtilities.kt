package io.github.etheradon.graalbridge.scripting.utils

import io.github.etheradon.graalbridge.scripting.patches.*
import net.bytebuddy.agent.ByteBuddyAgent
import net.lenni0451.classtransform.TransformerManager
import net.lenni0451.classtransform.transformer.IRawTransformer
import net.lenni0451.classtransform.utils.tree.BasicClassProvider
import org.objectweb.asm.tree.ClassNode
import java.lang.instrument.Instrumentation


object GraalUtilities {

    val classProvider = ForwardingClassProvider()
    val transformerManager = TransformerManager(classProvider)
    lateinit var inst: Instrumentation

    var isMultiThreadedAccessAllowed: Boolean = false

    val importPackagePrefixes = mutableListOf("java", "net", "com", "org", "io", "xyz")

    fun matchesImportPackage(impSrc: String): Boolean {
        return importPackagePrefixes.any { impSrc.startsWith("$it.") }
    }

    private fun loadInstrumentation() {
        if (System.getProperty("java.version").startsWith("1.8")) {
            throw UnsupportedOperationException("Java 8 is not supported!")
        } else {
            inst = ByteBuddyAgent.install()
        }
    }

    fun install() {
        loadInstrumentation()
        transformerManager.addTransformer(StubTransformer::class.java.name)
        transformerManager.hookInstrumentation(inst)
        val mixins = buildList {
            add(ThreadingPatch::class)
            add(SourceImplPatch::class)
            add(SourceBuilderPatch::class)
            add(HostContextPatch::class)
            add(HostInteropPatch::class)
            add(HostClassDescPatch::class)
        }

        mixins.forEach { transformerManager.addTransformer(it.java.name) }
        transformerManager.addRawTransformer("org.graalvm.polyglot.Engine", InstallationTransformer())
    }

}

object Agent {
    @JvmStatic
    fun agentmain(agentArgs: String?, inst: Instrumentation) {
        GraalUtilities.inst = inst
    }
}

class ForwardingClassProvider : BasicClassProvider() {
    val classToBytes = mutableMapOf<String, ByteArray>()

    override fun getClass(name: String): ByteArray {
        return classToBytes[name.replace('.', '/')] ?: super.getClass(name)
    }
}

class InstallationTransformer : IRawTransformer {

    override fun transform(transformerManager: TransformerManager, classNode: ClassNode): ClassNode {
        val isGraal23OrNewer = classNode.version >= 61
        println("Graal 23 or newer: $isGraal23OrNewer")

        if (isGraal23OrNewer) {
            transformerManager.addTransformer(NpmModuleLoaderPatch::class.java.name)
        } else {
            transformerManager.addTransformer(ESModulePatch::class.java.name)
        }
        return classNode
    }
}
