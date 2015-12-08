package es.gabrielborg.keystroke.core

import es.gabrielborg.keystroke.utility.*
import es.gabrielborg.keystroke.utility.polynomial.*

public object EnrolmentService {
    fun enroll(username: String, password: String, verbose: Boolean = false) {
        val randomPolynomial = getRandomPolynomial()
        if (verbose && kDebug)
            println("Hpwd is " + randomPolynomial[0])

        val instructionTable = Array(kNumberOfFeatures) {
            val y0 = randomPolynomial.at(2 * it) // y0 = f(2i)
            val y1 = randomPolynomial.at(2 * it + 1) // y1 = f(2i+1)
            TableRow(getAlpha(y0, it, password), getBeta(y1, it, password))
        }

        IO.writeInstructionTable(username, instructionTable)

        IO.writeHistoryFile(username, randomPolynomial[0])

        if (verbose)
            println("The account for '$username' has been created successfully.")
    }
}