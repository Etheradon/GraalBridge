package io.github.etheradon.graalbridge.scripting.utils.typescript

import com.caoccao.javet.swc4j.ast.enums.Swc4jAstVarDeclKind
import com.caoccao.javet.swc4j.ast.expr.*
import com.caoccao.javet.swc4j.ast.expr.lit.Swc4jAstStr
import com.caoccao.javet.swc4j.ast.interfaces.ISwc4jAstImportSpecifier
import com.caoccao.javet.swc4j.ast.module.*
import com.caoccao.javet.swc4j.ast.pat.Swc4jAstBindingIdent
import com.caoccao.javet.swc4j.ast.program.Swc4jAstModule
import com.caoccao.javet.swc4j.ast.stmt.Swc4jAstEmptyStmt
import com.caoccao.javet.swc4j.ast.stmt.Swc4jAstVarDecl
import com.caoccao.javet.swc4j.ast.stmt.Swc4jAstVarDeclarator
import com.caoccao.javet.swc4j.ast.visitors.Swc4jAstVisitor
import com.caoccao.javet.swc4j.ast.visitors.Swc4jAstVisitorResponse
import io.github.etheradon.graalbridge.scripting.utils.GraalUtilities
import io.github.etheradon.graalbridge.scripting.utils.mapping.MappingManager

class ImportConverter : Swc4jAstVisitor() {

    private fun generateNamedImport(node: Swc4jAstImportNamedSpecifier): Swc4jAstVarDecl? {
        val newName = node.local.sym
        return generateJavaTypeImport(node, node.imported.map { it.toString() }.orElse(newName), newName)
    }

    private fun generateDefaultImport(node: Swc4jAstImportDefaultSpecifier): Swc4jAstVarDecl? {
        return generateJavaTypeImport(node, node.local.sym)
    }

    private fun generateJavaTypeImport(
        node: ISwc4jAstImportSpecifier,
        importName: String,
        newName: String = importName
    ): Swc4jAstVarDecl? {
        val parent = node.getParent(Swc4jAstImportDecl::class.java)
        val sourceName =
            parent.get().src.raw.get().substring(1, parent.get().src.raw.get().length - 1)
        if (!GraalUtilities.matchesImportPackage(sourceName)) {
            return null
        }

        var isValueImport = false
        var isInnerClass = false
        try {
            Class.forName(MappingManager.remapClass(sourceName) ?: sourceName)
            val innerClassName = "$sourceName$$importName"
            try {
                Class.forName(MappingManager.remapClass(innerClassName) ?: innerClassName)
                isInnerClass = true
            } catch (_: Exception) {
                // If it's not an inner class, treat it as a normal import
                isValueImport = true
            }
        } catch (_: Exception) {
            // Ignore
        }

        val javaImport = if (isValueImport) sourceName else "$sourceName${if (isInnerClass) '$' else '.'}$importName"

        val javaType = Swc4jAstCallExpr.create(
            Swc4jAstMemberExpr.create(Swc4jAstIdent.create("Java"), Swc4jAstIdentName.create("type")),
            mutableListOf(Swc4jAstExprOrSpread.create(Swc4jAstStr.create(javaImport)))
        )

        if (isValueImport) {
            return Swc4jAstVarDecl.create(
                Swc4jAstVarDeclKind.Const, mutableListOf(
                    Swc4jAstVarDeclarator.create(
                        Swc4jAstBindingIdent.create(Swc4jAstIdent.create(newName)),
                        Swc4jAstMemberExpr.create(
                            javaType,
                            Swc4jAstIdentName.create(importName)
                        )
                    )
                )
            )
        }
        return Swc4jAstVarDecl.create(
            Swc4jAstVarDeclKind.Const, mutableListOf(
                Swc4jAstVarDeclarator.create(
                    Swc4jAstBindingIdent.create(Swc4jAstIdent.create(newName)), javaType
                )
            )
        )
    }

    override fun visitImportDecl(node: Swc4jAstImportDecl): Swc4jAstVisitorResponse {
        // Here is the entry for the import statement
        val body = node.getParent(Swc4jAstModule::class.java).get().body

        val newNodes = node.specifiers.mapNotNull {
            when (it) {
                is Swc4jAstImportDefaultSpecifier -> generateDefaultImport(it)
                is Swc4jAstImportNamedSpecifier -> generateNamedImport(it)
                else -> null
            }
        }
        if (newNodes.isNotEmpty()) {
            body.addAll(body.indexOf(node) + 1, newNodes)
            node.parent.replaceNode(node, Swc4jAstEmptyStmt.create())
        }

        return super.visitImportDecl(node)
    }

    /**
     * Here iff import is named, e.g. import { a as A, b as B, c as C } from ...
     * Logic is implemented in [visitImportDecl]
     */
    override fun visitImportNamedSpecifier(node: Swc4jAstImportNamedSpecifier): Swc4jAstVisitorResponse {

        return super.visitImportNamedSpecifier(node)
    }

    /**
     * Here iff import is not named, e.g. import { a, b, c } from ...
     * Logic is implemented in [visitImportDecl]
     */
    override fun visitImportDefaultSpecifier(node: Swc4jAstImportDefaultSpecifier): Swc4jAstVisitorResponse {
        return super.visitImportDefaultSpecifier(node)
    }

    override fun visitImport(node: Swc4jAstImport): Swc4jAstVisitorResponse {
        // When is this called?
        return super.visitImport(node)
    }

    // Not supported
    /**
     * Here iff import is *, e.g. import * as ... from ...
     */
    override fun visitImportStarAsSpecifier(node: Swc4jAstImportStarAsSpecifier): Swc4jAstVisitorResponse {
        val parent = node.getParent(Swc4jAstImportDecl::class.java)
        val impSrc = parent.get().src.raw.get().substring(1, parent.get().src.raw.get().length - 1)
        if (GraalUtilities.matchesImportPackage(impSrc)) {
            throw UnsupportedOperationException("Wildcard imports are not supported")
        }
        return super.visitImportStarAsSpecifier(node)
    }

    /**
     * Here iff import is =, e.g. import ... = require(...)
     */
    override fun visitTsImportEqualsDecl(node: Swc4jAstTsImportEqualsDecl): Swc4jAstVisitorResponse {
        return super.visitTsImportEqualsDecl(node)
    }

}
