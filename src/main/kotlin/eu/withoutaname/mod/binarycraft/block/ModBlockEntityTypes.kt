package eu.withoutaname.mod.binarycraft.block

import eu.withoutaname.mod.binarycraft.BinaryCraft
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries
import thedarkcolour.kotlinforforge.forge.registerObject

object ModBlockEntityTypes {
    val REGISTRY: DeferredRegister<BlockEntityType<*>> =
        DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, BinaryCraft.ID)

    val WIRE: BlockEntityType<WireBlockEntity> by REGISTRY.registerObject("wire") {
        BlockEntityType.Builder.of(::WireBlockEntity, ModBlocks.WIRE).build(null)
    }
}
