package eu.withoutaname.mod.binarycraft.logic.api

interface CircuitConnection {
    val id: Int
    val state: State
}

interface CircuitInput : CircuitConnection {
    override var state: State
}
