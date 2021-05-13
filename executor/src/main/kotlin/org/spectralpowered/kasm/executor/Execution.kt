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

import me.coley.analysis.*
import me.coley.analysis.util.InheritanceGraph
import me.coley.analysis.util.TypeUtil
import org.objectweb.asm.Type
import org.objectweb.asm.tree.MethodNode
import org.spectralpowered.kasm.ClassPool
import org.spectralpowered.kasm.owner
import org.spectralpowered.kasm.toByteCode

class Execution(val pool: ClassPool, private val optionsBlock: (Options.() -> Unit)? = null) {

    val inheritanceGraph = InheritanceGraph()

    private val options = Options()
    private lateinit var typeResolver: TypeResolver
    private lateinit var analyzer: SimAnalyzer

    init {
        this.init()
    }

    private fun init() {
        optionsBlock?.invoke(options)
        this.buildInheritanceGraph()
        this.buildTypeResolver()
        this.buildAnalyzer()
    }

    private fun buildInheritanceGraph() {
        pool.forEach { cls ->
            inheritanceGraph.addClass(cls.toByteCode())
        }

        pool.forEach { cls ->
            val parents = mutableListOf<String>()
            if(cls.superName != null) {
                parents.add(cls.superName)
            }

            parents.addAll(cls.interfaces)

            /*
             * Add the calculated parents to the inheritance graph
             */
            inheritanceGraph.add(cls.name, parents)
        }
    }

    private fun buildTypeResolver() {
        typeResolver = object : TypeResolver {

            override fun common(type1: Type, type2: Type): Type {
                val common = inheritanceGraph.getCommon(type1.internalName, type2.internalName)
                if(common != null) {
                    return Type.getObjectType(common)
                }

                return TypeUtil.OBJECT_TYPE
            }

            override fun commonException(type1: Type, type2: Type): Type {
                val common = inheritanceGraph.getCommon(type1.internalName, type2.internalName)
                if(common != null) {
                    return Type.getObjectType(common)
                }

                return TypeUtil.EXCEPTION_TYPE
            }
        }
    }

    private fun buildAnalyzer() {
        analyzer = object : SimAnalyzer(SimInterpreter()) {

            override fun createTypeChecker(): TypeChecker {
                return TypeChecker { parent, child -> inheritanceGraph.getAllParents(child.internalName).contains(parent.internalName) }
            }

            override fun createTypeResolver(): TypeResolver {
                return typeResolver
            }
        }

        analyzer.setSkipDeadCodeBlocks(options.skipDeadCode)
        analyzer.setThrowUnresolvedAnalyzerErrors(options.ignoreTypeResolutionErrors)
    }

    fun execute(method: MethodNode): ExecutionResult {
        val result = ExecutionResult(this, method)

        analyzer.analyze(method.owner.name, method).forEachIndexed { index, frame ->
            result.frames[method.instructions[index]] = frame as SimFrame
        }

        return result
    }

    class Options {
        var skipDeadCode = true
        var ignoreTypeResolutionErrors = true
    }
}