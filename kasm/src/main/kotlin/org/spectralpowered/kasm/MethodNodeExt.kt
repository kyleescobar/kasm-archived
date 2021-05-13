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
import org.objectweb.asm.tree.MethodNode
import org.spectralpowered.kasm.commons.propertymixin.mixin

var MethodNode.owner: ClassNode by mixin()
    internal set

val MethodNode.pool: ClassPool get() = this.owner.pool

val MethodNode.type: Type get() = Type.getObjectType(this.desc)

val MethodNode.identifier: String get() = "${this.owner}.${this.name}${this.desc}"

internal fun MethodNode.init(owner: ClassNode) {
    this.owner = owner
}