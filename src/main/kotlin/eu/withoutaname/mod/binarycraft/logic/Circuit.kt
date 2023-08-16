package eu.withoutaname.mod.binarycraft.logic

interface Circuit {
    fun createConnection(): ConnectionId
    fun removeConnection(connection: ConnectionId)
    fun getState(connection: ConnectionId): State

    fun addGate(gateBehavior: GateBehavior): GateId
    fun removeGate(gateId: GateId)
    fun setGateInput(gateId: GateId, inputIndex: Int, connection: ConnectionId?)
    fun setGateOutput(gateId: GateId, outputIndex: Int, connection: ConnectionId?)
}

