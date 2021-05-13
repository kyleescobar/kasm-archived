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

package org.spectralpowered.kasm.util

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import org.spectralpowered.kasm.toByteCode
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

object JarUtil {

    fun readJar(path: String): Collection<ClassNode> {
        val nodes = mutableListOf<ClassNode>()
        val file = File(path)

        if(!file.exists()) {
            throw FileNotFoundException("Could not find file: $path")
        }

        if(file.extension != "jar") {
            throw IllegalArgumentException("The provided path is not a jar file. $path")
        }

        JarFile(file).use { jar ->
            jar.entries().asSequence()
                .filter { it.name.endsWith(".class") }
                .forEach { entry ->
                    val node = ClassNode()
                    val reader = ClassReader(jar.getInputStream(entry))
                    reader.accept(node, ClassReader.SKIP_FRAMES)
                    nodes.add(node)
                }
        }

        return nodes
    }

    fun writeJar(path: String, nodes: Collection<ClassNode>) {
        val file = File(path)

        if(file.extension != "jar") {
            throw IllegalArgumentException("The provided path is not a jar file. $path")
        }

        if(file.exists()) {
            file.delete()
        }

        val jos = JarOutputStream(FileOutputStream(file))
        nodes.forEach { node ->
            jos.putNextEntry(JarEntry(node.name + ".class"))
            jos.write(node.toByteCode())
            jos.closeEntry()
        }

        jos.close()
    }
}