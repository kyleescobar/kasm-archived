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

package org.spectralpowered.kasm.commons.propertymixin

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class KotlinWeakKeyedReference<K, V>(
    key: K,
    override val map: MutableMap<WeakKeyedReference<K, V>, V>
) : KotlinWeakReference<K>(key), WeakKeyedReference<K, V>  {

    /*
     * Use a global scoped coroutine to clear the queue of the reference.
     * This fixes an issue where the JVM fails to garbage collect stored references
     * after they are no longer needed.
     */
    init {
        GlobalScope.launch {
            @Suppress("BlockingMethodInNonBlockingContext")
            queue.remove()
            map.remove(this@KotlinWeakKeyedReference)
        }
    }
}