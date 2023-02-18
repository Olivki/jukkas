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

package net.ormr.jukkas.parser.parselets.prefix

import net.ormr.jukkas.ast.AstAnonymousFunction
import net.ormr.jukkas.createSpan
import net.ormr.jukkas.lexer.Token
import net.ormr.jukkas.lexer.TokenType.*
import net.ormr.jukkas.parser.JukkasParser
import net.ormr.jukkas.parser.JukkasParser.Companion.IDENTIFIERS

object AnonymousFunctionParselet : PrefixParselet {
    override fun parse(parser: JukkasParser, token: Token): AstAnonymousFunction = parser with {
        val name = consumeIfMatch(IDENTIFIERS, "identifier")
        name?.syntaxError("Anonymous functions with names are prohibited")
        consume(LEFT_PAREN)
        val arguments = parseArguments(COMMA, RIGHT_PAREN, ::parseFunctionArgument)
        val argEnd = consume(RIGHT_PAREN)
        val returnType = parseOptionalTypeDeclaration(ARROW)
        val returnTypePosition = returnType?.position
        val body = when {
            match(EQUAL) -> parseExpressionStatement().expression
            match(LEFT_BRACE) -> parseBlock(RIGHT_BRACE)
            else -> createSpan(token, returnTypePosition ?: argEnd) syntaxError "Function must have a body"
        }
        AstAnonymousFunction(arguments, body, returnType, createSpan(token, body))
    }
}