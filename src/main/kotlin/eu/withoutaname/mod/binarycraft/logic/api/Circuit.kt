package eu.withoutaname.mod.binarycraft.logic.api

interface Circuit {
    fun createInput(): InputConnection
    fun createOutput(): OutputConnection
    fun createConnectionWire(): InternalConnection

    fun addGate(gate: Gate, inputWires: List<InputConnection>, outputWires: List<OutputConnection>): GateID
    fun removeGate(gateID: GateID)
}

