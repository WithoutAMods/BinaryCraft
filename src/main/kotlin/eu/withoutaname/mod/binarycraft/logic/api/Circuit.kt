package eu.withoutaname.mod.binarycraft.logic.api

interface Circuit {
    fun createInput(): CircuitInput
    fun createConnection(): CircuitConnection

    fun addGate(gate: Gate, inputWires: List<CircuitConnection>, outputWires: List<CircuitConnection>): GateID
    fun removeGate(gateID: GateID)
}

