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

import net.ormr.jukkas.ast.AstExpression
import net.ormr.jukkas.ast.AstStringLiteral
import net.ormr.jukkas.ast.AstStringTemplate
import net.ormr.jukkas.ast.AstStringTemplatePart
import net.ormr.jukkas.createSpan
import net.ormr.jukkas.lexer.Token
import net.ormr.jukkas.lexer.TokenType.ESCAPE_SEQUENCE
import net.ormr.jukkas.lexer.TokenType.STRING_CONTENT
import net.ormr.jukkas.lexer.TokenType.STRING_END
import net.ormr.jukkas.lexer.TokenType.STRING_TEMPLATE_END
import net.ormr.jukkas.lexer.TokenType.STRING_TEMPLATE_START
import net.ormr.jukkas.parser.JukkasParser
import net.ormr.jukkas.utils.unescapeUnicode
import net.ormr.jukkas.ast.AstStringTemplatePart.Expression as ExpressionPart
import net.ormr.jukkas.ast.AstStringTemplatePart.Literal as LiteralPart

object StringParselet : PrefixParselet {
    @Suppress("UNCHECKED_CAST")
    override fun parse(parser: JukkasParser, token: Token): AstExpression = parser with {
        val parts = buildList {
            while (!check(STRING_END) && hasMore()) {
                add(parseLiteralOrTemplate(parser))
            }
        }
        val end = consume(STRING_END)

        when {
            // If the string does not have any template variables, just join all parts into a single literal
            // TODO: may be a good idea to also merge consecutive literals
            parts.all { it is LiteralPart } -> {
                val literalParts = parts as List<LiteralPart>
                val value = literalParts.joinToString("") { it.literal.value }
                AstStringLiteral(value, createSpan(token, end))
            }
            else -> AstStringTemplate(parts, createSpan(token, end))
        }
    }

    private fun parseLiteralOrTemplate(parser: JukkasParser): AstStringTemplatePart = parser with {
        when {
            match(STRING_CONTENT) -> {
                val content = previous()
                val text = content.text
                val position = content.point
                val literal = AstStringLiteral(text, position)
                LiteralPart(literal, position)
            }
            match(ESCAPE_SEQUENCE) -> {
                val sequence = previous()
                val text = previous().text.unescapeUnicode()
                val position = sequence.point
                val literal = AstStringLiteral(text, position)
                LiteralPart(literal, position)
            }
            match(STRING_TEMPLATE_START) -> {
                val start = current()
                val expression = parseExpression()
                val end = consume(STRING_TEMPLATE_END)
                ExpressionPart(expression, createSpan(start, end))
            }
            else -> current() syntaxError "Unexpected token in string <${current()}>"
        }
    }
}