package eu.withoutaname.mod.binarycraft.client

import net.minecraftforge.client.event.ModelEvent.RegisterGeometryLoaders
import thedarkcolour.kotlinforforge.forge.MOD_BUS

object ClientSetup {

    fun init() {
        MOD_BUS.addListener(::modelInit)
    }

    private fun modelInit(event: RegisterGeometryLoaders) {
        WireModelLoader.register(event)
    }
}
