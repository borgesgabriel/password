package es.gabrielborg.keystroke.utility

import java.math.BigInteger

/**
 * Represents a point in a polynomial: (x, f(x))
 */
data class Point(val x: Int = 0, val y: BigInteger = BigInteger.ZERO)
