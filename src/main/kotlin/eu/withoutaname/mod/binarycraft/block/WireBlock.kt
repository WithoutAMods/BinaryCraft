package eu.withoutaname.mod.binarycraft.block

import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

object WireBlock : BaseEntityBlock(Properties.of()) {
    override fun newBlockEntity(pPos: BlockPos, pState: BlockState): BlockEntity {
        return WireBlockEntity(pPos, pState)
    }
}
