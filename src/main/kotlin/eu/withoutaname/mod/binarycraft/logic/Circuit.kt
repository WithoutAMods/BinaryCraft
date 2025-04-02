package eu.withoutaname.mod.binarycraft.logic

interface Circuit {
    fun getState(connection: ConnectionId): ConnectionState

    fun addGate(gateBehavior: GateBehavior): GateId
    fun removeGate(gateId: GateId)
    fun setGateInput(gateId: GateId, inputIndex: Int, connection: ConnectionId?)
    fun setGateOutput(gateId: GateId, outputIndex: Int, connection: ConnectionId?)
}

