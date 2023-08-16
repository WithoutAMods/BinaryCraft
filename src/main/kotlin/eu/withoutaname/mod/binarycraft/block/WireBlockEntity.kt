package eu.withoutaname.mod.binarycraft.block

import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

class WireBlockEntity(pos: BlockPos, state: BlockState) : BlockEntity(ModBlockEntityTypes.WIRE, pos, state)
