package io.github.etheradon.graalbridge.scripting.patches

import io.github.etheradon.graalbridge.scripting.patches.stubs.HostFieldDesc
import io.github.etheradon.graalbridge.scripting.patches.stubs.HostMethodDesc
import io.github.etheradon.graalbridge.scripting.utils.GraalUtilities
import io.github.etheradon.graalbridge.scripting.utils.mapping.MappingManager
import net.lenni0451.classtransform.InjectionCallback
import net.lenni0451.classtransform.annotations.CLocalVariable
import net.lenni0451.classtransform.annotations.CShadow
import net.lenni0451.classtransform.annotations.CTarget
import net.lenni0451.classtransform.annotations.CTransformer
import net.lenni0451.classtransform.annotations.injection.CASM
import net.lenni0451.classtransform.annotations.injection.CInject
import net.lenni0451.classtransform.annotations.injection.CRedirect
import net.lenni0451.classtransform.annotations.injection.CWrapCatch
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.*

@CTransformer(name = ["com.oracle.truffle.host.HostInteropReflect"])
object HostInteropPatch {

    @CRedirect(
        method = ["findInnerClass"],
        target = CTarget(value = "INVOKE", target = "Ljava/lang/String;equals(Ljava/lang/Object;)Z")
    )
    @JvmStatic
    fun modifyNameEquals(
        innerClassSimpleName: String,
        searchForName: Any,
        @CLocalVariable(index = 0) clazz: Class<*>
    ): Boolean {
        return innerClassSimpleName == searchForName || MappingManager.hasInnerClass(
            clazz,
            innerClassSimpleName,
            searchForName as String
        )
    }

}

@CTransformer(name = ["com.oracle.truffle.host.HostContext"])
abstract class HostContextPatch {

    @CShadow
    abstract fun findClassImpl(name: String): Class<*>

    @CWrapCatch(
        value = ["findClass"],
        target = "Lcom/oracle/truffle/host/HostContext;findClassImpl(Ljava/lang/String;)Ljava/lang/Class;"
    )
    fun injectFindClassImpl(@CLocalVariable(index = 1) name: String, e: Throwable): Class<*> {
        MappingManager.remapClass(name)?.let {
            return findClassImpl(it)
        }
        throw e
    }

}

@CTransformer(
    name = [
        "io.github.etheradon.graalbridge.scripting.patches.HostClassDescPatch",
    ]
)
object StubTransformer {

    @CASM(shift = CASM.Shift.TOP)
    @JvmStatic
    fun transform(node: ClassNode) {
        val oldPackage = "io/github/etheradon/graalbridge/scripting/patches/stubs"
        val newPackage = "com/oracle/truffle/host"
        node.fields.forEach { field ->
            field.desc = field.desc.replace(oldPackage, newPackage)
        }
        node.methods.forEach { method ->
            method.desc = method.desc.replace(oldPackage, newPackage)
            method.instructions?.iterator()?.forEach { insn ->
                if (insn is FieldInsnNode) {
                    // Update field references (getfield, putfield, etc.)
                    insn.owner = insn.owner.replace(oldPackage, newPackage)
                    insn.desc = insn.desc.replace(oldPackage, newPackage)
                }
                if (insn is MethodInsnNode) {
                    // Update method references (invokevirtual, invokestatic, etc.)
                    insn.owner = insn.owner.replace(oldPackage, newPackage)
                    insn.desc = insn.desc.replace(oldPackage, newPackage)
                }
                if (insn is LdcInsnNode && insn.cst is String) {
                    // Update any constant strings that reference the package
                    insn.cst = (insn.cst as String).replace(oldPackage, newPackage)
                }
                if (insn is TypeInsnNode) {
                    // Update type instructions (new, checkcast, instanceof, etc.)
                    insn.desc = insn.desc.replace(oldPackage, newPackage)
                }
            }
        }

        val classWriter = ClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS)
        node.accept(classWriter)
        GraalUtilities.classProvider.classToBytes[node.name] = classWriter.toByteArray()
    }

}

@CTransformer(name = ["com.oracle.truffle.host.HostClassDesc"])
abstract class HostClassDescPatch {

    @CShadow
    private var type: Class<*>? = null

    @CShadow
    abstract fun lookupField(name: String, onlyStatic: Boolean): HostFieldDesc?

    @CInject(
        method = ["Lcom/oracle/truffle/host/HostClassDesc;lookupField(Ljava/lang/String;Z)Lcom/oracle/truffle/host/HostFieldDesc;"],
        target = [CTarget("RETURN")],
        cancellable = true
    )
    fun lookupFieldPatch(name: String, onlyStatic: Boolean, ci: InjectionCallback) {
        if (ci.returnValue != null) {
            return
        }
        MappingManager.remapField(type!!, name)?.let {
            ci.returnValue = lookupField(it, onlyStatic)
        }
    }

    @CShadow
    abstract fun lookupMethod(name: String, onlyStatic: Boolean): HostMethodDesc?

    @CInject(
        method = ["Lcom/oracle/truffle/host/HostClassDesc;lookupMethod(Ljava/lang/String;Z)Lcom/oracle/truffle/host/HostMethodDesc;"],
        target = [CTarget("RETURN")],
        cancellable = true
    )
    fun lookupMethodPatch(name: String, onlyStatic: Boolean, ci: InjectionCallback) {
        if (ci.returnValue != null) {
            return
        }
        MappingManager.remapMethod(type!!, name)?.let {
            ci.returnValue = lookupMethod(it, onlyStatic)
        }
    }

    @CShadow
    abstract fun lookupMethodBySignature(nameAndSignature: String, onlyStatic: Boolean): HostMethodDesc?

    @CInject(
        method = ["Lcom/oracle/truffle/host/HostClassDesc;lookupMethodBySignature(Ljava/lang/String;Z)Lcom/oracle/truffle/host/HostMethodDesc;"],
        target = [CTarget("RETURN")],
        cancellable = true
    )
    fun lookupMethodBySignaturePatch(nameAndSignature: String, onlyStatic: Boolean, ci: InjectionCallback) {
        if (ci.returnValue != null) {
            return
        }
        MappingManager.remapMethodNameAndSignature(type!!, nameAndSignature)?.let {
            ci.returnValue = lookupMethodBySignature(it, onlyStatic)
        }
    }

}
