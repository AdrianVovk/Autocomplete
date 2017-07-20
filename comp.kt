#!/usr/bin/env kscript
//DEPS org.jetbrains.kotlin:kotlin-compiler-embeddable:1.1.3-2, com.beust:klaxon:0.30
import org.jetbrains.kotlin.com.intellij.psi.PsiFileFactory
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.psi.*

import com.beust.klaxon.*

val project = KotlinCoreEnvironment.createForProduction(
   Disposer.newDisposable(),
   CompilerConfiguration(),
   EnvironmentConfigFiles.JVM_CONFIG_FILES
).project
val psiFactory = PsiFileFactory.getInstance(project)

//print("File: "); val rawPath = readLine()!!

val ktFile = psiFactory.createFileFromText("test.kt", KotlinLanguage.INSTANCE, """
package a

import a.b.c

fun main(args: Array<String>) {
   println("hello")
}

fun test(): String = "ayy"

fun asdf(arg: String, defArg: Boolean = false) = ""

class A {

}

interface B

val a = "test"
""") as KtFile

abstract class NamedType(open val name: String) {
   abstract fun toJson(): JsonObject
   override fun toString() = toJson().toJsonString(true)
}
data class Argument(override val name: String, val type: String, val default: String, val mutable: Boolean = true): NamedType(name) {
   override fun toJson() = JSON().obj(
      "name" to name,
      "type" to type,
      "default" to default,
      "mutable" to mutable
   )
   override fun toString() = super.toString() // Data class stuff
}
data class Function(override val name: String, val args: Array<Argument>, val returnType: String, val documentation: String = ""): NamedType(name) {
   override fun toJson() = JSON().obj(
      "name" to name,
      "args" to JSON().array(args.map(Argument::toJson)),
      "returns" to returnType,
      "documentation" to documentation
   )
   override fun toString() = super.toString() // Data class stuff
}

println("Package Name: ${ktFile.getPackageFqName().asString()}")
val functions = mutableListOf<Function>()
ktFile.getDeclarations().forEach {
   when (it) {
      is KtFunction -> {
         val func = Function(
            name = it.getNameIdentifier()?.text ?: "ERROR",
            args = it.getValueParameters().map {
               Argument(
                  name = it.name ?: "ERROR",
                  type = it.getTypeReference()?.text ?: "ERROR",
                  default = it.getDefaultValue()?.text ?: "<NO DEFAULT>"
               )
            }.toTypedArray(),
            returnType = when {
              it.hasDeclaredReturnType() -> it.getTypeReference()?.text ?: "ERROR"
              it.hasBlockBody() -> "Unit" // No explicit type + body = Unit
              else -> "?" // TODO
            },
            documentation = ""
         )
         functions.add(func)
      }
      else -> println("[WARN] Can't process ${it::class.simpleName?.removePrefix("Kt")?.toLowerCase()?.reversed()?.replaceFirst("y", "ei")?.reversed() ?: "ERROR"}s yet")
   }
}

println(json {
   obj("functions" to array(functions.map(Function::toJson)))
}.toJsonString(true))