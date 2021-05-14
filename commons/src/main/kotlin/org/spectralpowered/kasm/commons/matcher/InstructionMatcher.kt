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
import org.objectweb.asm.tree.InsnList
import java.util.*

/**
 * Represents a instruction matcher which can match bytecode instruction patterns through various step
 * conditions while iterating through instructions.
 *
 * @constructor Create empty instruction matcher instance.
 */
class InstructionMatcher() {

    /**
     * @param init [@kotlin.ExtensionFunctionType] Function1<InstructionMatcher, Unit>
     * @constructor Create a new instruction matcher with a builder DSL initialization for inline setup.
     */
    constructor(init: InstructionMatcher.() -> Unit) : this() {
        this.init()
    }

    /**
     * The steps which determine matching conditions at each iterating instruction.
     */
    private val steps = mutableListOf<IterableStep<*>>()

    /**
     * The instruction replacement if any should a step condition be met.
     */
    private val replacements = mutableMapOf<IterableStep<*>, Optional<AbstractInsnNode>>()

    /**
     * A all cases instruction list which will be added no mater the matching result.
     */
    private val endingInserts = MutableList<AbstractInsnNode?>(4) { null }

    /**
     * Whether this instruction matcher has already attempted a match iteration.
     */
    private var hasMatched: Boolean = false

    /**
     * Whether this instruction matcher has already attempted a match and replaced instructions
     * at matched step conditions.
     */
    private var hasReplaced: Boolean = false

    /**
     * Adds a step condition at the
     * @return T
     */
    operator fun <K : AbstractInsnNode, T : IterableStep<K>> T.unaryPlus() : T {
        assert(!hasMatched) { "Reset before adding steps to a pre-matched instance." }
        assert(!hasReplaced) { "Reset before adding steps to a pre-matched and replaced instance." }

        steps.forEach { s ->
            if(s == this) {
                throw IllegalArgumentException("Instruction matching step: $this has already been added.")
            }
        }

        steps.add(this)
        return this
    }

    fun match(it: ListIterator<AbstractInsnNode>): Boolean {
        if(steps.isEmpty()) {
            throw IllegalStateException("Instruction matching requires at least 1 step.")
        }

        while(it.hasNext()) {
            if(iteratingMatch(it)) {
                return true
            }
        }
        return false
    }

    fun matchFirst(it: ListIterator<AbstractInsnNode>): Boolean {
        if(steps.isEmpty()) {
            throw IllegalStateException("Instruction matching requires at least 1 step.")
        }

        if(!it.hasNext()) {
            return false
        }

        return iteratingMatch(it)
    }

    private fun iteratingMatch(it: ListIterator<AbstractInsnNode>): Boolean {
        val size = steps.size
        var stepCounter = 0
        var next: AbstractInsnNode? = it.next()
        while(next != null) {
            val step = steps[stepCounter]
            if(!step.match(next)) {
                break
            }
            stepCounter++

            if(stepCounter == size) {
                hasMatched = true
                return true
            }
            next = next.next
        }

        return false
    }

    fun addReplacement(step: IterableStep<*>, replacement: AbstractInsnNode?) {
        assert(steps.contains(step)) { "Unable to add replacement for step: $step" }
        assert(hasMatched) { "Cannot add replacement before matching." }
        replacements[step] = Optional.ofNullable(replacement)
    }

    fun addRemoval(step: IterableStep<*>) {
        addReplacement(step, null)
    }

    fun addEndingInsert(step: AbstractInsnNode) {
        endingInserts.add(step)
    }

    fun setRemoveAll() {
        steps.forEach { addRemoval(it) }
    }

    fun setRemoval(stepIndex: Int) {
        addRemoval(steps[stepIndex])
    }

    fun setReplacement(stepIndex: Int, replacement: AbstractInsnNode) {
        addReplacement(steps[stepIndex], replacement)
    }

    fun replace(it: MutableListIterator<AbstractInsnNode>): Boolean {
        it.previous()
        var isDirty = false
        steps.forEach { step ->
            val next = it.next()
            assert(step.captured == next) { "Iteration exception. Expected ${step.captured} but received $next" }

            val replacement = replacements[step]
            if(replacement != null) {
                if(replacement.isPresent) {
                    it.set(replacement.get())
                } else {
                    it.remove()
                }

                isDirty = true
            }
        }

        if(endingInserts.isNotEmpty()) {
            endingInserts.forEach { endingInsert ->
                it.add(endingInsert!!)
            }

            return true
        }

        return isDirty
    }

    fun reset() {
        steps.forEach { step ->
            step.reset()
        }

        hasMatched = false
        hasReplaced = false
    }

    companion object {

        fun removeAll(matcher: InstructionMatcher, insns: InsnList): Int {
            var counter = 0
            val it = insns.iterator()

            while(it.hasNext()) {
                if(matcher.match(it)) {
                    matcher.setRemoveAll()
                    if(!matcher.replace(it)) {
                        throw IllegalStateException("Failed to find any replacements.")
                    }
                    matcher.reset()
                    ++counter
                }
            }

            return counter
        }
    }
}