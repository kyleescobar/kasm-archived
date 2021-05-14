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

import org.objectweb.asm.Opcodes.GETFIELD
import org.objectweb.asm.Opcodes.GETSTATIC
import org.objectweb.asm.tree.*

internal object FeatureProcessor {

    fun processRefs(pool: ClassPool) {
        /*
         * Loop pass - A
         */
        pool.forEach { cls ->
            processA(cls)
        }

        /*
         * Loop pass - B
         */
        pool.forEach { cls ->
            processB(cls)
        }
    }
    
    private fun processA(cls: ClassNode) {
        if(cls.superName != null) {
            val parent = cls.pool.findClass(cls.superName)
            if(parent != null) {
                cls.parent = parent
                parent.children.add(cls)
            }
        }
        
        val interfs = cls.interfaces.mapNotNull { cls.pool.findClass(it) }
        interfs.forEach { interf ->
            if(cls.interfaceClasses.add(interf)) interf.implementers.add(cls)
        }
    }

    private fun processB(cls: ClassNode) {
        cls.methods.forEach { method ->
            processMethodInsns(method)
        }
    }

    private fun processMethodInsns(method: MethodNode) {
        val it = method.instructions.iterator()
        while(it.hasNext()) {
            /*
             * Handle specific instruction types
             */
            when(val insn = it.next()) {
                /*
                 * Method Invocation Type
                 */
                is MethodInsnNode -> {
                    val owner = method.pool.findClass(insn.owner) ?: continue
                    val dst = owner.resolveMethod(insn.name, insn.desc, insn.itf) ?: continue
                    dst.refsIn.add(method)
                    method.refsOut.add(dst)
                    dst.owner.methodTypeRefs.add(method)
                    method.classRefs.add(dst.owner)
                }

                /*
                 * Field Read / Write
                 */
                is FieldInsnNode -> {
                    val owner = method.pool.findClass(insn.owner) ?: continue
                    val dst = owner.resolveField(insn.name, insn.desc) ?: continue

                    if(insn.opcode == GETSTATIC || insn.opcode == GETFIELD) {
                        dst.readRefs.add(method)
                        method.fieldReadRefs.add(dst)
                    } else {
                        dst.writeRefs.add(method)
                        method.fieldWriteRefs.add(dst)
                    }

                    dst.owner.methodTypeRefs.add(method)
                    method.classRefs.add(dst.owner)
                }

                /*
                 * Type reference
                 */
                is TypeInsnNode -> {
                    val dst = method.pool.findClass(insn.desc) ?: continue
                    dst.methodTypeRefs.add(method)
                    method.classRefs.add(dst)
                }
            }
        }
    }
}