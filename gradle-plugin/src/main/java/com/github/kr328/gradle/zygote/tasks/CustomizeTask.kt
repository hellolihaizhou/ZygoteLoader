package com.github.kr328.gradle.zygote.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

abstract class CustomizeTask : DefaultTask() {
    @get:InputDirectory
    abstract val customizeFiles: DirectoryProperty

    @get:InputDirectory
    abstract val checksumFiles: DirectoryProperty

    @get:OutputDirectory
    abstract val destinationDir: DirectoryProperty

    @TaskAction
    fun doAction() {
        val customizes = customizeFiles.get().asFile.listFiles()?.toList() ?: emptyList()
        val checksums = checksumFiles.get().asFile.listFiles()?.toList() ?: emptyList()

        val files = (customizes + checksums).map { it.name }.sorted()

        val customizeText = buildString {
            appendLine("# Generated by ZygoteLoader. DO NOT EDIT.")
            appendLine()
            files.forEach {
                appendLine("[ -f \"\$MODPATH/customize.d/$it\" ] || abort \"! Part '$it' not found\"")
                appendLine(". \"\$MODPATH/customize.d/$it\"")
            }
            appendLine()
            appendLine("rm -rf \"\$MODPATH/customize.d\"")
        }

        destinationDir.asFile.get().apply {
            mkdirs()

            resolve("customize.sh").writeText(customizeText)
        }
    }
}