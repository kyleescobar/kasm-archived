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

package org.spectralpowered.kasm.classloader

import org.objectweb.asm.tree.ClassNode
import org.spectralpowered.kasm.toByteCode

class KasmClassLoader(parent: ClassLoader) : ClassLoader(parent) {

    private val classData = mutableMapOf<String, ByteArray>()

    fun addClass(node: ClassNode) {
        if(classData.containsKey(node.name.replace("/", "."))) {
            throw IllegalArgumentException("A class with the name: ${node.name} already exists in the classpath.")
        }

        classData[node.name.replace("/", ".")] = node.toByteCode()
    }

    fun removeClass(node: ClassNode) {
        classData.remove(node.name.replace("/", "."))
    }

    override fun findClass(name: String): Class<*> {
        val bytes = classData[name]
        if(bytes != null) {
            return defineClass(name, bytes, 0, bytes.size)
        }

        return super.findClass(name)
    }
}