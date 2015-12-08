package es.gabrielborg.keystroke.core

import es.gabrielborg.keystroke.utility.*
import es.gabrielborg.keystroke.utility.polynomial.*
import org.apache.commons.math3.stat.StatUtils

import java.math.BigInteger
import java.util.*
import kotlin.math.div
import kotlin.math.plus

object LoginService {
    fun login(username: String, password: String, featureArray: DoubleArray, verbose: Boolean = false): Boolean {
        val instructionTable = try {
            IO.readInstructionTable(username)
        } catch (e: Exception) {
            if (kDebug)
                throw e
            return false
        }

        val nodeList = Array(featureArray.size) {
            if (featureArray[it] < IO.threshold[it]) {
                Point(2 * it, instructionTable[it].alpha / (((password + 2 * it).toSha256BigInteger()) % kLargePrime))
            } else {
                Point(2 * it + 1, instructionTable[it].beta / (((password + (2 * it + 1)).toSha256BigInteger()) % kLargePrime))
            }
        }

        val polynomial = try {
            BigIntegerDividedDifferenceInterpolator().interpolate(nodeList)
        } catch (e: Exception) {
            return false
        }
        val hpwd = try {
            polynomial.bigIntegerValue(0.0)
        } catch (e: Exception) {
            return false
        }
        if (verbose && kDebug)
            println("Hpwd': $hpwd")

        var featureTable = IO.readHistoryFile(username, hpwd, verbose) ?: return false

        featureTable.add(featureArray)
        featureTable = ArrayList(featureTable.takeLast(kHValue)) // Take only the last h elements

        IO.writeHistoryFile(username, hpwd, featureTable)

        val mean = DoubleArray(kNumberOfFeatures)
        val stdDeviation = DoubleArray(kNumberOfFeatures)
        for (i in 0..kNumberOfFeatures - 1) {
            val columnArray = featureTable.map { it[i].toDouble() }.toDoubleArray()
            mean[i] = StatUtils.mean(columnArray)
            stdDeviation[i] = Math.sqrt(StatUtils.variance(columnArray))
        }

        val randomPolynomial = getRandomPolynomial()
        randomPolynomial[0] = hpwd

        val newInstructionTable = Array(kNumberOfFeatures) {
            val y0: BigInteger
            val y1: BigInteger
            if (isDistinguishingFeature(featureTable.size, mean[it], stdDeviation[it], IO.threshold[it])) {
                DataSet.logDistinguishingFeature(it, true)
                if (mean[it] < IO.threshold[it]) {
                    // y0 = f(2i), y1 != f(2i+1)
                    y0 = randomPolynomial.at(2 * it)
                    y1 = getLowRandomBigInteger() + randomPolynomial.at(2 * it + 1)
                } else {
                    // y0 != f(2i), y1 = f(2i+1)
                    y0 = getLowRandomBigInteger() + randomPolynomial.at(2 * it)
                    y1 = randomPolynomial.at(2 * it + 1)
                }
            } else {
                DataSet.logDistinguishingFeature(it, false)
                // y0 = f(2i), y1 = f(2i+1)
                y0 = randomPolynomial.at(2 * it)
                y1 = randomPolynomial.at(2 * it + 1)
            }
            TableRow(getAlpha(y0, it, password), getBeta(y1, it, password))
        }

        IO.writeInstructionTable(username, newInstructionTable)

        return true
    }

    fun isDistinguishingFeature(successfulLogins: Int, mean: Double, stdDeviation: Double, threshold: Double): Boolean {
        if (successfulLogins < kHValue)
            return false
        return Math.abs(mean - threshold) > kValue * stdDeviation
    }
}
