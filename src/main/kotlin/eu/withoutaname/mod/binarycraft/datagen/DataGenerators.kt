package eu.withoutaname.mod.binarycraft.datagen

import net.minecraft.data.DataProvider
import net.neoforged.neoforge.data.event.GatherDataEvent
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS

object DataGenerators {

    fun init() {
        MOD_BUS.addListener(::gatherData)
    }

    private fun gatherData(event: GatherDataEvent) {
        val exFileHelper = event.existingFileHelper
        event.generator.run {
            addProvider(event.includeClient(), DataProvider.Factory { BlockStates(it, exFileHelper) })
        }
    }
}
