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

package net.ormr.jukkas.frontend.ast

import net.ormr.jukkas.Position
import net.ormr.jukkas.StructurallyComparable
import net.ormr.jukkas.frontend.lexer.Token
import net.ormr.jukkas.utils.checkStructuralEquivalence

sealed interface AstExecutable : AstNode, AstHasTable, AstDefinition {
    val returnType: AstTypeName

    override fun findTypeName(): AstTypeName = returnType
}

data class AstFunction(
    override val name: Token,
    val arguments: List<AstFunctionArgument>,
    val body: AstExpression?,
    override val returnType: AstTypeName,
    override val table: AstSymbolTable,
    override val position: Position,
) : AstExecutable, AstStatement, AstTopLevelNode, AstNamedDefinition {
    override fun isStructurallyEquivalent(other: StructurallyComparable): Boolean =
        other is AstFunction &&
            name isStructurallyEquivalent other.name &&
            checkStructuralEquivalence(arguments, other.arguments) &&
            checkStructuralEquivalence(body, other.body) &&
            checkStructuralEquivalence(returnType, other.returnType)
}

data class AstAnonymousFunction(
    val arguments: List<AstFunctionArgument>,
    val body: AstExpression,
    override val returnType: AstTypeName,
    override val table: AstSymbolTable,
    override val position: Position,
) : AstExecutable, AstExpression, AstDefinition {

    override fun isStructurallyEquivalent(other: StructurallyComparable): Boolean =
        other is AstAnonymousFunction &&
            checkStructuralEquivalence(arguments, other.arguments) &&
            checkStructuralEquivalence(body, other.body) &&
            checkStructuralEquivalence(returnType, other.returnType)
}

data class AstLambda(
    val arguments: List<AstFunctionArgument>,
    val body: AstExpression,
    override val table: AstSymbolTable,
    override val position: Position,
) : AstExecutable, AstExpression, AstDefinition {
    // TODO: better position
    override val returnType: AstTypeName = AstUndefinedTypeName(body.findPosition())

    override fun isStructurallyEquivalent(other: StructurallyComparable): Boolean =
        other is AstLambda &&
            checkStructuralEquivalence(arguments, other.arguments) &&
            checkStructuralEquivalence(body, other.body)
}