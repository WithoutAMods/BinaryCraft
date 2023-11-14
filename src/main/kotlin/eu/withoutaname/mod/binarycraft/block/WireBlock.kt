package eu.withoutaname.mod.binarycraft.block

import net.minecraft.core.BlockPos
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape
import net.minecraftforge.client.model.data.ModelProperty

object WireBlock : BaseEntityBlock(
    Properties.of()
        .dynamicShape().noOcclusion().isValidSpawn { _, _, _, _ -> false }
        .isRedstoneConductor { _, _, _ -> false }.isSuffocating { _, _, _ -> false }.isViewBlocking { _, _, _ -> false }
) {
    val DATA = ModelProperty<WireBlockData>()
    override fun newBlockEntity(pPos: BlockPos, pState: BlockState): BlockEntity {
        return WireBlockEntity(pPos, pState)
    }

    @Deprecated("Deprecated in Java")
    override fun getRenderShape(pState: BlockState): RenderShape {
        return RenderShape.MODEL
    }

    override fun getShape(
        pState: BlockState, pLevel: BlockGetter, pPos: BlockPos, pContext: CollisionContext
    ): VoxelShape {
        return Shapes.or(
            Shapes.box(.0, .0, .25, 1.0, 0.03125, .75), Shapes.box(.25, .0, .0, .75, 0.03125, 1.0)
        )
    }

    override fun getShadeBrightness(pState: BlockState, pLevel: BlockGetter, pPos: BlockPos): Float {
        return 1.0F
    }

    override fun propagatesSkylightDown(pState: BlockState, pReader: BlockGetter, pPos: BlockPos): Boolean {
        return true
    }
}
