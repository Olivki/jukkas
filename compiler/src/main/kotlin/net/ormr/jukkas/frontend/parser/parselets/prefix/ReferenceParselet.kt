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
import net.ormr.jukkas.frontend.ast.AstExpression
import net.ormr.jukkas.frontend.ast.AstFunctionInvocation
import net.ormr.jukkas.frontend.ast.AstIdentifierReference
import net.ormr.jukkas.frontend.lexer.Token
import net.ormr.jukkas.frontend.lexer.TokenType.COMMA
import net.ormr.jukkas.frontend.lexer.TokenType.LEFT_PAREN
import net.ormr.jukkas.frontend.lexer.TokenType.RIGHT_PAREN
import net.ormr.jukkas.frontend.parser.JukkasParser

object ReferenceParselet : PrefixParselet {
    override fun parse(parser: JukkasParser, token: Token): AstExpression = parser with {
        when {
            match(LEFT_PAREN) -> {
                val arguments = parseArguments(COMMA, RIGHT_PAREN, ::parseInvocationArgument)
                val end = consume(RIGHT_PAREN)
                AstFunctionInvocation(token, arguments, createSpan(token, end))
            }
            else -> AstIdentifierReference(token, token.point)
        }
    }
}