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

sealed interface AstInvocation : AstExpression

data class AstFunctionInvocation(
    val name: Token,
    val arguments: List<AstInvocationArgument>,
    override val position: Position,
) : AstInvocation {
    override fun isStructurallyEquivalent(other: StructurallyComparable): Boolean =
        other is AstFunctionInvocation &&
                name isStructurallyEquivalent other.name &&
                checkStructuralEquivalence(arguments, other.arguments)
}

data class AstAnonymousFunctionInvocation(
    val left: AstExpression,
    val arguments: List<AstInvocationArgument>,
    override val position: Position,
) : AstInvocation {
    override fun isStructurallyEquivalent(other: StructurallyComparable): Boolean =
        other is AstAnonymousFunctionInvocation &&
                left isStructurallyEquivalent other.left &&
                checkStructuralEquivalence(arguments, other.arguments)
}

data class AstInfixInvocation(
    val left: AstExpression,
    val name: Token,
    val right: AstExpression,
    override val position: Position,
) : AstInvocation {
    override fun isStructurallyEquivalent(other: StructurallyComparable): Boolean =
        other is AstInfixInvocation &&
                left isStructurallyEquivalent other.left &&
                name isStructurallyEquivalent other.name &&
                right isStructurallyEquivalent other.right
}