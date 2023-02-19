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

package net.ormr.jukkas.parser

import io.kotest.core.spec.style.FunSpec
import net.ormr.jukkas.ROOT_POINT
import net.ormr.jukkas.frontend.ast.AstStringTemplate
import net.ormr.jukkas.binary
import net.ormr.jukkas.boolean
import net.ormr.jukkas.int
import net.ormr.jukkas.invArg
import net.ormr.jukkas.invocation
import net.ormr.jukkas.ir.BinaryOperator.DIVISION
import net.ormr.jukkas.ir.BinaryOperator.MINUS
import net.ormr.jukkas.ir.BinaryOperator.MULTIPLICATION
import net.ormr.jukkas.ir.BinaryOperator.PLUS
import net.ormr.jukkas.memberAccess
import net.ormr.jukkas.parseExpression
import net.ormr.jukkas.reference
import net.ormr.jukkas.shouldBeStructurallyEquivalentTo
import net.ormr.jukkas.shouldBeSuccess
import net.ormr.jukkas.string
import net.ormr.jukkas.frontend.ast.AstStringTemplatePart.Expression as ExpressionPart
import net.ormr.jukkas.frontend.ast.AstStringTemplatePart.Literal as LiteralPart

class ExpressionParsingTest : FunSpec({
    test("'false' should parse to BooleanLiteral(false)") {
        parseExpression("false") shouldBeSuccess { expr, _ ->
            expr shouldBeStructurallyEquivalentTo boolean(false)
        }
    }

    test("'true' should parse to BooleanLiteral(true)") {
        parseExpression("true") shouldBeSuccess { expr, _ ->
            expr shouldBeStructurallyEquivalentTo boolean(true)
        }
    }

    test("'12345' should parse to IntLiteral(true)") {
        parseExpression("12345") shouldBeSuccess { expr, _ ->
            expr shouldBeStructurallyEquivalentTo int(12345)
        }
    }

    test("'foo' should parse to DefinitionReference('foo')") {
        parseExpression("foo") shouldBeSuccess { expr, _ ->
            expr shouldBeStructurallyEquivalentTo reference("foo")
        }
    }

    test("\"foo\" should parse to StringLiteral(\"foo\")") {
        parseExpression("\"foo\"") shouldBeSuccess { expr, _ ->
            expr shouldBeStructurallyEquivalentTo string("foo")
        }
    }

    test("Parse string literal with unicode") {
        parseExpression("\"\\u0000\\u0000\\u0000\"") shouldBeSuccess { expr, _ ->
            expr shouldBeStructurallyEquivalentTo string("\u0000\u0000\u0000")
        }
    }

    test("\"foo {1 + 2} bar\" should parse to StringExpression(...)") {
        parseExpression("\"foo \\{1 + 2} bar\"") shouldBeSuccess { expr, _ ->
            expr shouldBeStructurallyEquivalentTo AstStringTemplate(
                listOf(
                    LiteralPart(string("foo "), ROOT_POINT),
                    ExpressionPart(binary(int(1), PLUS, int(2)), ROOT_POINT),
                    LiteralPart(string(" bar"), ROOT_POINT),
                ),
                ROOT_POINT,
            )
        }
    }

    test("'foo(1, bar = 2, 3)' should parse to FunctionInvocation(...)") {
        parseExpression("foo(1, bar = 2, 3)") shouldBeSuccess { expr, _ ->
            expr shouldBeStructurallyEquivalentTo invocation(
                "foo",
                listOf(
                    invArg(int(1)),
                    invArg(int(2), "bar"),
                    invArg(int(3)),
                ),
            )
        }
    }

    test("'foo.bar(1)' should parse to (foo.bar (1))") {
        parseExpression("foo.bar(1)") shouldBeSuccess { expr, _ ->
            expr shouldBeStructurallyEquivalentTo memberAccess(
                reference("foo"),
                isSafeAccess = false,
                invocation(
                    "bar",
                    listOf(
                        invArg(int(1)),
                    ),
                ),
            )
        }
    }

    test("'1 + 2' -> (+ 1 2)") {
        parseExpression("1 + 2") shouldBeSuccess { expr, _ ->
            expr shouldBeStructurallyEquivalentTo binary(int(1), PLUS, int(2))
        }
    }

    test("'1 + 2 + 3' -> (+ (+ 1 2) 3)") {
        parseExpression("1 + 2 + 3") shouldBeSuccess { expr, _ ->
            expr shouldBeStructurallyEquivalentTo binary(
                binary(
                    int(1),
                    PLUS,
                    int(2),
                ),
                PLUS,
                int(3),
            )
        }
    }

    test("'1 - 2 * 3 / 4' -> (+ 1 (/ (* 2 3) 4)") {
        parseExpression("1 - 2 * 3 / 4") shouldBeSuccess { expr, _ ->
            expr shouldBeStructurallyEquivalentTo binary(
                int(1),
                MINUS,
                binary(
                    binary(
                        int(2),
                        MULTIPLICATION,
                        int(3),
                    ),
                    DIVISION,
                    int(4),
                ),
            )
        }
    }
})