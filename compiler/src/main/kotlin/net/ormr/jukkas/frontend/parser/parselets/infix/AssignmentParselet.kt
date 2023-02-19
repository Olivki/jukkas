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

package net.ormr.jukkas.frontend.parser.parselets.infix

import net.ormr.jukkas.createSpan
import net.ormr.jukkas.frontend.ast.AstAssignmentOperation
import net.ormr.jukkas.frontend.ast.AstExpression
import net.ormr.jukkas.frontend.lexer.Token
import net.ormr.jukkas.frontend.parser.JukkasParser
import net.ormr.jukkas.frontend.parser.Precedence
import net.ormr.jukkas.ir.AssignmentOperator

/**
 * Produces an [AstAssignmentOperation] instance.
 *
 * Assignments are *not* expression in Jukkas, they're still parsed *like* expressions here, this is to avoid making
 * the parser more complex that it needs to be. Usage of assignments as expression *(`if (a = b)`)* is verified at a
 * later stage.
 */
object AssignmentParselet : InfixParselet {
    private val knownOperators = AssignmentOperator.values().mapTo(hashSetOf()) { it.symbol }

    override val precedence: Int
        get() = Precedence.ASSIGNMENT

    override fun parse(
        parser: JukkasParser,
        left: AstExpression,
        token: Token,
    ): AstAssignmentOperation = parser with {
        val operator = AssignmentOperator.fromSymbolOrNull(token.text)
            ?: (token syntaxError "Unknown assignment operator")
        val value = parseExpression(precedence - 1) // left to right
        AstAssignmentOperation(left, operator, token, value, createSpan(left, value))
    }
}