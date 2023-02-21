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

package net.ormr.jukkas.oldtype

import net.ormr.jukkas.Positionable
import net.ormr.jukkas.backend.ir.CompilationUnit
import net.ormr.jukkas.backend.ir.reportSemanticError

class TypeCache internal constructor(private val unit: CompilationUnit) {
    private val entries = hashMapOf<String, ResolvedType>()

    fun find(name: String): ResolvedType? = entries[name]

    fun define(position: Positionable, type: ResolvedType, alias: String? = null) {
        if (alias == null) {
            addType(position, type.simpleName, type)
            if (type.internalName != type.simpleName) {
                addType(position, type.internalName, type)
            }
        } else {
            // TODO: is this the proper way of handling aliases?
            addType(position, alias, type)
        }
    }

    private fun addType(
        position: Positionable,
        name: String,
        type: ResolvedType,
    ) {
        if (name in entries) {
            unit.reportSemanticError(position, "Redefining name: $name")
        } else {
            entries[name] = type
        }
    }
}