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

package net.ormr.jukkas.type

interface BuiltinTypes {
    val any: Type
    val nothing: Type
    val unit: Type
    val string: Type
    val boolean: Type
    val char: Type
    val int8: Type
    val int16: Type
    val int32: Type
    val int64: Type
    val float32: Type
    val float64: Type
}