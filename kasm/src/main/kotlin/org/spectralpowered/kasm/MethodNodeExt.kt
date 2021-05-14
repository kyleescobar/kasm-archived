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

var MethodNode.owner: ClassNode by mixin()
    internal set

val MethodNode.pool: ClassPool get() = this.owner.pool
val MethodNode.type: Type get() = Type.getObjectType(this.desc)
val MethodNode.identifier: String get() = "${this.owner}.${this.name}${this.desc}"

val MethodNode.returnType: Type get() = this.type.returnType
val MethodNode.argumentTypes: List<Type> get() = this.type.argumentTypes.toList()
val MethodNode.isPublic: Boolean get() = Modifier.isPublic(this.access)
val MethodNode.isPrivate: Boolean get() = Modifier.isPrivate(this.access)
val MethodNode.isFinal: Boolean get() = Modifier.isFinal(this.access)
val MethodNode.isAbstract: Boolean get() = Modifier.isAbstract(this.access)
val MethodNode.isStatic: Boolean get() = Modifier.isStatic(this.access)

val MethodNode.refsIn: MutableList<MethodNode> by mixin()
val MethodNode.refsOut: MutableList<MethodNode> by mixin()
val MethodNode.fieldReadRefs: MutableList<FieldNode> by mixin()
val MethodNode.fieldWriteRefs: MutableList<FieldNode> by mixin()
val MethodNode.classRefs: MutableList<ClassNode> by mixin()

internal fun MethodNode.init(owner: ClassNode) {
    this.owner = owner
}