package es.gabrielborg.keystroke.utility

import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.MessageDigest
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.math.BigInteger
import java.security.InvalidKeyException
import javax.crypto.BadPaddingException
import javax.crypto.Cipher

object CryptoUtils {

    private val IV = "1JVd6Bsasp;L9Uy7"
    private val transformation = "AES/CBC/PKCS5Padding"
    private val provider = BouncyCastleProvider()
    private val algorithm = "AES"

    fun encrypt(key: String, input: String): ByteArray {
        val cipher = Cipher.getInstance(transformation, provider)
        val secretKeySpec = SecretKeySpec(key.toSha256ByteArray(), algorithm)
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, IvParameterSpec(IV.toByteArray(kCharset)))
        return cipher.doFinal(input.toByteArray(kCharset))
    }

    @Throws(BadPaddingException::class)
    fun decrypt(key: String, input: ByteArray): String {
        val cipher = Cipher.getInstance(transformation, provider)
        val secretKeySpec = SecretKeySpec(key.toSha256ByteArray(), algorithm)
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, IvParameterSpec(IV.toByteArray(kCharset)))
        return String(cipher.doFinal(input), kCharset)
    }

}

fun String.toSha256BigInteger(): BigInteger {
    val md = MessageDigest.getInstance("SHA-256");
    md.update(this.toByteArray(kCharset));
    return BigInteger(1, md.digest())
}

fun String.toSha256ByteArray(): ByteArray {
    val sha256 = this.toSha256BigInteger().toByteArray()
    val sha256unsigned = sha256.sliceArray(1..sha256.size - 1) // First byte (sign) dropped
    if (sha256unsigned.size > 32)
        throw InvalidKeyException("SHA-256 key has ${sha256unsigned.size} bytes")
    return ByteArray(32 - sha256unsigned.size) + sha256unsigned // Handle cases where BigInteger has zeros to the left
}
