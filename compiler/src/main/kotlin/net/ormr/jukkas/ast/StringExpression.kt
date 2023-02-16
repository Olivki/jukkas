package net.ormr.jukkas.ast

import net.ormr.jukkas.StructurallyComparable
import net.ormr.jukkas.type.JvmReferenceType
import net.ormr.jukkas.type.ResolvedType
import net.ormr.jukkas.utils.checkStructuralEquivalence

sealed class StringTemplatePart : ChildNode() {
    class LiteralPart(val literal: StringLiteral) : StringTemplatePart() {
        override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visitLiteral(literal)

        override fun isStructurallyEquivalent(other: StructurallyComparable): Boolean =
            other is LiteralPart && literal.isStructurallyEquivalent(other.literal)
    }

    class ExpressionPart(val expression: Expression) : StringTemplatePart() {
        override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visitExpression(expression)

        override fun isStructurallyEquivalent(other: StructurallyComparable): Boolean =
            other is ExpressionPart && expression.isStructurallyEquivalent(other.expression)
    }
}

class StringTemplateExpression(parts: List<StringTemplatePart>) : Expression() {
    val parts: MutableNodeList<StringTemplatePart> = parts.toMutableNodeList(this)

    override val type: ResolvedType
        get() = JvmReferenceType.STRING

    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visitStringTemplateExpression(this)

    override fun isStructurallyEquivalent(other: StructurallyComparable): Boolean =
        other is StringTemplateExpression &&
                checkStructuralEquivalence(parts, other.parts) &&
                type.isStructurallyEquivalent(other.type)
}