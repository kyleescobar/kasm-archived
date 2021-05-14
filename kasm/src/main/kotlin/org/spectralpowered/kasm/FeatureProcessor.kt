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
import org.objectweb.asm.tree.MethodNode

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

    }
}