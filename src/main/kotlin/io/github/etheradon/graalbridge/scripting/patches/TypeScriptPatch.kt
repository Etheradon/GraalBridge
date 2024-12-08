package io.github.etheradon.graalbridge.scripting.patches

import io.github.etheradon.graalbridge.scripting.utils.typescript.TypeScriptHelper
import net.lenni0451.classtransform.InjectionCallback
import net.lenni0451.classtransform.annotations.CLocalVariable
import net.lenni0451.classtransform.annotations.CTarget
import net.lenni0451.classtransform.annotations.CTransformer
import net.lenni0451.classtransform.annotations.injection.CInject
import net.lenni0451.classtransform.annotations.injection.CModifyExpressionValue
import java.net.URI

@CTransformer(name = ["com.oracle.truffle.api.source.SourceImpl\$Key"])
class SourceImplPatch {

    @CModifyExpressionValue(
        method = ["<init>"],
        target = CTarget(
            value = "FIELD",
            target = "Lcom/oracle/truffle/api/source/SourceImpl\$Key;content:Ljava/lang/Object;"
        )
    )
    fun init(
        content: Any?,
        @CLocalVariable(index = 0) thisRef: Any?,
        @CLocalVariable(index = 1) script: Any?,
        @CLocalVariable(index = 2) mimeType: String?,
        @CLocalVariable(index = 3) languageId: String?,
        @CLocalVariable(index = 4) name: String?,
        @CLocalVariable(index = 5) internal: Boolean,
    ): Any? {
        if (!internal && content is String) {
            return TypeScriptHelper.transpileCode(content, name!!)
        }
        return content
    }

}

@CTransformer(name = ["com.oracle.truffle.js.builtins.commonjs.NpmCompatibleESModuleLoader"])
class NpmModuleLoaderPatch {

    private val ESM_FORMAT =
        Class.forName("com.oracle.truffle.js.builtins.commonjs.NpmCompatibleESModuleLoader\$Format")
            .getEnumConstants()[1];

    @CInject(
        method = ["esmFileFormat"],
        target = [CTarget("HEAD")],
        cancellable = true
    )
    fun esmFileFormat(url: URI, env: Any, ci: InjectionCallback) {
        if (url.path.endsWith(".ts")) {
            ci.returnValue = ESM_FORMAT
        }
    }

}
