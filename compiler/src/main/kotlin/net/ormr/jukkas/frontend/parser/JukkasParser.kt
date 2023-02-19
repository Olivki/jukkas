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

package net.ormr.jukkas.frontend.parser

import net.ormr.jukkas.JukkasResult
import net.ormr.jukkas.Source
import net.ormr.jukkas.createSpan
import net.ormr.jukkas.frontend.ast.AstBasicTypeName
import net.ormr.jukkas.frontend.ast.AstBlock
import net.ormr.jukkas.frontend.ast.AstCompilationUnit
import net.ormr.jukkas.frontend.ast.AstExpression
import net.ormr.jukkas.frontend.ast.AstExpressionStatement
import net.ormr.jukkas.frontend.ast.AstFunction
import net.ormr.jukkas.frontend.ast.AstFunctionArgument
import net.ormr.jukkas.frontend.ast.AstImport
import net.ormr.jukkas.frontend.ast.AstImportEntry
import net.ormr.jukkas.frontend.ast.AstInvocationArgument
import net.ormr.jukkas.frontend.ast.AstLocalVariable
import net.ormr.jukkas.frontend.ast.AstProperty
import net.ormr.jukkas.frontend.ast.AstStatement
import net.ormr.jukkas.frontend.ast.AstStringLiteral
import net.ormr.jukkas.frontend.ast.AstTopLevelNode
import net.ormr.jukkas.frontend.ast.AstTypeName
import net.ormr.jukkas.frontend.lexer.Token
import net.ormr.jukkas.frontend.lexer.TokenStream
import net.ormr.jukkas.frontend.lexer.TokenType
import net.ormr.jukkas.frontend.lexer.TokenType.*
import net.ormr.jukkas.frontend.parser.parselets.prefix.PrefixParselet
import net.ormr.jukkas.frontend.parser.parselets.prefix.StringParselet
import net.ormr.jukkas.ir.Expression
import java.nio.file.Path

class JukkasParser private constructor(tokens: TokenStream) : Parser(tokens) {
    fun parseCompilationUnit(): AstCompilationUnit {
        val imports = buildList {
            while (hasMore()) {
                if (!check(IMPORT)) break
                add(parseImport() ?: continue)
            }
        }
        val children = buildList {
            while (hasMore()) {
                add(parseTopLevel() ?: continue)
            }
        }
        val end = consume(END_OF_FILE)
        val position = children.firstOrNull()?.let { createSpan(it, end) } ?: end.findPosition()
        return AstCompilationUnit(source, imports, children, position)
    }

    fun consumeIdentifier(): Token = consume(IDENTIFIERS, "identifier")

    /**
     * Returns a list of identifiers in a potentially qualified name.
     *
     * Does not error if the given identifier is a plain identifier, but rather just returns a list of size `1`.
     *
     * Note that the separators *(`/`)* are not included in the list, *only* the identifiers are.
     */
    // TODO: handle nested classes identifiers
    fun parseQualifiedIdentifier(): List<Token> = buildList {
        add(consumeIdentifier())
        while (match(SLASH)) {
            add(consumeIdentifier())
        }
    }

    /**
     * Returns an [Expression] parsed from the available tokens, or `null` if no [PrefixParselet] could be found for
     * the [current] token.
     *
     * If no `PrefixParselet` could be found, then the consumed `current` token will be [unconsume]d.
     *
     * @see [parseExpression]
     */
    fun parseExpressionOrNull(precedence: Int = 0): AstExpression? {
        var token = consume()
        val prefix = Grammar.getPrefixParselet(token)
        if (prefix == null) {
            unconsume()
            return null
        }
        var left = prefix.parse(this, token)

        while (precedence < Grammar.getPrecedence(current())) {
            token = consume()
            val infix = Grammar.getInfixParselet(token) ?: (token syntaxError "Unknown infix operator '${token.text}'")
            left = infix.parse(this, left, token)
        }

        return left
    }

    fun parseExpression(precedence: Int = 0): AstExpression =
        parseExpressionOrNull(precedence) ?: (current() syntaxError "Expecting expression got ${previous().type}")

    fun parseImport(): AstImport? = withSynchronization(
        { check<TopSynch>() },
        { null },
    ) {
        val keyword = consume(IMPORT)
        val pathStart = consume(STRING_START)
        val path = StringParselet.parse(this, pathStart)
        consume(LEFT_BRACE)
        val entries = parseArguments(COMMA, RIGHT_BRACE, ::parseImportEntry)
        val end = consume(RIGHT_BRACE)
        // report error here so we can attempt to properly parse the symbol block first
        if (path !is AstStringLiteral) path syntaxError "Only simple strings are allowed as paths"
        AstImport(path, entries, createSpan(keyword, end))
    }

    private fun parseImportEntry(): AstImportEntry {
        // TODO: handle nested classes identifiers
        val name = consumeIdentifier()
        val alias = when {
            match(AS) -> consumeIdentifier()
            else -> null
        }
        val position = alias?.let { createSpan(name, it) } ?: name.findPosition()
        return AstImportEntry(name, alias, position)
    }

    private fun parseTopLevel(): AstTopLevelNode? = withSynchronization(
        { check<TopSynch>() },
        { null },
    ) {
        when {
            check(FUN) -> parseFunction()
            check(PROPERTIES) -> parseProperty()
            check(IMPORT) -> current() syntaxError "'import' must be declared before anything else"
            else -> current() syntaxError "Expected a top level declaration"
        }
    }

    private fun parseBasicTypeName(): AstBasicTypeName {
        val identifier = consumeIdentifier()
        return AstBasicTypeName(identifier, identifier.findPosition())
    }

    fun parseTypeName(): AstTypeName = parseBasicTypeName()

    fun parseTypeDeclaration(separator: TokenType = COLON): AstTypeName {
        consume(separator)
        return parseTypeName()
    }

    fun parseOptionalTypeDeclaration(separator: TokenType = COLON): AstTypeName? = when {
        check(separator) -> parseTypeDeclaration(separator)
        else -> null
    }

    private fun parseFunction(): AstFunction {
        val keyword = consume(FUN)
        val name = consumeIdentifier()
        consume(LEFT_PAREN)
        val arguments = parseArguments(COMMA, RIGHT_PAREN, ::parseFunctionArgument)
        val argEnd = consume(RIGHT_PAREN)
        val returnType = parseOptionalTypeDeclaration(ARROW)
        val returnTypePosition = returnType?.position
        val body = when {
            match(EQUAL) -> parseExpressionStatement().expression
            match(LEFT_BRACE) -> parseBlock(RIGHT_BRACE)
            // TODO: verify that the function is actually abstract if no body exists in the verifier
            //       and also verify that only class level functions are marked abstract and that stuff
            else -> null
        }
        val position = createSpan(keyword, body ?: returnTypePosition ?: argEnd)
        return AstFunction(name, arguments, body, returnType, position)
    }

    private fun parseProperty(): AstProperty = TODO()

    inline fun <T> parseArguments(
        separator: TokenType,
        terminator: TokenType,
        parse: () -> T,
    ): List<T> = when {
        check(terminator) -> emptyList()
        else -> buildList {
            add(parse())
            while (match(separator)) {
                // allows for trailing terminators
                if (check(terminator)) break
                add(parse())
            }
        }
    }

    fun parseInvocationArgument(): AstInvocationArgument {
        val name = consumeIfMatch(IDENTIFIERS, "identifier")
        if (name != null) consume(EQUAL)
        val value = parseExpression()
        val position = name?.let { createSpan(it, value) } ?: value.findPosition()
        return AstInvocationArgument(name, value, position)
    }

    fun parseFunctionArgument(): AstFunctionArgument {
        val name = consumeIdentifier()
        val type = parseTypeDeclaration()
        val default = if (match(EQUAL)) parseExpression() else null
        val position = default?.let { createSpan(name, it) } ?: createSpan(name, type)
        return AstFunctionArgument(name, type, default, position)
    }

    private fun parseVariable(): AstLocalVariable = TODO("parseVariable")

    fun parseStatement(): AstStatement = when {
        check(FUN) -> parseFunction()
        check(PROPERTIES) -> parseVariable()
        else -> parseExpressionStatement()
    }

    fun parseExpressionStatement(precedence: Int = 0): AstExpressionStatement {
        val expr = parseExpression(precedence)
        val end = consume(SEMICOLON)
        return AstExpressionStatement(expr, createSpan(expr, end))
    }

    fun parseBlockOrExpression(blockStart: TokenType, blockEnd: TokenType): AstExpression = when {
        match(blockStart) -> parseBlock(blockEnd)
        else -> parseExpression()
    }

    fun parseBlock(blockEnd: TokenType): AstBlock {
        val start = previous()
        val children = buildList {
            while (!check(blockEnd) && hasMore()) {
                val statement = withSynchronization({ check<BlockSynch>() }, { null }, ::parseStatement) ?: continue
                add(statement)
            }
        }
        val end = consume(blockEnd)
        return AstBlock(children, createSpan(start, end))
    }

    inline infix fun <R> with(block: JukkasParser.() -> R): R = run(block)

    companion object {
        internal val IDENTIFIERS = TokenType.setOf<IdentifierLike>()
        internal val PROPERTIES = hashSetOf(VAL, VAR)

        @PublishedApi
        internal fun of(source: Source): JukkasParser {
            val tokens = TokenStream.from(source)
            return JukkasParser(tokens)
        }

        inline fun <T> parse(source: Source, crossinline action: JukkasParser.() -> T): JukkasResult<T> {
            val parser = of(source)
            return try {
                parser.reporter.toResult { parser.use(action) }
            } catch (_: JukkasParseException) {
                JukkasResult.Failure(parser.reporter.messages)
            }
        }

        fun parseText(text: String): JukkasResult<AstCompilationUnit> =
            parse(
                Source.Text(text),
                JukkasParser::parseCompilationUnit
            )

        fun parseFile(file: Path): JukkasResult<AstCompilationUnit> =
            parse(
                Source.File(file),
                JukkasParser::parseCompilationUnit
            )
    }
}