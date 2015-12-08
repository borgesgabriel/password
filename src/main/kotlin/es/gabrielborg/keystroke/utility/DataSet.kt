package es.gabrielborg.keystroke.utility

import es.gabrielborg.keystroke.utility.polynomial.TableRow
import java.util.*

object DataSet {
    /**
     * Ranges:
     * subject: 1 to 51
     * sessionIndex: 1 to 8
     * rep: 1 to 50
     * featureArray: 21 doubles
     */
    private val matrix = Array(52) { Array(9) { Array(51) { doubleArrayOf() } } }
    var distinguishing = Array(kNumberOfFeatures) { 0 }
    var nonDistinguishing = Array(kNumberOfFeatures) { 0 }
    private val instructionTableMap = HashMap<String, Array<TableRow>>()
    private val historyMap = HashMap<String, ByteArray>()

    operator fun get(subject: Int, sessionIndex: Int, rep: Int) = matrix[subject][sessionIndex][rep]

    fun getInstructionTable(username: String) = instructionTableMap[username]!!

    fun getEncryptedHistory(username: String) = historyMap[username]!!

    operator fun set(subject: Int, sessionIndex: Int, rep: Int, featureArray: DoubleArray) {
        matrix[subject][sessionIndex][rep] = featureArray
    }

    operator fun set(username: String, instructionTable: Array<TableRow>) =
            instructionTableMap.put(username, instructionTable)

    operator fun set(username: String, encryptedHistory: ByteArray) = historyMap.put(username, encryptedHistory)

    fun logDistinguishingFeature(feature: Int, isDistinguishing: Boolean) =
            if (isDistinguishing) ++distinguishing[feature] else ++nonDistinguishing[feature]

    fun removeUser(username: String) {
        historyMap.remove(username)
        instructionTableMap.remove(username)
    }
}