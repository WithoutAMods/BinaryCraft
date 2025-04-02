package eu.withoutaname.mod.binarycraft.datagen

import eu.withoutaname.mod.binarycraft.BinaryCraft
import eu.withoutaname.mod.binarycraft.block.ModBlocks
import eu.withoutaname.mod.binarycraft.client.WireModelLoader
import eu.withoutaname.mod.binarycraft.util.cableThickness
import net.minecraft.data.PackOutput
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder
import net.neoforged.neoforge.client.model.generators.BlockStateProvider
import net.neoforged.neoforge.client.model.generators.CustomLoaderBuilder
import net.neoforged.neoforge.common.data.ExistingFileHelper

class BlockStates(output: PackOutput, exFileHelper: ExistingFileHelper) :
    BlockStateProvider(output, BinaryCraft.ID, exFileHelper) {

    override fun registerStatesAndModels() {
        simpleBlock(
            ModBlocks.WIRE,
            models().getBuilder("wire").customLoader { builder, exFileHelper ->
                object : CustomLoaderBuilder<BlockModelBuilder>(
                    WireModelLoader.resourceLocation,
                    builder,
                    exFileHelper,
                    false
                ) {}
            }.end()
        )
        val start = 8f - cableThickness.toFloat() * 16
        val end = 8f + cableThickness.toFloat() * 16
        models().withExistingParent("wire_inventory", mcLoc("block/block"))
            .element()
            .from(start, start, 0f)
            .to(end, end, 16f)
            .textureAll("#texture")
            .end()
    }
}
