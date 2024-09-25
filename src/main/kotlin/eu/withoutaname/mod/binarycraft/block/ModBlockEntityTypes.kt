package eu.withoutaname.mod.binarycraft.block

import eu.withoutaname.mod.binarycraft.BinaryCraft
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.level.block.entity.BlockEntityType
import net.neoforged.neoforge.registries.DeferredRegister
import thedarkcolour.kotlinforforge.neoforge.forge.getValue

object ModBlockEntityTypes {
    val REGISTRY: DeferredRegister<BlockEntityType<*>> =
        DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, BinaryCraft.ID)

    val WIRE: BlockEntityType<WireBlockEntity> by REGISTRY.register("wire") {->
        BlockEntityType.Builder.of(::WireBlockEntity, ModBlocks.WIRE).build(null)
    }
}
