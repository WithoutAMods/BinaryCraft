package eu.withoutaname.mod.binarycraft.logic.api

interface Connection {
    val id: Int
}

interface InputConnection : Connection

interface OutputConnection : Connection

interface InternalConnection : InputConnection, OutputConnection

interface ExternalInput : InputConnection {
    fun setState(state: State)
}

interface ExternalOutput : OutputConnection {
    fun getState(): State
}
