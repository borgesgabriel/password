package es.gabrielborg.keystroke.utility.polynomial

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.math.BigInteger

import org.apache.commons.math3.analysis.differentiation.DerivativeStructure
import org.apache.commons.math3.analysis.differentiation.UnivariateDifferentiableFunction
import org.apache.commons.math3.exception.DimensionMismatchException
import org.apache.commons.math3.exception.NoDataException
import org.apache.commons.math3.exception.NullArgumentException

/**
 * Modification to handle BigIntegers and adapted to Kotlin
 *
 * Implements the representation of a real polynomial function in
 * Newton Form. For reference, see **Elementary Numerical Analysis**,
 * ISBN 0070124477, chapter 2.
 *
 *
 * The formula of polynomial in Newton form is
 * p(x) = a[0] + a[1](x-c[0]) + a[2](x-c[0])(x-c[1]) + ... +
 * a[n](x-c[0])(x-c[1])...(x-c[n-1])
 * Note that the length of a[] is one more than the length of c[]

 * @since 1.2
 */
class BigIntegerPolynomialFunctionNewtonForm
/**
 * Construct a Newton polynomial with the given a[] and c[]. The order of
 * centers are important in that if c[] shuffle, then values of a[] would
 * completely change, not just a permutation of old a[].
 *
 *
 * The constructor makes copy of the input arrays and assigns them.

 * @param a Coefficients in Newton form formula.
 * *
 * @param c Centers.
 * *
 * @throws NullArgumentException      if any argument is `null`.
 * *
 * @throws NoDataException            if any array has zero length.
 * *
 * @throws DimensionMismatchException if the size difference between
 * *                                    `a` and `c` is not equal to 1.
 */
@Throws(NullArgumentException::class, NoDataException::class, DimensionMismatchException::class)
constructor(a: Array<BigInteger>, c: DoubleArray) : UnivariateDifferentiableFunction {

    /**
     * Centers of the Newton polynomial.
     */
    private val c: DoubleArray

    private val a: Array<BigInteger>

    init {

        this.a = Array(a.size) { BigInteger.ZERO }
        this.c = DoubleArray(c.size)
        System.arraycopy(a, 0, this.a, 0, a.size)
        System.arraycopy(c, 0, this.c, 0, c.size)
    }

    /**
     * Calculate the function value at the given point.

     * @param z Point at which the function value is to be computed.
     * *
     * @return the function value.
     * * Should throw OperationNotSupportedException
     */
    override fun value(z: Double): Double {
        return java.lang.Double.MIN_VALUE
    }

    fun bigIntegerValue(z: Double): BigInteger {
        return evaluate(a, c, z)
    }

    /**
     * {@inheritDoc}

     * @since 3.1
     */
    override fun value(t: DerivativeStructure): DerivativeStructure? {
        return null
    }

    companion object {

        /**
         * Evaluate the Newton polynomial using nested multiplication. It is
         * also called [Horner's Rule](http://mathworld.wolfram.com/HornersRule.html) and takes O(N) time.
         * @param a Coefficients in Newton form formula.
         * @param c Centers.
         * @param z Point at which the function value is to be computed.
         * @return the function value.
         */
        fun evaluate(a: Array<BigInteger>, c: DoubleArray, z: Double): BigInteger {
            val n = c.size
            var value = a[n]
            for (i in n-1 downTo 0) {
                value = a[i].add(BigInteger.valueOf((z - c[i]).toLong()).multiply(value))
            }

            return value
        }
    }

}