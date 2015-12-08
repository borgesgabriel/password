package es.gabrielborg.keystroke

import es.gabrielborg.keystroke.core.EnrolmentService
import es.gabrielborg.keystroke.core.LoginService
import es.gabrielborg.keystroke.utility.DataSet
import es.gabrielborg.keystroke.utility.IO
import es.gabrielborg.keystroke.utility.kNumberOfFeatures
import es.gabrielborg.keystroke.utility.kPassword
import org.apache.commons.csv.CSVFormat
import java.io.FileReader
import java.util.*
import kotlin.util.measureTimeMillis

fun main(args: Array<String>) {
    val records = CSVFormat.EXCEL.withHeader().parse(FileReader("./assets/dataset/dataset.csv"))
    for (record in records) {
        val featureArray = Array(kNumberOfFeatures) {
            record.get(it + 3).toDouble()
        }.toDoubleArray()
        DataSet[record.get("subject").toInt(), record.get("sessionIndex").toInt(), record.get("rep").toInt()] =
                featureArray
    }

    val confusion = Array(2) { Array(2) { 0 } }
    println("Time elapsed: " + measureTimeMillis {
        for (i in 1..51) {
            val username = "user$i"
            println("$username being tested")
            EnrolmentService.enroll(username, kPassword)

            for (j in 1..50)
                assert(LoginService.login(username, kPassword, DataSet[i, 1, j]))

            val loginSequence = ArrayList<DoubleArray>()

            for (j in 1..50)
                for (k in 2..8)
                    loginSequence.add(DataSet[i, k, j]) // called 350 times

            for (j in 1..51) {
                if (i == j) continue
                for (k in 1..7) {
                    loginSequence.add(DataSet[j, k, k]) // called 350 times
                }
            }

            for (j in 0..349) {
                // authentic login
                var result = LoginService.login(username, kPassword, loginSequence[j])
                if (result) ++confusion[0][0]
                else ++confusion[0][1]
                // non-authentic login
                result = LoginService.login(username, kPassword, loginSequence[350 + j])
                if (result) ++confusion[1][0]
                else ++confusion[1][1]
            }

            IO.deleteUser(username)
        }
    } / 1000.0)
    println("Results:")
    println("Confusion matrix:\n${confusion[0][0]} ${confusion[0][1]}\n${confusion[1][0]} ${confusion[1][1]}")
    val total = (confusion[0][0] + confusion[0][1] + confusion[1][0] + confusion[1][1]).toDouble() / 100
    println("Normalised confusion matrix:\n${confusion[0][0] / total} ${confusion[0][1] / total}\n" +
            "${confusion[1][0] / total} ${confusion[1][1] / total}")
    val sum = { a: Int, b: Int -> a + b }
    println("Distinguishing features:\n" +
            "${DataSet.distinguishing.joinToString()}\n" +
            "${DataSet.nonDistinguishing.joinToString()}\n" +
            "${DataSet.distinguishing.reduce(sum)}/${DataSet.nonDistinguishing.reduce(sum)} = " +
            "${100 * DataSet.distinguishing.reduce(sum) / (DataSet.distinguishing.reduce(sum) + DataSet.nonDistinguishing.reduce(sum))}")
}
