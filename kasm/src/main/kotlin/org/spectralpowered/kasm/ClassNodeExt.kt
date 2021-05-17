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

import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.MethodNode
import org.spectralpowered.kasm.commons.propertymixin.mixin
import org.spectralpowered.kasm.commons.propertymixin.nullableMixin
import java.lang.reflect.Modifier
import java.util.ArrayDeque

var ClassNode.pool: ClassPool by mixin()
    internal set

val ClassNode.type: Type get() = Type.getObjectType(this.name)
val ClassNode.identifier: String get() = this.name

val ClassNode.isStatic: Boolean get() = Modifier.isStatic(this.access)
val ClassNode.isAbstract: Boolean get() = Modifier.isAbstract(this.access)
val ClassNode.isInterface: Boolean get() = Modifier.isInterface(this.access)

var ClassNode.parent: ClassNode? by nullableMixin()
val ClassNode.children: MutableList<ClassNode> by mixin(mutableListOf())
val ClassNode.interfaceClasses: MutableList<ClassNode> by mixin(mutableListOf())
val ClassNode.implementers: MutableList<ClassNode> by mixin(mutableListOf())
val ClassNode.methodTypeRefs: MutableList<MethodNode> by mixin(mutableListOf())
val ClassNode.fieldTypeRefs: MutableList<FieldNode> by mixin(mutableListOf())
val ClassNode.strings: MutableList<String> by mixin(mutableListOf())


internal fun ClassNode.init(pool: ClassPool) {
    this.pool = pool
    this.methods.forEach { it.init(this) }
    this.fields.forEach { it.init(this) }
}

fun ClassNode.findMethod(name: String, desc: String): MethodNode? {
    return this.methods.firstOrNull { it.name == name && it.desc == desc }
}

fun ClassNode.findField(name: String, desc: String): FieldNode? {
    return this.fields.firstOrNull { it.name == name && it.desc == desc }
}

fun ClassNode.toByteCode(): ByteArray {
    val writer = ClassWriter(ClassWriter.COMPUTE_MAXS)
    this.accept(writer)

    return writer.toByteArray()
}

/**
 * Resolves a field from the class and respects inheritance order defines by the JVM specifications
 *
 * @receiver ClassNode
 * @param name String
 * @param desc String
 * @return FieldNode?
 */
fun ClassNode.resolveField(name: String, desc: String): FieldNode? {
    var ret = findField(name, desc)
    if(ret != null) return ret

    if(interfaceClasses.isNotEmpty()) {
        val queue = ArrayDeque<ClassNode>()
        queue.addAll(interfaceClasses)

        var cls = queue.pollFirst()
        while(cls != null) {
            ret = cls.findField(name, desc)
            if(ret != null) return ret

            cls.interfaceClasses.forEach {
                queue.addFirst(it)
            }

            cls = queue.pollFirst()
        }
    }

    var cls = parent
    while(cls != null) {
        ret = cls.findField(name, desc)
        if(ret != null) return ret

        cls = cls.parent
    }

    return null
}

/**
 * Resolves a method from a class respecting the JVM method resolution specifications.
 *
 * @receiver ClassNode
 * @param name String
 * @param desc String
 * @param toInterface Boolean
 * @return MethodNode?
 */
fun ClassNode.resolveMethod(name: String, desc: String, toInterface: Boolean): MethodNode? {
    if (!toInterface) {
        var ret = findMethod(name, desc)
        if (ret != null) return ret

        var cls = this.parent
        while (cls != null) {
            ret = cls.findMethod(name, desc)
            if (ret != null) return ret

            cls = cls.parent
        }
        return resolveInterfaceMethod(name, desc)
    } else {
        var ret = findMethod(name, desc)
        if (ret != null) return ret

        if (parent != null) {
            ret = parent!!.findMethod(name, desc)
            if (ret != null && (ret.access and (ACC_PUBLIC or ACC_STATIC)) == ACC_PUBLIC) return ret
        }
        return resolveInterfaceMethod(name, desc)
    }
}

private fun ClassNode.resolveInterfaceMethod(name: String, desc: String): MethodNode? {
    val queue = ArrayDeque<ClassNode>()
    val queued = mutableSetOf<ClassNode>()
    var cls = this.parent

    while(cls != null) {
        cls.interfaceClasses.forEach { interf ->
            if(queued.add(interf)) queue.add(interf)
        }
        cls = cls.parent
    }

    if(queue.isEmpty()) return null

    val matches = mutableSetOf<MethodNode>()
    var foundNonAbstract = false

    cls = queue.poll()
    while(cls != null) {
        var ret = cls.findMethod(name, desc)
        if(ret != null && (ret.access and (ACC_PRIVATE or ACC_STATIC)) == 0) {
            matches.add(ret)
            if((ret.access and ACC_ABSTRACT) == 0) {
                foundNonAbstract = true
            }
        }

        cls.interfaceClasses.forEach { interf ->
            if(queued.add(interf)) queue.add(interf)
        }
    }

    if(matches.isEmpty()) return null
    if(matches.size == 1) return matches.iterator().next()

    if(foundNonAbstract) {
        val it = matches.iterator()
        while(it.hasNext()) {
            val m = it.next()
            if((m.access and ACC_ABSTRACT) != 0) {
                it.remove()
            }
        }

        if(matches.size == 1) return matches.iterator().next()
    }

    val it = matches.iterator()
    while(it.hasNext()) {
        val m = it.next()

        cmpLoop@ for(m2 in matches) {
            if(m2 == m) continue

            if(m2.owner.interfaceClasses.contains(m.owner)) {
                it.remove()
                break
            }

            queue.addAll(m2.owner.interfaceClasses)

            cls = queue.poll()
            while(cls != null) {
                if(cls.interfaceClasses.contains(m.owner)) {
                    it.remove()
                    queue.clear()
                    break@cmpLoop
                }

                queue.addAll(cls.interfaceClasses)
            }
        }
    }

    return matches.iterator().next()
}