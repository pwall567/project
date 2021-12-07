package net.pwall.util.args

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.expect
import java.io.File
import net.pwall.util.UserError

class MainArgsTest {

    @Test fun `should accept empty array`() {
        MainArgs {
            process(emptyArray())
        }
    }

    @Test fun `should process string arg`() {
        val testArg = MainArgs.StringArg("--test")
        MainArgs {
            add(testArg)
            process(arrayOf("--test", "testValue"))
        }
        expect("testValue") { testArg.value }
    }

    @Test fun `should process int arg`() {
        val testArg = MainArgs.IntArg("--test")
        MainArgs {
            add(testArg)
            process(arrayOf("--test", "8888"))
        }
        expect(8888) { testArg.value }
    }

    @Test fun `should process file arg`() {
        val testArg = MainArgs.FileArg("--test")
        val file = File("src/test/kotlin/net/pwall/util/MainArgsTest.kt")
        MainArgs {
            add(testArg)
            process(arrayOf("--test", file.absolutePath))
        }
        expect(file.absolutePath) { testArg.value.absolutePath }
    }

    @Test fun `should process boolean arg`() {
        val testArg = MainArgs.BooleanArg("--test")
        MainArgs {
            add(testArg)
            process(arrayOf("--test"))
        }
        expect(true) { testArg.value }
    }

    @Test fun `should process multiple args`() {
        val testArg1 = MainArgs.BooleanArg("--test1")
        val testArg2 = MainArgs.StringArg("--test2")
        MainArgs {
            add(testArg1)
            add(testArg2)
            process(arrayOf("--test1", "--test2", "testValue"))
        }
        expect(true) { testArg1.value }
        expect("testValue") { testArg2.value }
    }

    @Test fun `should fail on error`() {
        val testArg = MainArgs.StringArg("--test")
        assertFailsWith<UserError> {
            MainArgs {
                add(testArg)
                process(arrayOf("--text", "testValue"))
            }
        }.let {
            expect("Unrecognised arg: --text") { it.message }
        }
    }

    @Test fun `should enforce mutual exclusivity`() {
        val arg1 = MainArgs.BooleanArg("--arg1")
        val arg2 = MainArgs.BooleanArg("--arg2")
        assertFailsWith<MainArgs.ArgError> {
            MainArgs {
                mutuallyExclusive {
                    add(arg1)
                    add(arg2)
                }
                process(arrayOf("--arg1", "--arg2"))
            }
        }.let {
            expect("Arg --arg2 conflicts with --arg1") { it.message }
        }
    }

}
