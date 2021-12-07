package net.pwall.util

import java.io.File
import net.pwall.util.args.MainArgs

fun main(args: Array<String>) {
    val nameArg = MainArgs.StringArg("--name")
    val groupArg = MainArgs.StringArg("--group")
    val versionArg = MainArgs.StringArg("--version", "1.0")
    val packageArg = MainArgs.StringArg("--package")
    val kotlinArg = MainArgs.BooleanArg("--kotlin")
    val javaArg = MainArgs.BooleanArg("--java")
    val mavenArg = MainArgs.BooleanArg("--maven")
    val gradleArg = MainArgs.BooleanArg("--gradle")
    val dirArg = MainArgs.FileArg("--dir", File("."))
    MainArgs {
        add(dirArg)
        add(nameArg)
        add(groupArg)
        add(packageArg)
        add(versionArg)
        mutuallyExclusive {
            add(kotlinArg)
            add(javaArg)
        }
        mutuallyExclusive {
            add(mavenArg)
            add(gradleArg)
        }
        process(args)
    }
    val language = when { // TODO can we combine this with the mutuallyExclusive, creating an EnumArg?
        kotlinArg.supplied -> Language.KOTLIN
        javaArg.supplied -> Language.JAVA
        else -> null
    }
    val buildTool = when {
        mavenArg.supplied -> BuildTool.MAVEN
        gradleArg.supplied -> BuildTool.GRADLE
        else -> null
    }
    Project(
        dir = dirArg.value,
        name = nameArg.value,
        group = groupArg.value,
        version = versionArg.value,
        packageName = packageArg.value,
        language = language,
        buildTool = buildTool,
    ).generate()
}
