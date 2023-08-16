package eu.withoutaname.mod.binarycraft.block

import eu.withoutaname.mod.binarycraft.BinaryCraft
import net.minecraft.world.level.block.Block
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries
import thedarkcolour.kotlinforforge.forge.registerObject

object ModBlocks {
    val REGISTRY: DeferredRegister<Block> = DeferredRegister.create(ForgeRegistries.BLOCKS, BinaryCraft.ID)

    val WIRE by REGISTRY.registerObject("wire") { WireBlock }
}
