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

package net.ormr.jukkas

abstract class AbstractSymbolTable<T : AbstractSymbolTable<T, E>, E> {
    abstract val parent: T?
    protected val entries = hashMapOf<String, E>()

    fun find(name: String): E? {
        walkHierarchy { table ->
            val entry = table.entries[name]
            if (entry != null) return entry
        }
        return null
    }

    fun define(name: String, definition: E) {
        require(name !in entries) { "Definition with name '$name' already exists" }
        entries[name] = definition
    }

    fun hasLocal(name: String): Boolean = name in entries

    private inline fun walkHierarchy(action: (AbstractSymbolTable<T, E>) -> Unit) {
        var current: AbstractSymbolTable<T, E>? = this
        while (current != null) {
            action(current)
            current = current.parent
        }
    }
}