package eu.withoutaname.mod.binarycraft.block

import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.Connection
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape
import net.neoforged.neoforge.client.model.data.ModelData
import kotlin.math.min

class WireBlockEntity(pos: BlockPos, state: BlockState) : BlockEntity(ModBlockEntityTypes.WIRE, pos, state) {
    private val data = WireBlockData()

    override fun loadAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.loadAdditional(tag, registries)
        data.deserializeNBT(registries, tag.getCompound("data"))
    }

    override fun saveAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        tag.put("data", data.serializeNBT(registries))
        super.saveAdditional(tag, registries)
    }

    override fun getUpdateTag(registries: HolderLookup.Provider): CompoundTag {
        return data.serializeNBT(registries)
    }

    override fun handleUpdateTag(tag: CompoundTag, lookupProvider: HolderLookup.Provider) {
        data.deserializeNBT(lookupProvider, tag)
    }

    override fun getUpdatePacket(): Packet<ClientGamePacketListener> {
        return ClientboundBlockEntityDataPacket.create(this)
    }

    override fun onDataPacket(
        net: Connection,
        pkt: ClientboundBlockEntityDataPacket,
        lookupProvider: HolderLookup.Provider
    ) {
        pkt.tag.let {
            handleUpdateTag(it, lookupProvider)
            if (level?.isClientSide == true) {
                level?.sendBlockUpdated(worldPosition, blockState, blockState, Block.UPDATE_ALL)
                requestModelDataUpdate()
            }
        }
    }

    override fun getModelData(): ModelData {
        return ModelData.builder()
            .with(WireBlock.DATA, data)
            .build()
    }

    fun getShape(): VoxelShape {
        if (data.wireAbove) return shapes[4]
        return shapes[data.level - 1]
    }

    companion object {
        private val gridShapes = Array(4, ::getGridShape)
        private val wireShapes = Array(4, ::getWireShape)
        private val shapes = Array(5) { level ->
            Shapes.or(
                Shapes.empty(),
                *((1..level).map { gridShapes[it-1] }).toTypedArray(),
                *((0 .. min(3, level)).map { wireShapes[it] }).toTypedArray()
            )
        }

        private fun getGridShape(level: Int): VoxelShape {
            val pillarWidth = 1 / 16.0
            val platformHeight = 4 / 16.0
            val platformThickness = .5 / 16.0
            val pillarHeight = platformHeight - platformThickness
            val offset = level * platformHeight
            return Shapes.or(
                // Platform
                Shapes.box(0.0, offset + pillarHeight, 0.0, 1.0, offset + platformHeight, 1.0),
                // Pillar nw
                Shapes.box(0.0, offset, 0.0, pillarWidth, offset + pillarHeight, pillarWidth),
                // Pillar sw
                Shapes.box(0.0, offset, 1.0 - pillarWidth, pillarWidth, offset + pillarHeight, 1.0),
                // Pillar se
                Shapes.box(1.0 - pillarWidth, offset, 1.0 - pillarWidth, 1.0, offset + pillarHeight, 1.0),
                // Pillar ne
                Shapes.box(1.0 - pillarWidth, offset, 0.0, 1.0, offset + pillarHeight, pillarWidth)
            )
        }

        private fun getWireShape(level: Int): VoxelShape {
            val platformHeight = 4 / 16.0
            val offset = level * platformHeight
            val cableThickness = .5 / 16.0
            val cableSpacing = .25 / 16.0
            val cableStart = .5 - (cableThickness * 16 + cableSpacing * 15) / 2

            return Shapes.or(
                Shapes.box(0.0, offset, cableStart, 1.0, offset + cableThickness, 1.0 - cableStart),
                Shapes.box(cableStart, offset, 0.0, 1.0 - cableStart, offset + cableThickness, 1.0)
            )
        }
    }
}
