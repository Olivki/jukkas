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

package net.ormr.jukkas.oldtype.member

import net.ormr.jukkas.oldtype.AsmMethodType
import net.ormr.jukkas.oldtype.ResolvedType

sealed interface TypeMember {
    val name: String
    val declaringType: ResolvedType

    val isStatic: Boolean

    sealed interface Executable : TypeMember {
        val parameterTypes: List<ResolvedType>

        val returnType: ResolvedType

        fun toAsmType(): AsmMethodType
    }

    sealed interface Method : Executable

    sealed interface Constructor : Executable

    sealed interface Field : TypeMember {
        val type: ResolvedType
    }
}