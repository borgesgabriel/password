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
package es.gabrielborg.keystroke.utility.polynomial

import java.io.Serializable
import java.math.BigInteger

import org.apache.commons.math3.analysis.UnivariateFunction
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator
import org.apache.commons.math3.exception.MathIllegalArgumentException

import es.gabrielborg.keystroke.utility.Point

/**
 * Modification to handle BigIntegers and adapted to Kotlin
 *
 * Implements the [Divided Difference Algorithm]
 * (http://mathworld.wolfram.com/NewtonsDividedDifferenceInterpolationFormula.html) for interpolation of real univariate
 * functions. For reference, see **Introduction to Numerical Analysis**,
 * ISBN 038795452X, chapter 2.
 *
 * The actual code of Neville's evaluation is in PolynomialFunctionLagrangeForm,
 * this class provides an easy-to-use interface to it.
 *
 * @since 1.2
 */
class BigIntegerDividedDifferenceInterpolator : UnivariateInterpolator, Serializable {

    /**
     * Compute an interpolating function for the dataset.
     *
     * @param polynomial the polynomial to be interpolated
     * @return a function which interpolates the dataset.
     */
    fun interpolate(polynomial: Array<Point>): BigIntegerPolynomialFunctionNewtonForm {
        val x = DoubleArray(polynomial.size)
        val y = Array(polynomial.size) { BigInteger.ZERO }
        for (i in polynomial.indices) {
            x[i] = polynomial[i].x.toDouble()
            y[i] = polynomial[i].y
        }
        val c = DoubleArray(x.size - 1)
        System.arraycopy(x, 0, c, 0, c.size)

        val a = computeDividedDifference(x, y)
        return BigIntegerPolynomialFunctionNewtonForm(a, c)
    }

    @Throws(MathIllegalArgumentException::class)
    override fun interpolate(arg0: DoubleArray, arg1: DoubleArray): UnivariateFunction? {
        return null
    }

    companion object {
        /**
         * Return a copy of the divided difference array.
         *
         * The divided difference array is defined recursively by
         * f\[x0] = f(x0)
         * f[x0,x1,...,xk] = (f[x1,...,xk] - f[x0,...,x[k-1]]) / (xk - x0)
         *
         * The computational complexity is O(N^2).
         *
         * @param x Interpolating points array.
         * @param y Interpolating values array.
         * @return a fresh copy of the divided difference array.
         */
        fun computeDividedDifference(x: DoubleArray, y: Array<BigInteger>): Array<BigInteger> {

            val dividedDifference = y.clone()

            val n = x.size
            val a = Array(n) { BigInteger.ZERO }
            a[0] = dividedDifference[0]
            for (i in 1..n - 1) {
                for (j in 0..n - i - 1) {
                    val denominator = BigInteger.valueOf((x[j + i] - x[j]).toLong())
                    dividedDifference[j] = (dividedDifference[j + 1].subtract(dividedDifference[j])).divide(denominator)
                }
                a[i] = dividedDifference[0]
            }

            return a
        }
    }
}