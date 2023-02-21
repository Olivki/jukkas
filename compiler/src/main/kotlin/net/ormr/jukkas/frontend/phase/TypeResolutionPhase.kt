/*
 * Copyright 2023 Oliver Berg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.ormr.jukkas.frontend.phase

import net.ormr.jukkas.CompilerContext
import net.ormr.jukkas.Positionable
import net.ormr.jukkas.Source
import net.ormr.jukkas.frontend.ast.*
import net.ormr.jukkas.type.BuiltinTypes
import net.ormr.jukkas.type.ContainerType
import net.ormr.jukkas.type.ErrorType
import net.ormr.jukkas.type.IntersectionType
import net.ormr.jukkas.type.Type
import net.ormr.jukkas.type.TypeCache
import net.ormr.jukkas.type.TypeOrError
import net.ormr.jukkas.type.UnionType
import net.ormr.jukkas.type.flatMap
import net.ormr.jukkas.type.member.TypeMember
import net.ormr.jukkas.type.member.TypeMemberCache
import net.ormr.jukkas.utils.identifierName
import net.ormr.jukkas.utils.unreachable

internal class TypeResolutionPhase private constructor(
    source: Source,
    private val context: CompilerContext,
) : FrontendPhase(source) {
    private val types = TypeCache()
    private val importedMembers = TypeMemberCache()
    private val builtinTypes: BuiltinTypes
        get() = context.builtinTypes

    private fun check(node: AstNode?) {
        when (node) {
            is AstCompilationUnit -> node withTable {
                checkNodes(node.imports)
                checkNodes(node.entries)
            }
            is AstHasType -> resolve(node)
            is AstImport -> resolveImports(node)
            is AstImportEntry -> unreachable<AstImportEntry>()
            is AstExpressionStatement -> inferType(node.expression)
            is AstStringTemplatePart.Expression -> TODO()
            is AstStringTemplatePart.Literal -> TODO()
            is AstTypeName, null -> {
                // do nothing
            }
        }
    }

    private fun resolve(node: AstHasType): TypeOrError = when (node) {
        is AstDefinition -> resolveDefinition(node)
        is AstExpression -> inferType(node)
    }

    private fun resolveImports(node: AstImport) {
        val path = node.path.value
        for (entry in node.entries) {
            val name = entry.name.text
            val alias = entry.alias?.text
            val type = context.resolveType(path, name)
            if (type != null) {
                defineType(entry, alias ?: name, type)
            } else {
                val members = context.findMembers(path, name)
                if (members.isNotEmpty()) {
                    members.forEach { importedMembers.define(alias ?: name, it) }
                } else {
                    reportSemanticError(entry, "Unable to find $path/$name")
                }
            }
        }
    }

    private fun defineType(positionable: Positionable, name: String, type: Type) {
        if (name !in types) {
            types.define(name, type)
        } else {
            reportSemanticError(positionable, "Import for $name already exists")
        }
    }

    private fun resolveDefinition(node: AstDefinition): TypeOrError = node resolveIfNeeded {
        when (node) {
            is AstAnonymousFunction -> node withTable {
                TODO()
            }
            is AstFunction -> node withTable {
                checkNodes(node.arguments)
                check(node.body)
                // TODO: actually infer the type
                resolveTypeNameIfNeeded(node.returnType) { builtinTypes.unit }
            }
            is AstLambda -> node withTable {
                TODO()
            }
            is AstFunctionArgument -> resolveTypeName(node.type)
            is AstLocalVariable -> resolveTypeNameIfNeeded(node.type) {
                resolveIfPossible(node.initializer) {
                    errorType(node, "Could not infer variable type, please specify it explicitly.")
                }
            }
            is AstProperty -> TODO()
        }
    }

    // TODO: we should only infer types of expressions when it's *actually* needed
    private fun inferType(node: AstExpression): TypeOrError = node resolveIfNeeded {
        when (node) {
            is AstDefinition -> resolveDefinition(node)
            is AstAssignmentOperation -> TODO()
            is AstBinaryOperation -> TODO()
            is AstBlock -> node withTable {
                // TODO: infer type of block
                node.children.forEach(::check)
                builtinTypes.unit
            }
            is AstConditionalBranch -> {
                // TODO: infer type of conditional branch
                check(node.condition)
                check(node.thenBranch)
                check(node.elseBranch)
                builtinTypes.unit
            }
            is AstIdentifierReference -> resolveReference(node)
            is AstAnonymousFunctionInvocation -> TODO()
            is AstFunctionInvocation -> TODO()
            is AstInfixInvocation -> TODO()
            is AstInvocationArgument -> TODO()
            is AstLiteralExpression -> when (node) {
                is AstBooleanLiteral -> builtinTypes.boolean
                is AstCharLiteral -> builtinTypes.char
                is AstFloatLiteral -> builtinTypes.float32
                is AstIntLiteral -> builtinTypes.int32
                is AstStringLiteral -> builtinTypes.string
            }
            is AstMemberAccessOperation -> resolveAccess(node)
            is AstParenthesizedExpression -> inferType(node.expression)
            is AstReturn -> TODO()
            is AstStringTemplate -> builtinTypes.string
        }
    }

    private fun resolveReference(
        node: AstIdentifierReference,
    ): TypeOrError = when (val definition = findDefinition(node)) {
        null -> {
            val name = node.name.identifierName
            // if no direct definition could be found then we check the type cache for any matching type names
            when (val type = types.find(name)) {
                // if no type is found, check through the imported members for a match
                null -> findImportedProperty(node, name)
                else -> {
                    node.kind = getReferenceKind(type)
                    type
                }
            }
        }
        else -> {
            node.kind = AstReferenceKind.LOCAL
            resolveDefinition(definition)
        }
    }

    private fun findImportedProperty(node: AstIdentifierReference, name: String): TypeOrError {
        val properties = importedMembers.findProperties(name)
        return if (properties.isNotEmpty()) {
            if (properties.size > 1) {
                val property = properties.single()
                node.kind = AstReferenceKind.TOP_LEVEL
                node.member = property
                property.returnType
            } else {
                // TODO: report info about all the matches
                semanticErrorType(node, "Property overload resolution error")
            }
        } else {
            unresolvedReference(node, name)
        }
    }

    // TODO: a STATIC 'AstIdentifierReference' just by itself should be an error if used as an expression
    private fun getReferenceKind(type: TypeOrError): AstReferenceKind = when (type) {
        is ContainerType -> if (type.isObject) AstReferenceKind.OBJECT else AstReferenceKind.STATIC
        else -> AstReferenceKind.STATIC
    }

    private fun resolveAccess(
        node: AstMemberAccessOperation,
    ): TypeOrError = resolve(node.left).flatMap { targetType ->
        // this is illegal for now, but should probably be legal once we add extension functions?
        if (targetType !is ContainerType) {
            return@flatMap semanticErrorType(
                node,
                "Member access operation is illegal on complex type: $targetType",
            )
        }
        // TODO: check if we can even access the member
        when (val right = node.right) {
            is AstFunctionInvocation -> {
                val name = right.name.identifierName
                val params = right.arguments.map { resolve(it.value) }
                when (val function = targetType.findFunction(name, params)) {
                    null -> {
                        val signature = "$name(${params.joinToString { it.asString() }})"
                        semanticErrorType(right, "No function found with signature: $signature")
                    }
                    else -> useIfPublic(right, function) {
                        node.member = it
                        right.member = it
                    }
                }.updateNode(right)
            }
            is AstIdentifierReference -> {
                val name = right.name.identifierName
                when (val property = targetType.findProperty(name)) {
                    null -> unresolvedReference(right, name)
                    else -> useIfPublic(right, property) {
                        node.member = it
                        right.member = it
                    }
                }.updateNode(right)
            }
            else -> errorType(right, "Expected invocation or reference")
        }
    }

    private fun checkNodes(nodes: Iterable<AstNode>) {
        for (node in nodes) check(node)
    }

    private inline fun <T : TypeMember> useIfPublic(
        position: Positionable,
        member: T,
        action: (T) -> Unit,
    ): TypeOrError = when {
        member.isPublic -> {
            action(member)
            member.returnType
        }
        else -> semanticErrorType(position, "Can't access non public member: ${member.name}")
    }

    private inline infix fun <reified T : AstDefinition> T.resolveIfNeeded(
        crossinline action: T.() -> TypeOrError,
    ): TypeOrError {
        val typeName = findTypeName()
        return when (val type = typeName.resolvedType) {
            null -> {
                val newType = action()
                typeName.resolvedType = newType
                newType
            }
            else -> type
        }
    }

    private inline infix fun <reified T : AstExpression> T.resolveIfNeeded(
        crossinline action: T.() -> TypeOrError,
    ): TypeOrError {
        return when (val type = resolvedType) {
            null -> {
                val newType = action()
                resolvedType = newType
                newType
            }
            else -> type
        }
    }

    private inline fun resolveIfPossible(
        node: AstHasType?,
        fallback: () -> TypeOrError,
    ): TypeOrError = when (node) {
        null -> fallback()
        else -> resolve(node)
    }

    private inline fun resolveTypeNameIfNeeded(
        typeName: AstTypeName,
        ifUndefined: () -> TypeOrError,
    ): TypeOrError = when (typeName) {
        is AstUndefinedTypeName -> ifUndefined()
        is AstExistingTypeName -> when (val type = typeName.resolvedType) {
            null -> resolveTypeName(typeName)
            else -> type
        }
    }

    private fun unresolvedReference(position: Positionable, name: String): ErrorType =
        semanticErrorType(position, "Unresolved reference: $name")

    private fun semanticErrorType(position: Positionable, message: String): ErrorType {
        reportSemanticError(position, message)
        return ErrorType(message)
    }

    private fun TypeOrError.updateNode(node: AstNode): TypeOrError = apply {
        if (node is AstHasType) {
            when (node) {
                is AstDefinition -> node.findTypeName().resolvedType = this
                is AstExpression -> node.resolvedType = this
            }
        }
    }

    private fun resolveTypeName(typeName: AstTypeName): TypeOrError = types.findOrDefine(typeName.asString()) {
        when (typeName) {
            is AstBasicTypeName -> errorType(typeName, "Unknown type name '$typeName'")
            is AstIntersectionTypeName -> resolveTypeName(typeName.left).flatMap { left ->
                resolveTypeName(typeName.right).flatMap { right ->
                    IntersectionType(left, right)
                }
            }
            is AstUnionTypeName -> resolveTypeName(typeName.left).flatMap { left ->
                resolveTypeName(typeName.right).flatMap { right ->
                    UnionType(left, right)
                }
            }
            is AstUndefinedTypeName -> errorType(typeName, "Unresolved type")
        }
    }
}