package eu.withoutaname.mod.binarycraft.block

import eu.withoutaname.mod.binarycraft.BinaryCraft
import net.neoforged.neoforge.registries.DeferredRegister
import thedarkcolour.kotlinforforge.neoforge.forge.getValue

object ModBlocks {
    val REGISTRY: DeferredRegister.Blocks = DeferredRegister.createBlocks(BinaryCraft.ID)

    val WIRE: WireBlock by REGISTRY.register("wire") { -> WireBlock }
}
