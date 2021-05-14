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

abstract class IterableStep<T : AbstractInsnNode> {

    var captured: T? = null
        private set

    fun reset() {
        captured = null
    }

    @Suppress("UNCHECKED_CAST")
    fun match(insn: AbstractInsnNode): Boolean {
        if(tryMatch(insn)) {
            captured = insn as T
            return true
        }

        return false
    }

    abstract fun tryMatch(insn: AbstractInsnNode): Boolean

    override fun toString(): String {
        return "IterableStep[captured=$captured]"
    }
}