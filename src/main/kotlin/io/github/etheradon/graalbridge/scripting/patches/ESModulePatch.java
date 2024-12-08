package io.github.etheradon.graalbridge.scripting.patches;

import net.lenni0451.classtransform.InjectionCallback;
import net.lenni0451.classtransform.annotations.CTarget;
import net.lenni0451.classtransform.annotations.CTransformer;
import net.lenni0451.classtransform.annotations.injection.CInject;

@CTransformer(name = "com.oracle.truffle.js.builtins.commonjs.CommonJSRequireBuiltin")
public class ESModulePatch {

    @CInject(method = "hasExtension", target = @CTarget("HEAD"), cancellable = true)
    private static void hasExtension(String filename, String ext, InjectionCallback ci) {
        if (filename.endsWith(".ts") && ext.equals(".js")) {
            ci.setReturnValue(true);
        }
    }

}
