package net.pwall.util

import java.io.PrintStream
import java.io.PrintWriter
import java.text.MessageFormat

open class UserError(msg: String) : RuntimeException(msg) {

    override fun printStackTrace() {
        printStackTrace(System.err)
    }

    override fun printStackTrace(ps: PrintStream) {
        ps.println(this)
    }

    override fun printStackTrace(pw: PrintWriter) {
        pw.println(this)
    }

    override fun toString(): String = MessageFormat.format(pattern, localizedMessage)

    companion object {
        private val formatKey = "${UserError::class.qualifiedName}.format"
        private const val defaultFormat = "Error: {0}"
        val pattern: String = System.getProperty(formatKey, defaultFormat)
    }

}
