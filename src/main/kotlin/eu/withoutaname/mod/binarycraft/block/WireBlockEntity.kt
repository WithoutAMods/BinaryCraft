package eu.withoutaname.mod.binarycraft.block

import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

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
}
