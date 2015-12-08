package es.gabrielborg.keystroke.utility

import java.math.BigInteger

val kPassword = ".tie5Roanl"
val kPasswordLength = kPassword.length
val kNumberOfFeatures = 2 * kPasswordLength - 1

val kSecretHeader = "This is a secret header. I'll just throw in some random stuff: ads8pf7d897vyhcxkljfsdahriuweyr87f"

val kLargePrime = BigInteger("25262728293031323334353637383940414243444546474849") // Guaranteed to be prime
                                                                                   // 2*10^50 ~ 164 bits. "q"
val kDebug = true

val kDelimiter = ";    ;"

val kCharset = "UTF-8"

val kHistoryFileSize = 2000

val kValue = 1.0 // This is called "k" in the paper

val kHValue = 50 // This is called "h" in the paper

val kWriteToFile = false
