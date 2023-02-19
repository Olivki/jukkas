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

sealed interface AstStatement : AstNode

data class AstExpressionStatement(val expression: AstExpression, override val position: Position) : AstStatement {
    override fun isStructurallyEquivalent(other: StructurallyComparable): Boolean =
        other is AstExpressionStatement && expression isStructurallyEquivalent other.expression
}

data class AstLocalVariable(
    val kind: Token,
    val name: Token,
    val type: AstTypeName?,
    val initializer: AstExpression?,
    override val position: Position,
) : AstStatement {
    override fun isStructurallyEquivalent(other: StructurallyComparable): Boolean =
        other is AstLocalVariable &&
            kind isStructurallyEquivalent other.kind &&
            name isStructurallyEquivalent other.name &&
            checkStructuralEquivalence(type, other.type) &&
            checkStructuralEquivalence(initializer, other.initializer)
}

data class AstProperty(
    val kind: Token,
    val name: Token,
    val type: AstTypeName?,
    val initializer: AstExpression?,
    override val position: Position,
) : AstStatement, AstTopLevelNode {
    override fun isStructurallyEquivalent(other: StructurallyComparable): Boolean =
        other is AstProperty &&
            kind isStructurallyEquivalent other.kind &&
            name isStructurallyEquivalent other.name &&
            checkStructuralEquivalence(type, other.type) &&
            checkStructuralEquivalence(initializer, other.initializer)
}