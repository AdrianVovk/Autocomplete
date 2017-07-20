#!/usr/bin/env kscript
import kotlin.system.exitProcess

//////////////////////////////////////////////////////////////////////////////////////////////////////////
// Helper methods
//////////////////////////////////////////////////////////////////////////////////////////////////////////

fun status(status: String) = println("status: $status")
fun error(message: String) = println("error: $message")
fun call(method: String) = println("call: $method")
fun input(prompt: String = "command"): String? {
   println("?$prompt")
   return readLine()
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////
// Impl.
//////////////////////////////////////////////////////////////////////////////////////////////////////////

var possible: Array<String>? = null

fun complete(data: String) {
   if (possible == null) {
      error("no index")
      return
   }
   val outputs = (possible as Array<String>).filter { it.startsWith(data) }
   val outputString = outputs.map { "\"$it\"" }.joinToString()
   println("""
      {
         "string":"$data",
         "completion": [
            $outputString
         ]
      }
   """.trimIndent())
}

fun describe(type: String, identifier: String) {
   if (possible == null) {
      error("no index")
      return
   }

   var name = identifier
   if (type == "func") name += "()"

   if ((possible as Array<String>).filter { it == name }.size < 1) {
      error("not found")
      return
   }
   println("""
      {
         "name":"${name.removeSuffix("()")}",
         "description":""
         "type":"${if (type == "func") "function" else "property"}"
         "args":[

         ]
      }
   """.trimIndent())
}

fun index(directory: String) {
   status("indexing")
   possible = arrayOf(
      "actionBar",
      "actions",
      "activity",
      "archive",
      "buildArchive()",
      "connect()",
      "disconnect()",
      "cleanup()",
      "connection_id",
      "cache",
      "dumpCache()",
      "forceRestartConnection()",
      "killAll()",
      "microCodeVersion",
      "malloc()",
      "GroupInfo",
      "groupInfo",
      "groupInfo.blocks",
      "groupInfo.test",
      "groupInfo.test.sub",
      "groupInfo.ammount",
      "groupInfo.nBlocks",
      "sizeOf()"
   )
   status("ready")
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////
// Main loop
//////////////////////////////////////////////////////////////////////////////////////////////////////////

val VER = 1

println("---------------- Starting Communication ----------------")
println("version: $VER")
if (input("verify") != "ok") {
   error("version mismatch")
   exitProcess(1)
}
status("connected")

listen@ while (true) {

   // Process the input to parse commands
   var ln = input() ?: ""
   if (ln.split(":").size < 2) ln += ";"
   val command = ln.split(";")[0].split(",")[0].trim()
   val modifiers = ln.removePrefix(command).removePrefix(",").trim().split(";")[0].split(",").map { it.trim() }
   val data = ln.split(";")[1].trim()
   //println("Input: command=$command, modifiers=$modifiers, data=$data")

   when (command) {
      "index" -> index(data)
      "complete" -> complete(data)
      "describe" -> describe(type = modifiers[0], identifier = data)
      "done" -> break@listen
      else -> error("invalid")
   }
}
status("done")