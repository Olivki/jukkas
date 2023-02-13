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

import net.ormr.jukkas.type.Type

sealed class Argument : Statement() {
    final override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visitArgument(this)
}

sealed class NamedArgument : Argument(), Definition {
    abstract val name: String
    abstract override var type: Type
}

class BasicArgument(override val name: String, override var type: Type) : NamedArgument() {
    override fun isStructurallyEquivalent(other: Node): Boolean =
        other is BasicArgument && name == other.name
}

class DefaultArgument(
override val name: String,
 override var type: Type,
 default: Expression,
) : NamedArgument() {
    var default: Expression by child(default)

    override fun isStructurallyEquivalent(other: Node): Boolean =
        other is DefaultArgument && name == other.name && default.isStructurallyEquivalent(other.default)
}

// TODO: we probably don't want to support arbitrary pattern matching for arguments,
// as that behavior would be relatively weird, just supporting basic
// destructuring is probably the safest
class PatternArgument(pattern: Pattern) : Argument() {
    var pattern: Pattern by child(pattern)

    override fun isStructurallyEquivalent(other: Node): Boolean =
        other is PatternArgument && pattern.isStructurallyEquivalent(other.pattern)
}