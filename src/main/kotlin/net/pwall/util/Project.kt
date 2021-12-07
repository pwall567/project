package net.pwall.util

import java.io.File
import java.io.StringReader
import java.time.LocalDate
import java.time.Year

import io.kjson.JSON.asArray
import io.kjson.JSON.asObject
import io.kjson.JSON.asString
import io.kjson.mustache.parser.Parser
import io.kjson.yaml.YAML

@Suppress("unused")
class Project(
    val dir: File,
    val name: String,
    val group: String,
    val version: String,
    val packageName: String,
    val language: Language?,
    val buildTool: BuildTool?,
) {

    val packageDir: String
        get() = packageName.replace('.', '/')

    val date: LocalDate = LocalDate.now()
    val year: Year = Year.now()

    fun generate() {
        val resourceURL = Project::class.java.getResource("/") ?: throw RuntimeException("Can't access templates")
        val parser = Parser(resourceURL)
        val projectTemplate = parser.parseByName("project")
        val yaml = YAML.parse(StringReader(projectTemplate.render(this))).rootNode
        val files = yaml.asObject["files"].asArray
        for (entry in files) {
            val entryObject = entry.asObject
            val file = File(dir, entryObject["name"].asString).absoluteFile
            if (!file.parentFile.exists())
                file.parentFile.mkdirs()
            when {
                entryObject.containsKey("binary") -> {
                    val inputStream = resourceURL.toURI().resolve(entryObject["binary"].asString).toURL().openStream()
                    file.outputStream().use {
                        inputStream.copyTo(it)
                    }
                }
                entryObject.containsKey("template") -> {
                    val fileTemplate = parser.parseByName(entryObject["template"].asString)
                    file.writer().use {
                        fileTemplate.renderTo(it, this)
                    }
                }
            }
        }
    }

}
