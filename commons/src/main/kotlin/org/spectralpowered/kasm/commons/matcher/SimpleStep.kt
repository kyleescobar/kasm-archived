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

package org.spectralpowered.kasm.commons.matcher

import org.objectweb.asm.tree.AbstractInsnNode

/**
 * Represents a simple opcode integer instruction matching step.
 *
 * @property opcodes IntArray
 * @constructor
 */
class SimpleStep(vararg val opcodes: Int) : IterableStep<AbstractInsnNode>() {

    /**
     * Trys to match the provided instruction with any conditions of this step.
     *
     * @param insn AbstractInsnNode
     * @return Boolean
     */
    override fun tryMatch(insn: AbstractInsnNode): Boolean {
        val opcode = insn.opcode
        opcodes.forEach { o ->
            if(opcode == o) {
                return true
            }
        }
        return false
    }

    override fun toString(): String {
        return "SimpleStep[captured=$captured, opcodes=$opcodes]"
    }

    companion object {

        /**
         * Defines an inclusive opcode range as a simple step.
         *
         * @param start Int
         * @param end Int
         * @return SimpleStep
         */
        fun withRange(start: Int, end: Int): SimpleStep {
            assert(start < end) { "Starting opcode must be smaller than the ending opcode." }
            val opcodes = IntArray(end - start + 1)
            var increment = 0
            for(i in 0..end) {
                opcodes[increment++] = i
            }

            return SimpleStep(*opcodes)
        }

        /**
         * Defines an inclusive opcode range as a simple step.
         *
         * @param start Int
         * @param end Int
         * @param additional IntArray
         * @return SimpleStep
         */
        fun withRange(start: Int, end: Int, additional: IntArray): SimpleStep {
            assert(start < end) { "Starting opcode must be smaller than the ending opcode." }
            val opcodes = IntArray(end - start + 1 + additional.size)
            var increment = 0
            for(i in 0..end) {
                opcodes[increment++] = i
            }

            for(i in additional) {
                opcodes[increment++] = i
            }

            return SimpleStep(*opcodes)
        }
    }
}