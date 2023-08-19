package eu.withoutaname.mod.binarycraft.logic

import net.minecraft.core.BlockPos

interface CircuitManager {
    fun addGate(gateBlock: GateBlock)
    fun removeGate(gateBlock: GateBlock)

    fun addConnection(pos: BlockPos, connectionType: ConnectionType)
    fun removeConnection(pos: BlockPos, connectionType: ConnectionType)
}
