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

package net.ormr.jukkas.frontend.phase

import net.ormr.jukkas.Positionable
import net.ormr.jukkas.Source
import net.ormr.jukkas.frontend.ast.AstHasTable
import net.ormr.jukkas.frontend.ast.AstIdentifierReference
import net.ormr.jukkas.frontend.ast.AstNamedDefinition
import net.ormr.jukkas.frontend.ast.AstSymbolTable
import net.ormr.jukkas.oldtype.OldType
import net.ormr.jukkas.reporter.MessageReporter
import net.ormr.jukkas.reporter.MessageType
import net.ormr.jukkas.type.ErrorType
import net.ormr.jukkas.utils.identifierName

sealed class FrontendPhase(private val source: Source) {
    protected val tables = ArrayDeque<AstSymbolTable>()
    protected val reporter: MessageReporter = MessageReporter()

    protected fun reportSemanticError(position: Positionable, message: String) {
        reporter.reportError(source, MessageType.Error.SEMANTIC, position, message)
    }

    protected fun reportInternalError(position: Positionable, message: String) {
        reporter.reportError(source, MessageType.Error.INTERNAL, position, message)
    }

    protected fun errorType(position: Positionable, description: String): ErrorType {
        reportTypeError(position, description)
        return ErrorType(description)
    }

    protected fun reportTypeError(position: Positionable, message: String) {
        reporter.reportError(source, MessageType.Error.TYPE, position, message)
    }

    protected fun formatIncompatibleTypes(expected: OldType, got: OldType): String =
        "Expected type <${expected.internalName}> got <${got.internalName}>"

    protected inline infix fun <R> AstHasTable.withTable(block: () -> R): R {
        tables.addFirst(table)
        return try {
            block()
        } finally {
            tables.removeFirst()
        }
    }

    protected fun findDefinition(reference: AstIdentifierReference): AstNamedDefinition? {
        val table = tables.firstOrNull()
        if (table == null) {
            reportInternalError(reference, "Scopes is empty, no lookup available")
            return null
        }
        return table.find(reference.name.identifierName)
    }
}
