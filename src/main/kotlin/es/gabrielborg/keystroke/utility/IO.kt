package es.gabrielborg.keystroke.utility

import es.gabrielborg.keystroke.utility.polynomial.TableRow
import java.io.File
import java.math.BigInteger
import java.util.*

object IO {

    val threshold = readThresholdValues()

    private fun readThresholdValues(): DoubleArray {
        val thresholdValues = File("./assets/threshold").readLines(kCharset)
        return thresholdValues.first().split(" ").map { it.toDouble() }.toDoubleArray()
    }

    fun readFeatureTable(username: String): DoubleArray? {
        val featureTable = File("./assets/features").readLines(kCharset)
        val featureRow = featureTable.find { it.startsWith(username) } ?: return null
        val featureList = featureRow.split(" ").drop(1)
        return featureList.map { it.toDouble() }.toDoubleArray()
    }

    fun readInstructionTable(username: String): Array<TableRow> {
        if (!kWriteToFile) {
            return DataSet.getInstructionTable(username)
        }
        val reader = File("./users/$username.table").bufferedReader()
        val instructionTable = Array(kNumberOfFeatures) { TableRow() }
        var lineNumber = 0
        reader.forEachLine {
            val feature = it.split(" ")
            instructionTable[lineNumber] = TableRow(BigInteger(feature[0]), BigInteger(feature[1]))
            ++lineNumber
        }
        return instructionTable
    }

    fun writeInstructionTable(username: String, instructionTable: Array<TableRow>) {
        if (kWriteToFile) // vide TableRow's toString()
            File("./users/$username.table").writeText(instructionTable.joinToString (separator = "\n"), kCharset)
        else
            DataSet[username] = instructionTable
    }

    fun readHistoryFile(username: String, hpwd: BigInteger, verbose: Boolean): ArrayList<DoubleArray>? {
        val encryptedHistory = if (kWriteToFile) {
            File("./users/$username.history").readBytes()
        } else {
            DataSet.getEncryptedHistory(username)
        }
        val history = try {
            CryptoUtils.decrypt(username + kDelimiter + hpwd, encryptedHistory)
        } catch (e: Throwable) {
            // hpwd doesn't match, probably
            return null
        }
        val historyLines = history.lines().dropLast(1) // last line is '\0's
        if (historyLines.size < 3 || historyLines[0] != kSecretHeader || historyLines[1] != username)
            return null
        // History is properly-formed
        if (verbose && kDebug)
            println("History file has been read correctly")
        var featureTable = ArrayList<DoubleArray>()
        val numberOfLogins = historyLines[2].toInt()
        if (historyLines.size < 3 + numberOfLogins) {
            if (verbose && kDebug)
                throw Exception("History should have more lines")
            return null
        }
        for (i in 1..numberOfLogins) {
            featureTable.add(historyLines[i + 2].split(" ").map { it.toDouble() }.toDoubleArray())
        }
        return featureTable
    }

    fun writeHistoryFile(username: String, hpwd: BigInteger, featureTable: ArrayList<DoubleArray> = ArrayList<DoubleArray>()) {
        var history = kSecretHeader + "\n" +
                username + "\n" +
                featureTable.size + "\n" +
                featureTable.joinToString(separator = "\n") { it.joinToString(separator = " ") } + "\n"
        // Pad to 2000 bytes
        for (i in 1..kHistoryFileSize - history.length)
            history += 0.toChar()

        val encryptedHistory = CryptoUtils.encrypt(username + kDelimiter + hpwd, history)
        if (kDebug && history != CryptoUtils.decrypt(username + kDelimiter + hpwd, encryptedHistory))
            throw Exception("Chaos is amongst us")
        if (kWriteToFile) {
            val file = File("./users/$username.history")
            file.writeBytes(encryptedHistory)
        } else {
            DataSet[username] = encryptedHistory
        }
    }

    public fun readPassword(enroll: Boolean): String {
        while (true) {
            println(if (enroll) "Please enter a $kPasswordLength character password." else "Please enter your password.")
            val password = readLine() ?: continue
            if (password.length == kPasswordLength) {
                return password
            }
        }
    }

    public fun deleteUser(username: String) {
        if (kWriteToFile) {
            File("./users/$username.table").delete()
            File("./users/$username.history").delete()
        } else {
            DataSet.removeUser(username)
        }
    }
}
