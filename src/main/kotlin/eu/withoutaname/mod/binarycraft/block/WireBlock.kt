package eu.withoutaname.mod.binarycraft.block

import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape
import net.neoforged.neoforge.client.model.data.ModelProperty

object WireBlock : BaseEntityBlock(
    Properties.of()
        .dynamicShape().noOcclusion().isValidSpawn { _, _, _, _ -> false }
        .isRedstoneConductor { _, _, _ -> false }.isSuffocating { _, _, _ -> false }.isViewBlocking { _, _, _ -> false }
) {
    val DATA = ModelProperty<WireBlockData>()
    private val CODEC: MapCodec<WireBlock> = simpleCodec {
        WireBlock
    }

    override fun newBlockEntity(pPos: BlockPos, pState: BlockState): BlockEntity {
        return WireBlockEntity(pPos, pState)
    }

    override fun codec(): MapCodec<out BaseEntityBlock> {
        return CODEC
    }

    override fun getRenderShape(pState: BlockState): RenderShape {
        return RenderShape.MODEL
    }

    override fun getShape(
        pState: BlockState, pLevel: BlockGetter, pPos: BlockPos, pContext: CollisionContext
    ): VoxelShape {
        val entity = pLevel.getBlockEntity(pPos, ModBlockEntityTypes.WIRE).orElse(null) ?: return Shapes.block()
        return entity.getShape()
    }

    override fun getShadeBrightness(pState: BlockState, pLevel: BlockGetter, pPos: BlockPos): Float {
        return 1.0F
    }

    override fun propagatesSkylightDown(pState: BlockState, pReader: BlockGetter, pPos: BlockPos): Boolean {
        return true
    }
}
