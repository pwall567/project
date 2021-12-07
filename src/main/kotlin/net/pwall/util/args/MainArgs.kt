package net.pwall.util.args

import java.io.File
import java.lang.NumberFormatException
import net.pwall.util.UserError

class MainArgs(vararg args: Arg, block: MainArgs.() -> Unit = {}) {

    // TODO - standard --version arg?
    // TODO - standard --help arg?

    private val argDefs = mutableListOf(*args)
    private val groups = mutableListOf<ExclusiveGroup>()

    init {
        block()
    }

    fun add(arg: Arg) {
        argDefs.add(arg)
    }

    fun mutuallyExclusive(block: ExclusiveGroup.() -> Unit) {
        groups.add(ExclusiveGroup(this).also { it.block() })
    }

    fun process(args: Array<String>) {
        var i = 0
        while (i < args.size) {
            val arg = args[i++]
            val argDef = argDefs.find { it.name == arg } ?: throw ArgError("Unrecognised arg: $arg")
            groups.find { it.argDefs.contains(argDef) }?.argDefs?.find { it.supplied }?.let {
                throw ArgError("Arg $arg conflicts with ${it.name}")
            }
            i = argDef.acceptArg(args, i)
        }
        // TODO loop through argDefs and check for mandatory settings?
        // TODO allow entries without name?
    }

    abstract class Arg(val name: String) { // TODO val mandatory: Boolean = true  (?)

        // TODO allow addition of description to use in error messages or "usage" text

        var supplied: Boolean = false

        abstract fun acceptArg(args: Array<String>, index: Int): Int

        open fun match(arg: String) = arg == name // TODO switch to use this

        fun checkDuplicate() {
            if (supplied)
                throw ArgError("Duplicate arg: $name")
            supplied = true
        }

    }

    abstract class TypedArg<T>(name: String, private val defaultValue: T? = null) : Arg(name) {

        private var argValue: T? = null

        override fun acceptArg(args: Array<String>, index: Int): Int {
            checkDuplicate()
            if (index >= args.size)
                throw ArgError("Arg with no value: $name")
            argValue = fromString(args[index])
            return index + 1
        }

        abstract fun fromString(string: String): T

        val value: T
            get() = argValue ?: defaultValue ?: throw ArgError("Arg not set: $name")

    }

    class StringArg(name: String, defaultValue: String? = null) : TypedArg<String>(name, defaultValue) {

        override fun fromString(string: String) = string

    }

    class IntArg(name: String, defaultValue: Int? = null) : TypedArg<Int>(name, defaultValue) {

        override fun fromString(string: String): Int = try {
                string.toInt()
            }
            catch (_: NumberFormatException) {
                throw ArgError("Not an integer - $string")
            }

    }

    class FileArg(
        name: String,
        defaultValue: File? = null,
        private val checkExists: Boolean = false,
        private val checkDirectory: Boolean = false,
        private val checkFile: Boolean = false,
    ) : TypedArg<File>(name, defaultValue) {

        override fun fromString(string: String): File {
            val file = File(string)
            if (checkExists && !file.exists())
                throw ArgError("File does not exist - $string")
            if (checkDirectory && !file.isDirectory)
                throw ArgError("Not a directory - $string")
            if (checkFile && !file.isFile)
                throw ArgError("Not a file - $string")
            return file
        }

    }

    class BooleanArg(name: String) : Arg(name) {

        // TODO consider --no option (Boolean set but false) (call the new one BooleanArg and the existing one FlagArg?)

        private var argValue: Boolean? = null

        override fun acceptArg(args: Array<String>, index: Int): Int {
            checkDuplicate()
            argValue = true
            return index
        }

        val value: Boolean
            get() = argValue ?: throw ArgError("Arg not set: $name")

    }

    class ArgError(msg: String) : UserError(msg)

    class ExclusiveGroup(val mainArgs: MainArgs) {
        val argDefs = mutableListOf<Arg>()
        fun add(arg: Arg) {
            argDefs.add(arg)
            mainArgs.argDefs.add(arg)
        }
    }

}
