package net.ormr.jukkas

import net.ormr.jukkas.oldtype.TypeResolver

@DslMarker
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class CompilerContextBuilderDsl

@CompilerContextBuilderDsl
class OldCompilerContextBuilder {
    var resolverBuilder: TypeResolverBuilder? = null

    @CompilerContextBuilderDsl
    inner class TypeResolverBuilder {
        val typeResolvers = mutableListOf<TypeResolver>()

        fun resolver(resolver: TypeResolver) {
            typeResolvers.add(resolver)
        }
    }

    fun types(builder: TypeResolverBuilder.() -> Unit) {
        require(resolverBuilder == null) { "'types' should only be declared once" }
        val resolverBuilder = TypeResolverBuilder()
        builder.invoke(resolverBuilder)
        this.resolverBuilder = resolverBuilder
    }

    fun build(): OldCompilerContext {
        require(resolverBuilder != null) { "'types' should be declared" }
        return OldCompilerContext(
            resolverBuilder!!.typeResolvers
        )
    }
}

fun buildCompilationContext(builder: OldCompilerContextBuilder.() -> Unit): OldCompilerContext {
    val compilerContextBuilder = OldCompilerContextBuilder()
    builder.invoke(compilerContextBuilder)
    return compilerContextBuilder.build()
}