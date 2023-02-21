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

package net.ormr.jukkas.type.member

import net.ormr.jukkas.type.TypeOrError

class TypeMemberCache {
    // TODO: if two functions with same name and same parameters exist, report function overload resolution error
    //       do the same for properties
    private val members = hashMapOf<String, MutableList<TypeMember>>()

    fun define(name: String, member: TypeMember) {
        members.getOrPut(name) { mutableListOf() }.add(member)
    }

    fun find(name: String): List<TypeMember> = members[name] ?: emptyList()

    fun findProperties(name: String): List<TypeMember.Property> = find(name).filterIsInstance<TypeMember.Property>()

    // TODO: actually check type parameters and stuff
    fun findFunctions(name: String, typeParameters: List<TypeOrError>): List<TypeMember.Function> = find(name)
        .asSequence()
        .filterIsInstance<TypeMember.Function>()
        .toList()
}