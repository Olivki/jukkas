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
import net.ormr.jukkas.type.JvmPrimitiveType
import net.ormr.jukkas.type.JvmReferenceType
import net.ormr.jukkas.type.ResolvedType

sealed class Literal : Expression() {
    abstract override val type: ResolvedType
}

class SymbolLiteral(val text: String) : Literal() {
    override lateinit var type: ResolvedType

    override fun isStructurallyEquivalent(other: StructurallyComparable): Boolean =
        other is SymbolLiteral && text == other.text
}

class IntLiteral(val value: Int) : Literal() {
    override val type: ResolvedType
        get() = JvmPrimitiveType.INT

    override fun toString(): String = value.toString()

    override fun isStructurallyEquivalent(other: StructurallyComparable): Boolean =
        other is IntLiteral && value == other.value && type.isStructurallyEquivalent(other.type)
}

class BooleanLiteral(val value: Boolean) : Literal() {
    override val type: ResolvedType
        get() = JvmPrimitiveType.BOOLEAN

    override fun toString(): String = value.toString()

    override fun isStructurallyEquivalent(other: StructurallyComparable): Boolean =
        other is BooleanLiteral && value == other.value && type.isStructurallyEquivalent(other.type)
}

class StringLiteral(val value: String) : Literal() {
    override val type: ResolvedType
        get() = JvmReferenceType.STRING

    override fun toString(): String = "\"$value\""

    override fun isStructurallyEquivalent(other: StructurallyComparable): Boolean =
        other is StringLiteral && value == other.value && type.isStructurallyEquivalent(other.type)
}