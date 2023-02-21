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

package net.ormr.jukkas.backend.ir

import net.ormr.jukkas.StructurallyComparable
import net.ormr.jukkas.oldtype.JvmReferenceType
import net.ormr.jukkas.oldtype.ResolvedType
import net.ormr.jukkas.utils.checkStructuralEquivalence

sealed class StringTemplatePart : ChildNode() {
    class LiteralPart(val literal: StringLiteral) : StringTemplatePart() {
        override fun isStructurallyEquivalent(other: StructurallyComparable): Boolean =
            other is LiteralPart && literal.isStructurallyEquivalent(other.literal)
    }

    class ExpressionPart(val expression: Expression) : StringTemplatePart() {
        override fun isStructurallyEquivalent(other: StructurallyComparable): Boolean =
            other is ExpressionPart && expression.isStructurallyEquivalent(other.expression)
    }
}

class StringTemplateExpression(parts: List<StringTemplatePart>) : Expression() {
    val parts: MutableNodeList<StringTemplatePart> = parts.toMutableNodeList(this)

    override val type: ResolvedType
        get() = JvmReferenceType.STRING

    override fun isStructurallyEquivalent(other: StructurallyComparable): Boolean =
        other is StringTemplateExpression &&
            checkStructuralEquivalence(parts, other.parts) &&
            type.isStructurallyEquivalent(other.type)
}