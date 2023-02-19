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
import net.ormr.jukkas.type.Type
import net.ormr.jukkas.type.UnknownType
import net.ormr.jukkas.type.member.TypeMember

class DefinitionReference(val name: String) : Expression(), HasMutableType {
    override var type: Type = UnknownType

    // TODO: support references to property members too
    var member: TypeMember.Field? = null

    /**
     * Whether `this` represents a static reference.
     *
     * A static reference is something like `System.out` where `System` is just a type name.
     */
    var isStaticReference: Boolean = false

    fun find(table: Table): NamedDefinition? = table.find(name)

    override fun isStructurallyEquivalent(other: StructurallyComparable): Boolean =
        other is DefinitionReference &&
                name == other.name &&
                type.isStructurallyEquivalent(other.type)

    override fun toString(): String = "DefinitionReference(name='$name', type=$type)"

    operator fun component1(): String = name

    operator fun component2(): Type = type
}