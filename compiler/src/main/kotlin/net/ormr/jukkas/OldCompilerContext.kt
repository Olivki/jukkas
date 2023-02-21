package net.ormr.jukkas

import net.ormr.jukkas.oldtype.ResolvedType
import net.ormr.jukkas.oldtype.TypeResolver

class OldCompilerContext(val typeResolvers: List<TypeResolver>) {
    fun resolveType(path: String, symbol: String): ResolvedType? =
        typeResolvers.firstNotNullOfOrNull { it.resolve(path, symbol) }
}