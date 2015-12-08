package es.gabrielborg.keystroke.utility.polynomial

import es.gabrielborg.keystroke.utility.kLargePrime
import es.gabrielborg.keystroke.utility.kNumberOfFeatures
import es.gabrielborg.keystroke.utility.toSha256BigInteger
import java.io.Serializable
import java.math.BigInteger
import java.security.SecureRandom
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.plus
import kotlin.math.times

public data class TableRow(
        public val alpha: BigInteger = BigInteger.ZERO,
        public val beta: BigInteger = BigInteger.ZERO
) : Serializable {
    public override fun toString(): String = "$alpha $beta"
}

private val secureRandom = SecureRandom()

// randomPolynomial has degree (kNumberOfFeatures - 1) and contains elements in the [100,160)-bit range
fun getRandomPolynomial() = Array(kNumberOfFeatures) { getRandomBigInteger() } // paper calls it "f"

fun getRandomBigInteger() = BigInteger(ThreadLocalRandom.current().nextInt(100, 160), secureRandom)

fun getLowRandomBigInteger() : BigInteger {
    var result : BigInteger
    do {
        result = BigInteger.valueOf(ThreadLocalRandom.current().nextLong(-1000, 1000))
    } while (result == BigInteger.ZERO)
    return result
}

// alpha[i] = y0[i]*(F(password,2i) mod q)
fun getAlpha(y0: BigInteger, i: Int, password: String) = y0 * (((password + 2 * i).toSha256BigInteger()) % kLargePrime)

// beta[i] = y1[i]*(F(password,2i+1) mod q)
fun getBeta(y1: BigInteger, i: Int, password: String) = y1 * (((password + (2 * i + 1)).toSha256BigInteger()) % kLargePrime)

fun Array<BigInteger>.at(x: Int): BigInteger {
    // Evaluate f(x), where f is an array of BigIntegers
    val bigX = BigInteger.valueOf(x.toLong())
    var result = BigInteger.ZERO
    for (i in 0..kNumberOfFeatures - 1) {
        result += bigX.pow(i) * this[i]
    }
    return result
}