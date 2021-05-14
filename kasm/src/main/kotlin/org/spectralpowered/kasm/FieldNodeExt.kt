/*
 * Copyright (C) 2021 Spectral Powered <Kyle Escobar>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.spectralpowered.kasm

import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.MethodNode
import org.spectralpowered.kasm.commons.propertymixin.mixin
import java.lang.reflect.Modifier

var FieldNode.owner: ClassNode by mixin()
    internal set

val FieldNode.pool: ClassPool get() = this.owner.pool
val FieldNode.type: Type get() = Type.getType(this.desc)
val FieldNode.identifier: String get() = "${this.owner}.${this.name}"

val FieldNode.isPublic: Boolean get() = Modifier.isPublic(this.access)
val FieldNode.isPrivate: Boolean get() = Modifier.isPrivate(this.access)
val FieldNode.isFinal: Boolean get() = Modifier.isFinal(this.access)
val FieldNode.isStatic: Boolean get() = Modifier.isStatic(this.access)

val FieldNode.readRefs: MutableList<MethodNode> by mixin()
val FieldNode.writeRefs: MutableList<MethodNode> by mixin()

internal fun FieldNode.init(owner: ClassNode) {
    this.owner = owner
}