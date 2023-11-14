package eu.withoutaname.mod.binarycraft.block

import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.Connection
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraftforge.client.model.data.ModelData

class WireBlockEntity(pos: BlockPos, state: BlockState) : BlockEntity(ModBlockEntityTypes.WIRE, pos, state) {
    private val data = WireBlockData()

    override fun load(pTag: CompoundTag) {
        super.load(pTag)
        data.deserializeNBT(persistentData.getCompound("data"))
    }

    override fun saveAdditional(pTag: CompoundTag) {
        persistentData.put("data", data.serializeNBT())
        super.saveAdditional(pTag)
    }

    override fun getUpdateTag(): CompoundTag {
        return data.serializeNBT()
    }

    override fun handleUpdateTag(tag: CompoundTag) {
        data.deserializeNBT(tag)
    }

    override fun getUpdatePacket(): Packet<ClientGamePacketListener> {
        return ClientboundBlockEntityDataPacket.create(this)
    }

    override fun onDataPacket(net: Connection, pkt: ClientboundBlockEntityDataPacket) {
        pkt.tag?.let {
            handleUpdateTag(it)
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
}
