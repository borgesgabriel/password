package es.gabrielborg.keystroke

import es.gabrielborg.keystroke.core.EnrolmentService
import es.gabrielborg.keystroke.core.LoginService
import es.gabrielborg.keystroke.utility.IO
import es.gabrielborg.keystroke.utility.kDebug
import org.apache.commons.lang3.StringUtils

fun main(args: Array<String>) {
    println("Welcome to the PhS!")
    if (kDebug)
        println("*You are running this service on debug mode. Be aware passwords will be displayed in the console*")
    loop@while (true) {
        println("Enter a username to login. If you do not yet have an account, " +
                "enter 'c username'. If you wish to exit this application, type 'q'.")
        var input = readLine() ?: continue
        if (input.isEmpty()) continue

        when (input.take(2)) {
            "c " -> EnrolmentService.enroll(input.drop(2), IO.readPassword(enroll = true), verbose = true)
            "q" -> break@loop
            else -> {
                // Login
                if (!StringUtils.isAlphanumeric(input)) {
                    println("Usernames are restricted to alphanumeric characters.")
                    continue@loop
                }
                val featureArray = IO.readFeatureTable(input)
                if (featureArray == null) {
                    println("User not found on the feature table.")
                    continue@loop
                }
                println("Verifying $input.")
                if (LoginService.login(input, IO.readPassword(enroll = false), featureArray, verbose = true)) {
                    println("'$input' has logged in successfully.")
                } else {
                    println("'$input' has **NOT** logged in successfully.")
                }
            }
        }
    }
    println("Goodbye!")
}

