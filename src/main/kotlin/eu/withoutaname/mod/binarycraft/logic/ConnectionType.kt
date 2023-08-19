package eu.withoutaname.mod.binarycraft.logic

import net.minecraft.world.item.DyeColor

sealed class ConnectionType {
    data object Simple : ConnectionType()
    data class Colored(val color: DyeColor) : ConnectionType()
}
