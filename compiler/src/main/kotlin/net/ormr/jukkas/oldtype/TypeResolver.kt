package net.ormr.jukkas.oldtype

interface TypeResolver {
    fun resolve(path: String, symbol: String): ResolvedType?
}