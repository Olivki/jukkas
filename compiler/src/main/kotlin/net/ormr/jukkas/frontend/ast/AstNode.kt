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
import net.ormr.jukkas.Positionable
import net.ormr.jukkas.Source
import net.ormr.jukkas.StructurallyComparable
import net.ormr.jukkas.frontend.lexer.Token
import net.ormr.jukkas.utils.checkStructuralEquivalence

sealed interface AstNode : Positionable, StructurallyComparable {
    val position: Position

    override fun findPositionOrNull(): Position = position
}

data class AstCompilationUnit(
    val source: Source,
    val imports: List<AstImport>,
    val entries: List<AstTopLevelNode>,
    override val position: Position,
) : AstNode {
    override fun isStructurallyEquivalent(other: StructurallyComparable): Boolean =
        other is AstCompilationUnit &&
                checkStructuralEquivalence(imports, other.imports) &&
                checkStructuralEquivalence(entries, other.entries)
}

data class AstImport(
    val path: AstStringLiteral,
    val entries: List<AstImportEntry>,
    override val position: Position,
) : AstNode, AstTopLevelNode {
    override fun isStructurallyEquivalent(other: StructurallyComparable): Boolean =
        other is AstImport &&
                path isStructurallyEquivalent other.path &&
                checkStructuralEquivalence(entries, other.entries)
}

data class AstImportEntry(val name: Token, val alias: Token?, override val position: Position) : AstNode {
    override fun isStructurallyEquivalent(other: StructurallyComparable): Boolean =
        other is AstImportEntry &&
                name isStructurallyEquivalent other.name &&
                checkStructuralEquivalence(alias, other.alias)
}

data class AstFunctionArgument(
    val name: Token,
    val type: AstTypeName,
    val default: AstExpression?,
    override val position: Position,
) : AstNode {
    override fun isStructurallyEquivalent(other: StructurallyComparable): Boolean =
        other is AstFunctionArgument &&
                name isStructurallyEquivalent other.name &&
                type isStructurallyEquivalent other.type &&
                checkStructuralEquivalence(default, other.default)
}

sealed interface AstStringTemplatePart : AstNode {
    data class Literal(val literal: AstStringLiteral, override val position: Position) : AstStringTemplatePart {
        override fun isStructurallyEquivalent(other: StructurallyComparable): Boolean =
            other is Literal && literal isStructurallyEquivalent other.literal
    }

    data class Expression(val expression: AstExpression, override val position: Position) : AstStringTemplatePart {
        override fun isStructurallyEquivalent(other: StructurallyComparable): Boolean =
            other is Expression && expression isStructurallyEquivalent other.expression
    }
}