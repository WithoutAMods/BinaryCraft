package eu.withoutaname.mod.binarycraft.client

import net.neoforged.neoforge.client.event.ModelEvent
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS

object ClientSetup {

    fun init() {
        MOD_BUS.addListener(::modelInit)
    }

    private fun modelInit(event: ModelEvent.RegisterGeometryLoaders) {
        WireModelLoader.register(event)
    }
}
