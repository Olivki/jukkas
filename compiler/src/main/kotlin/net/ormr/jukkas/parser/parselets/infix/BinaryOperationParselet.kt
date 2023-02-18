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

package net.ormr.jukkas.parser.parselets.infix

import net.ormr.jukkas.ast.AstBinaryOperation
import net.ormr.jukkas.ast.AstExpression
import net.ormr.jukkas.createSpan
import net.ormr.jukkas.ir.BinaryOperator
import net.ormr.jukkas.lexer.Token
import net.ormr.jukkas.parser.JukkasParser

class BinaryOperationParselet(override val precedence: Int) : InfixParselet {
    override fun parse(
        parser: JukkasParser,
        left: AstExpression,
        token: Token,
    ): AstBinaryOperation = parser with {
        val operator = BinaryOperator.fromSymbolOrNull(token.text)
            ?: (token syntaxError "Unknown assignment operator")
        val right = parseExpression(precedence)
        AstBinaryOperation(left, operator, token, right, createSpan(left, right))
    }
}