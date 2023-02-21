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

package net.ormr.jukkas

import net.ormr.jukkas.backend.ir.BinaryOperator
import net.ormr.jukkas.frontend.ast.*
import net.ormr.jukkas.frontend.lexer.Token
import net.ormr.jukkas.frontend.lexer.TokenType

fun boolean(value: Boolean) = AstBooleanLiteral(value, ROOT_POINT)

fun int(value: Int) = AstIntLiteral(value, ROOT_POINT)

fun string(value: String) = AstStringLiteral(value, ROOT_POINT)

fun identifierToken(text: String) = Token(TokenType.IDENTIFIER, text, ROOT_POINT)

fun reference(name: String) = AstIdentifierReference(identifierToken(name), ROOT_POINT)

fun binary(
    left: AstExpression,
    operator: BinaryOperator,
    right: AstExpression,
) = AstBinaryOperation(left, operator, identifierToken(operator.symbol), right, ROOT_POINT)

fun memberAccess(
    left: AstExpression,
    isSafeAccess: Boolean,
    right: AstExpression,
) = AstMemberAccessOperation(left, isSafeAccess, identifierToken(if (isSafeAccess) "." else "?."), right, ROOT_POINT)

fun function(
    name: String,
    arguments: List<AstFunctionArgument>,
    body: AstExpression?,
    returnType: AstTypeName,
) = AstFunction(identifierToken(name), arguments, body, returnType, AstSymbolTable(), ROOT_POINT)

fun import(
    path: String,
    entries: List<AstImportEntry>,
) = AstImport(string(path), entries, ROOT_POINT)

fun importEntry(
    name: String,
    alias: String? = null,
) = AstImportEntry(identifierToken(name), alias?.let(::identifierToken), ROOT_POINT)

fun invArg(value: AstExpression, name: String? = null) =
    AstInvocationArgument(name?.let(::identifierToken), value, ROOT_POINT)

fun arg(
    name: String,
    type: AstTypeName,
    default: AstExpression? = null,
) = AstFunctionArgument(identifierToken(name), type, default, ROOT_POINT)

fun invocation(
    name: String,
    arguments: List<AstInvocationArgument>,
) = AstFunctionInvocation(identifierToken(name), arguments, ROOT_POINT)

fun typeName(name: String) = AstBasicTypeName(identifierToken(name))

fun undefinedTypeName() = AstUndefinedTypeName(ROOT_POINT)

val ROOT_POINT = Point.of(0, 0, 0, 0)