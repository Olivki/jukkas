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

sealed interface AstLiteralExpression : AstExpression

data class AstBooleanLiteral(val value: Boolean, override val position: Position) : AstLiteralExpression {
    override fun isStructurallyEquivalent(other: StructurallyComparable): Boolean =
        other is AstBooleanLiteral && value == other.value
}

data class AstCharLiteral(val value: Char, override val position: Position) : AstLiteralExpression {
    override fun isStructurallyEquivalent(other: StructurallyComparable): Boolean =
        other is AstCharLiteral && value == other.value
}

data class AstStringLiteral(val value: String, override val position: Position) : AstLiteralExpression {
    override fun isStructurallyEquivalent(other: StructurallyComparable): Boolean =
        other is AstStringLiteral && value == other.value
}

data class AstIntLiteral(val value: Int, override val position: Position) : AstLiteralExpression {
    override fun isStructurallyEquivalent(other: StructurallyComparable): Boolean =
        other is AstIntLiteral && value == other.value
}

data class AstFloatLiteral(val value: Float, override val position: Position) : AstLiteralExpression {
    override fun isStructurallyEquivalent(other: StructurallyComparable): Boolean =
        other is AstFloatLiteral && value == other.value
}