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

import net.ormr.jukkas.frontend.ast.AstIntLiteral
import net.ormr.jukkas.frontend.lexer.Token
import net.ormr.jukkas.frontend.parser.JukkasParser

object IntParselet : PrefixParselet {
    override fun parse(parser: JukkasParser, token: Token): AstIntLiteral = parser with {
        // TODO: handle underscores, and handle potential overflow by parsing as BigInteger
        val value = token.text.toInt()
        AstIntLiteral(value, token.point)
    }
}