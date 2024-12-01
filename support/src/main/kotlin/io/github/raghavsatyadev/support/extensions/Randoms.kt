package io.github.raghavsatyadev.support.extensions

import kotlin.random.Random

@Suppress("unused")
object Randoms {

    /**
     * Random int starting from initial value to [Int.MAX_VALUE]
     *
     * @param initialValue default 1, if you want to start from 0 then pass 0
     * @return random int
     */
    fun randomInt(initialValue: Int = 1): Int {
        return Random.nextInt(initialValue, Int.MAX_VALUE)
    }

    /**
     * Random long starting from initial value to [Long.MAX_VALUE]
     *
     * @param initialValue default 1, if you want to start from 0 then pass 0
     * @return random long
     */
    fun randomLong(initialValue: Long = 1): Long {
        return Random.nextLong(initialValue, Long.MAX_VALUE)
    }

    fun <T> selectRandomElement(vararg elements: T): T {
        return elements[Random.nextInt(0, elements.size)]
    }
}