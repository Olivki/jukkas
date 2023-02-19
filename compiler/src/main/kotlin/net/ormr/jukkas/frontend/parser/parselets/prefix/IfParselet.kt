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

package net.ormr.jukkas.frontend.parser.parselets.prefix

import net.ormr.jukkas.createSpan
import net.ormr.jukkas.frontend.ast.AstConditionalBranch
import net.ormr.jukkas.frontend.lexer.Token
import net.ormr.jukkas.frontend.lexer.TokenType.ELSE
import net.ormr.jukkas.frontend.lexer.TokenType.LEFT_BRACE
import net.ormr.jukkas.frontend.lexer.TokenType.LEFT_PAREN
import net.ormr.jukkas.frontend.lexer.TokenType.RIGHT_BRACE
import net.ormr.jukkas.frontend.lexer.TokenType.RIGHT_PAREN
import net.ormr.jukkas.frontend.parser.JukkasParser

object IfParselet : PrefixParselet {
    override fun parse(parser: JukkasParser, token: Token): AstConditionalBranch = parser with {
        consume(LEFT_PAREN)
        val condition = parseExpression()
        consume(RIGHT_PAREN)
        val thenBranch = parseBlockOrExpression(LEFT_BRACE, RIGHT_BRACE)
        val elseBranch = if (match(ELSE)) parseBlockOrExpression(LEFT_BRACE, RIGHT_BRACE) else null
        val position = elseBranch?.let { createSpan(token, it) } ?: createSpan(token, thenBranch)
        AstConditionalBranch(condition, thenBranch, elseBranch, position)
    }
}