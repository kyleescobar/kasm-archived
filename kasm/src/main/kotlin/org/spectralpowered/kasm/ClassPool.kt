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

import org.objectweb.asm.tree.ClassNode
import org.spectralpowered.kasm.classloader.KasmClassLoader
import java.net.URLClassLoader

class ClassPool {

    private val classMap = mutableMapOf<String, ClassNode>()
    private var hasCalculatedRefs = false

    val size: Int get() = classMap.size

    fun contains(node: ClassNode): Boolean {
        return classMap[node.name] != null
    }

    fun addClass(node: ClassNode) {
        node.init(this)
        classMap[node.name] = node
    }

    fun removeClass(node: ClassNode) {
        classMap.remove(node.name)
    }

    fun findClass(name: String): ClassNode? = classMap[name]

    /**
     * Extracts extra reference information after loading all classes in the pool.
     */
    fun calculateRefs() {
        if(hasCalculatedRefs) {
            return
        }


    }

    fun reinitialize() {
        val copy = mutableListOf<ClassNode>()
        copy.addAll(classMap.values)

        classMap.clear()
        copy.forEach { node ->
            this.addClass(node)
        }
    }

    fun toList(): List<ClassNode> = this.classMap.values.toList()

    fun forEach(action: (ClassNode) -> Unit) = this.toList().forEach(action)

    fun first(predicate: (ClassNode) -> Boolean) = this.toList().first(predicate)

    fun firstOrNull(predicate: (ClassNode) -> Boolean) = this.toList().firstOrNull(predicate)

    fun <T> map(transform: (ClassNode) -> T) = this.toList().map(transform)

    fun createClassloader(): KasmClassLoader {
        val classloader = KasmClassLoader(ClassLoader.getSystemClassLoader())

        this.forEach { cls ->
            classloader.addClass(cls)
        }

        return classloader
    }
}