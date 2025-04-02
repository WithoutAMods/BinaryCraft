package eu.withoutaname.mod.binarycraft.item

import eu.withoutaname.mod.binarycraft.block.ModBlocks
import eu.withoutaname.mod.binarycraft.block.WireBlock
import eu.withoutaname.mod.binarycraft.block.WireBlockEntity
import eu.withoutaname.mod.binarycraft.logic.ConnectionType
import eu.withoutaname.mod.binarycraft.util.platformHeight
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import kotlin.math.floor

class WireItem(val color: DyeColor) : Item(Properties()) {
    override fun onItemUseFirst(stack: ItemStack, context: UseOnContext): InteractionResult {
        val level = context.level
        if (!level.isClientSide) {
            val location = context.clickLocation
                .relative(context.clickedFace, 0.01) // tolerance
            val blockPos = BlockPos(floor(location.x).toInt(), floor(location.y).toInt(), floor(location.z).toInt())
            val inBlockLocation = location
                .subtract(blockPos.x.toDouble(), blockPos.y.toDouble(), blockPos.z.toDouble())
            val blockPosBelow = blockPos.below()
            if (level.getBlockState(blockPos).block == Blocks.AIR && level.getBlockState(blockPosBelow)
                    .isFaceSturdy(level, blockPosBelow, Direction.UP)
            ) {
                level.setBlock(blockPos, ModBlocks.WIRE.defaultBlockState(), Block.UPDATE_NONE)
            }
            val blockState = level.getBlockState(blockPos)
            if (blockState.block is WireBlock) {
                val wire = level.getBlockEntity(blockPos) as WireBlockEntity
                val side = when ((inBlockLocation.x < inBlockLocation.z) to (inBlockLocation.x < 1 - inBlockLocation.z)) {
                    true to true -> Direction.WEST
                    true to false -> Direction.SOUTH
                    false to true -> Direction.NORTH
                    false to false -> Direction.EAST
                    else -> error("unreachable")
                }
                val platformLevel =
                    (inBlockLocation.y / platformHeight).toInt().coerceAtMost(wire.data.numLevels - 1)
                if (wire.data.addConnection(platformLevel, side, ConnectionType.Colored(color))) {
                    wire.setChanged()
                    level.sendBlockUpdated(blockPos, blockState, blockState, Block.UPDATE_ALL_IMMEDIATE)
                    if (!context.player!!.isCreative) {
                        stack.shrink(1)
                    }
                    return InteractionResult.SUCCESS
                }
            }
        }
        return super.onItemUseFirst(stack, context)
    }
}
