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
import net.ormr.jukkas.backend.ir.AssignmentOperator
import net.ormr.jukkas.backend.ir.BinaryOperator
import net.ormr.jukkas.frontend.lexer.Token
import net.ormr.jukkas.utils.checkStructuralEquivalence

sealed interface AstExpression : AstStatement

data class AstBlock(
    val children: List<AstStatement>,
    override val position: Position,
) : AstExpression {
    override fun isStructurallyEquivalent(other: StructurallyComparable): Boolean =
        other is AstBlock && checkStructuralEquivalence(children, other.children)
}

data class AstInvocationArgument(
    val name: Token?,
    val value: AstExpression,
    override val position: Position,
) : AstExpression {
    override fun isStructurallyEquivalent(other: StructurallyComparable): Boolean =
        other is AstInvocationArgument &&
            checkStructuralEquivalence(name, other.name) &&
            value isStructurallyEquivalent other.value
}

data class AstConditionalBranch(
    val condition: AstExpression,
    val thenBranch: AstExpression,
    val elseBranch: AstExpression?,
    override val position: Position,
) : AstExpression {
    override fun isStructurallyEquivalent(other: StructurallyComparable): Boolean =
        other is AstConditionalBranch &&
            condition isStructurallyEquivalent other.condition &&
            thenBranch isStructurallyEquivalent other.thenBranch &&
            checkStructuralEquivalence(elseBranch, other.elseBranch)
}

data class AstParenthesizedExpression(
    val expression: AstExpression,
    override val position: Position,
) : AstExpression {
    override fun isStructurallyEquivalent(other: StructurallyComparable): Boolean =
        other is AstParenthesizedExpression && expression isStructurallyEquivalent other.expression
}

data class AstIdentifierReference(val name: Token) : AstExpression {
    override val position: Position
        get() = name.point

    override fun isStructurallyEquivalent(other: StructurallyComparable): Boolean =
        other is AstIdentifierReference && name isStructurallyEquivalent other.name
}

data class AstReturn(val value: AstExpression?, override val position: Position) : AstExpression {
    override fun isStructurallyEquivalent(other: StructurallyComparable): Boolean =
        other is AstReturn && checkStructuralEquivalence(value, other.value)
}

data class AstStringTemplate(
    val parts: List<AstStringTemplatePart>,
    override val position: Position,
) : AstExpression {
    override fun isStructurallyEquivalent(other: StructurallyComparable): Boolean =
        other is AstStringTemplate && checkStructuralEquivalence(parts, other.parts)
}

data class AstAssignmentOperation(
    val left: AstExpression,
    val operator: AssignmentOperator,
    val operatorToken: Token,
    val value: AstExpression,
    override val position: Position,
) : AstExpression {
    override fun isStructurallyEquivalent(other: StructurallyComparable): Boolean =
        other is AstAssignmentOperation &&
            left isStructurallyEquivalent other.left &&
            operator == other.operator &&
            value isStructurallyEquivalent other.value
}

data class AstBinaryOperation(
    val left: AstExpression,
    val operator: BinaryOperator,
    val operatorToken: Token,
    val right: AstExpression,
    override val position: Position,
) : AstExpression {
    override fun isStructurallyEquivalent(other: StructurallyComparable): Boolean =
        other is AstBinaryOperation &&
            left isStructurallyEquivalent other.left &&
            operator == other.operator &&
            right isStructurallyEquivalent other.right
}

data class AstMemberAccessOperation(
    val left: AstExpression,
    val isSafeAccess: Boolean,
    val operatorToken: Token,
    val right: AstExpression,
    override val position: Position,
) : AstExpression {
    override fun isStructurallyEquivalent(other: StructurallyComparable): Boolean =
        other is AstMemberAccessOperation &&
            left isStructurallyEquivalent other.left &&
            isSafeAccess == other.isSafeAccess &&
            right isStructurallyEquivalent other.right
}