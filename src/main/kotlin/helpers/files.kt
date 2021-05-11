package net.nprod.nphandler.helpers

import java.io.File

/**
 * Write a given string to a file.
 */
fun String.toFile(fileName: String) = File(fileName).bufferedWriter().use {
    it.write(this)
}
