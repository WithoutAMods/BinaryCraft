package eu.withoutaname.mod.binarycraft

import eu.withoutaname.mod.binarycraft.block.ModBlockEntityTypes
import eu.withoutaname.mod.binarycraft.block.ModBlocks
import net.minecraftforge.fml.common.Mod
import thedarkcolour.kotlinforforge.forge.MOD_BUS

@Mod(BinaryCraft.ID)
object BinaryCraft {
    const val ID = "binarycraft"

    init {
        ModBlocks.REGISTRY.register(MOD_BUS)
        ModBlockEntityTypes.REGISTRY.register(MOD_BUS)
    }
}
