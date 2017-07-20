# KIDE Autocomplete Mockup
This repo contains some code that can be used to imitate what KIDE would do to interact with the Kotlin compiler (for autocomplete) from JavaScript.

# Scripts
### `./ide.kt`
This is the front-end. This is what the IDE would be doing to talk to `./talk.kt`

### `./talk.kt`
This is the back-end. This builds the syntax tree and then provides an interface to communicate with `./ide`. *NOTE: Currently using dummy data.*

### `./comp.kt`
This is WIP on actually providing data from `./talk.kt`. Will be moved into the tool once completed

# Usage
You need to have KScript installed to use this scripts.