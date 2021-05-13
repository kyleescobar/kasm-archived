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

package org.spectralpowered.kasm.executor

import me.coley.analysis.value.AbstractValue
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.analysis.Frame

class ExecutionResult(val execution: Execution, val method: MethodNode) {

    val frames = mutableMapOf<AbstractInsnNode, Frame<AbstractValue>>()

    fun getPushed(insn: AbstractInsnNode): List<AbstractValue> {
        if(insn.next == null) {
            return emptyList()
        }

        val currentFrame = frames[insn]!!
        val proceedingFrame = frames[insn.next]!!

        val currentStack = mutableListOf<AbstractValue>().apply {
            repeat(currentFrame.stackSize) {
                if(currentFrame.getStack(it) != null) {
                    this.add(currentFrame.getStack(it))
                }
            }
        }

        val proceedingStack = mutableListOf<AbstractValue>().apply {
            repeat(proceedingFrame.stackSize) {
                if(proceedingFrame.getStack(it) != null) {
                    this.add(proceedingFrame.getStack(it))
                }
            }
        }

        return proceedingStack.minus(currentStack)
    }

    fun resolveOrigin(value: AbstractValue): AbstractInsnNode {
        return value.insns.first()
    }
}