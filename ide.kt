#!/usr/bin/env kscript
//DEPS com.beust:klaxon:0.30
import java.io.*
import com.beust.klaxon.*
import kotlin.system.*

///////////////////////////////////////////////////
// Start process
///////////////////////////////////////////////////

val proc = Runtime.getRuntime().exec(arrayOf("./talk.kt"))

///////////////////////////////////////////////////
// Process interaction
///////////////////////////////////////////////////

val reader = BufferedReader(InputStreamReader(proc.getInputStream()))
val writer = proc.getOutputStream()

// Run a command on the process
fun tell(message: String) {
   writer.write("$message\n".toByteArray())
   writer.flush()
   if (args.contains("-v")) println("[TOOL] $message")
}
// Read a line of input
fun read(): String {
   val ln = reader.readLine() ?: ""
   if (args.contains("-v")) println("[TOOL] $ln")
   return ln
}
// Read all lines until you hit a certain string
fun readUntil(stop: String): String {
   var data = ""
   read@ while (true) {
      val ln = read()
      if (ln == stop) break@read
      data += ln
   }
   return data
}
// Read all lines until you hit ?command. Also acts as waitForPrompt().
fun readData() = readUntil("?command")
// Wait until the process prints out a certain line
fun waitFor(stop: String) = readUntil(stop)
// Wait for the command prompt
fun waitForPrompt() = readData()

///////////////////////////////////////////////////
// Verify version
///////////////////////////////////////////////////

val VER = 1
var itVER = -1

verify@ while (true) {
   val ln = read()
   when {
      ln.startsWith("version: ") -> itVER = ln.removePrefix("version: ").toInt() // Extract the version from the tool
      ln == "?verify" -> tell(if (VER == itVER) "ok" else "no") // Wait for the tool to ask for verification
      ln == "status: connected" -> break@verify // Wait for the tool to accept the connection
      ln == "error: version mismatch" -> exitProcess(proc.waitFor()) // If the tool throws an error, exit this process
   }
}

///////////////////////////////////////////////////
// Build index
///////////////////////////////////////////////////

fun chdir() {
   print("Directory: "); val path = readLine() ?: "" // Ask for input
   tell("index; $path") // Tell the tool to index the path
   waitFor("status: indexing") // Wait for the tool to print out that it is indexing
   println("Indexing...")
   waitFor("status: ready") // Wait for it to finish indexing
}

waitForPrompt()
chdir() // Pick the initial directory
waitForPrompt()

///////////////////////////////////////////////////
// Main tool
///////////////////////////////////////////////////

main@ while (true) {
   print(":"); val input = readLine() ?: "" // Prompt for input
   if (!args.contains("-v")) print("\u001B[J") // Clear out previous output

   // Process meta commands
   if (input.startsWith(":")) when (input.removePrefix(":")) {
      "quit", "done" -> break@main // Quit command
      "chdir" -> {
         chdir() // Change the directory
         waitForPrompt()
         continue@main // Skip to next input
      }
      "help" -> {
         println("""
         Type in some text, and this tool will try to autocomplete it

         Available commands:
         :quit or :done - quits the tools
         :chdir - open and index a new directory
         :help - see this message

         Run this tool with `-v` to see the communication with the subprocess
         """.trimIndent())
      }
      else -> {
         println("Invalid command. Run :help for a list of available commands")
         continue@main
      }
   }

   // Autocomplete
   tell("complete; $input") // Get possible completion
   val parser = Parser() // Setup JSON parser
   val names = (parser.parse(StringReader(readData())) as JsonObject).array<String>("completion")!!.filter { it.split(".").size == input.split(".").size } // Get a list of names for completion
   var longestName = ""; var longestDesc = ""
   val completion = Array<String>(names.size) { "" } // Setup output array
   for (item in names) {
      tell("describe, ${if (item.endsWith("()")) "func" else "prop"}; ${item.removeSuffix("()")}") // Ask for description
      val json = parser.parse(StringReader(readData())) as JsonObject // Process the output
      val name = json.string("name"); val type = json.string("type") // Create variables

      val arguments: Array<String> = if (name == "connect" && type == "function") arrayOf("url: String", "(optional) http: Http") else json.array<String>("args")!!.toTypedArray()
      val desc = if (name == "connect" && type == "function") "Connect to our service" else json.string("description")

      val fName = " $name${ if (type == "function") "(${arguments.joinToString()})" else ""} "
      if (fName.length > longestName.length) longestName = fName
      val fDesc = " ${ if (desc != "") "$desc" else "" } "
      if (fDesc.length > longestDesc.length) longestDesc = fDesc
      completion.set(names.indexOf(item), "| ${if (type == "function") "F" else "V"} |$fName|$fDesc") // Format output
   }
   if (longestName != "") for (item in completion) {
      val (_, type, name, desc) = item.split("|")
      completion.set(completion.indexOf(item), arrayOf("", type, name.padEnd(longestName.length), desc).joinToString("|"))
   }
   if (longestDesc != "  ") for (item in completion) {
      val (_, type, name, desc) = item.split("|")
      completion.set(completion.indexOf(item), arrayOf("", type, name, desc.padEnd(longestDesc.length), "").joinToString("|"))
   }
   println(completion.joinToString("\n")) // Print out output

   if (!args.contains("-v")) print("\u001B[${completion.size + 1}A" + "".padEnd(input.length + 1) + "\u001B[${input.length + 1}D") // Jump up
}

///////////////////////////////////////////////////
// Cleanup
///////////////////////////////////////////////////

tell("done") // Tell the tool to cleanup
waitFor("status: done") // Wait for the tool to cleanup
proc.waitFor() // Wait for the tool to quit