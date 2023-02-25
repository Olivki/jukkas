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

package net.ormr.jukkas.ast

import net.ormr.jukkas.StructurallyComparable

sealed class Literal : AbstractExpression()

class IntLiteral(val value: Int) : Literal() {
    override fun toString(): String = value.toString()

    override fun isStructurallyEquivalent(other: StructurallyComparable): Boolean =
        other is IntLiteral && value == other.value
}

class LongLiteral(val value: Long) : Literal() {
    override fun toString(): String = value.toString()

    override fun isStructurallyEquivalent(other: StructurallyComparable): Boolean =
        other is LongLiteral && value == other.value
}

class BooleanLiteral(val value: Boolean) : Literal() {
    override fun toString(): String = value.toString()

    override fun isStructurallyEquivalent(other: StructurallyComparable): Boolean =
        other is BooleanLiteral && value == other.value
}

class StringLiteral(val value: String) : Literal() {
    override fun toString(): String = "\"$value\""

    override fun isStructurallyEquivalent(other: StructurallyComparable): Boolean =
        other is StringLiteral && value == other.value
}