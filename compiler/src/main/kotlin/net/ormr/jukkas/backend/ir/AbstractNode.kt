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

package net.ormr.jukkas.backend.ir

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

sealed class AbstractNode : net.ormr.jukkas.backend.ir.Node {
    override var parent: net.ormr.jukkas.backend.ir.Node? = null

    final override fun <T : net.ormr.jukkas.backend.ir.Node> adopt(child: T): T {
        if (child.parent !== this) {
            child.disownParent()
            child.parent = this
            childAdopted(child)
        }

        return child
    }

    protected open fun childAdopted(child: net.ormr.jukkas.backend.ir.Node) {}

    final override fun <T : net.ormr.jukkas.backend.ir.Node> disown(child: T): T {
        if (child.parent === this) {
            child.parent = null
            childDisowned(child)
        }

        return child
    }

    protected open fun childDisowned(child: net.ormr.jukkas.backend.ir.Node) {}

    protected fun <T : net.ormr.jukkas.backend.ir.Node> child(
        initial: T,
        setterCallback: ((T) -> Unit)? = null,
    ): ReadWriteProperty<net.ormr.jukkas.backend.ir.Node, T> =
        net.ormr.jukkas.backend.ir.AbstractNode.ChildProperty(this, initial, setterCallback)

    @JvmName("nullableChild")
    protected fun <T : net.ormr.jukkas.backend.ir.Node> child(
        initial: T?,
        setterCallback: ((T?) -> Unit)? = null,
    ): ReadWriteProperty<net.ormr.jukkas.backend.ir.Node, T?> =
        net.ormr.jukkas.backend.ir.AbstractNode.NullableChildProperty(this, initial, setterCallback)

    private class ChildProperty<T : net.ormr.jukkas.backend.ir.Node>(
        parent: net.ormr.jukkas.backend.ir.Node,
        private var child: T,
        private val setterCallback: ((T) -> Unit)?,
    ) : ReadWriteProperty<net.ormr.jukkas.backend.ir.Node, T> {
        init {
            val child = child
            setterCallback?.invoke(child)
            parent.adopt(child)
        }

        override fun getValue(thisRef: net.ormr.jukkas.backend.ir.Node, property: KProperty<*>): T = child

        override fun setValue(
            thisRef: net.ormr.jukkas.backend.ir.Node,
            property: KProperty<*>,
            value: T,
        ) {
            setterCallback?.invoke(value)
            child = thisRef.adopt(value)
        }
    }

    private class NullableChildProperty<T : net.ormr.jukkas.backend.ir.Node>(
        parent: net.ormr.jukkas.backend.ir.Node,
        private var child: T?,
        private val setterCallback: ((T?) -> Unit)?,
    ) : ReadWriteProperty<net.ormr.jukkas.backend.ir.Node, T?> {
        init {
            val child = child
            setterCallback?.invoke(child)
            if (child != null) parent.adopt(child)
        }

        override fun getValue(thisRef: net.ormr.jukkas.backend.ir.Node, property: KProperty<*>): T? = child

        override fun setValue(
            thisRef: net.ormr.jukkas.backend.ir.Node,
            property: KProperty<*>,
            value: T?,
        ) {
            setterCallback?.invoke(value)
            child = value?.let(thisRef::adopt)
        }
    }
}