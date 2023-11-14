package eu.withoutaname.mod.binarycraft.datagen

import eu.withoutaname.mod.binarycraft.BinaryCraft
import eu.withoutaname.mod.binarycraft.block.ModBlocks
import eu.withoutaname.mod.binarycraft.client.WireModelLoader
import net.minecraft.data.PackOutput
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.client.model.generators.BlockModelBuilder
import net.minecraftforge.client.model.generators.BlockStateProvider
import net.minecraftforge.client.model.generators.CustomLoaderBuilder
import net.minecraftforge.common.data.ExistingFileHelper

class BlockStates(output: PackOutput, exFileHelper: ExistingFileHelper) :
    BlockStateProvider(output, BinaryCraft.ID, exFileHelper) {

    override fun registerStatesAndModels() {
        simpleBlock(
            ModBlocks.WIRE,
            models().getBuilder("wire").customLoader { builder, exFileHelper ->
                object : CustomLoaderBuilder<BlockModelBuilder>(
                    ResourceLocation(BinaryCraft.ID, WireModelLoader.ID),
                    builder,
                    exFileHelper
                ) {}
            }.end()
        )
    }
}
