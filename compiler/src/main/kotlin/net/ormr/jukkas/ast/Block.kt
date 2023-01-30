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
import net.ormr.jukkas.type.UnknownType

class Block(override val table: Table, statements: List<Statement>) : Expression(), TableContainer {
    override var type: Type = UnknownType
    val statements: MutableNodeList<Statement> = statements.toMutableNodeList(this, ::onAddChild, ::onRemoveChild)

    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visitBlock(this)

    override fun isStructurallyEquivalent(other: Node): Boolean =
        other is Block
        && statements.size == other.statements.size
        && (this.statements zip other.statements).all { (first, second) -> first.isStructurallyEquivalent(second) }

    private fun onAddChild(index: Int, node: Statement) {
        if (node is Definition) {
            val name = node.name ?: return
            table.define(name, node)
        }
    }

    private fun onRemoveChild(index: Int, node: Statement) {
        if (node is Definition) {
            val name = node.name ?: return
            table.undefine(name)
        }
    }
}