package eu.withoutaname.mod.binarycraft.logic

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction

abstract class GateBlock {
    var updateTrigger: () -> Unit = {}

    abstract val pos: BlockPos
    abstract val gateBehavior: GateBehavior

    abstract fun canConnectTo(side: Direction, connectionType: ConnectionType): CanConnectResult?

    data class CanConnectResult(val isInput: Boolean, val id: Int)
}
