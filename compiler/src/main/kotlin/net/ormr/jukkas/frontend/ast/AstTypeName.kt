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
import net.ormr.jukkas.type.TypeOrError

sealed class AstTypeName : AstNode {
    var resolvedType: TypeOrError? = null

    abstract fun asString(): String
}

data class AstUndefinedTypeName(override val position: Position) : AstTypeName() {
    override fun asString(): String = "<undefined>"

    override fun isStructurallyEquivalent(other: StructurallyComparable): Boolean = other is AstUndefinedTypeName
}

sealed class AstExistingTypeName : AstTypeName()

data class AstBasicTypeName(val name: Token) : AstExistingTypeName() {
    override val position: Position
        get() = name.point

    override fun asString(): String = name.text

    override fun isStructurallyEquivalent(other: StructurallyComparable): Boolean =
        other is AstBasicTypeName && name isStructurallyEquivalent other.name
}

data class AstUnionTypeName(
    val left: AstExistingTypeName,
    val right: AstExistingTypeName,
    override val position: Position,
) : AstExistingTypeName() {
    override fun asString(): String = "${left.asString()} | ${right.asString()}"

    override fun isStructurallyEquivalent(other: StructurallyComparable): Boolean =
        other is AstUnionTypeName && left isStructurallyEquivalent other.left &&
            right isStructurallyEquivalent other.right
}

data class AstIntersectionTypeName(
    val left: AstExistingTypeName,
    val right: AstExistingTypeName,
    override val position: Position,
) : AstExistingTypeName() {
    override fun asString(): String = "${left.asString()} & ${right.asString()}"

    override fun isStructurallyEquivalent(other: StructurallyComparable): Boolean =
        other is AstIntersectionTypeName &&
            left isStructurallyEquivalent other.left &&
            right isStructurallyEquivalent other.right
}