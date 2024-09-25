package eu.withoutaname.mod.binarycraft.client

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonObject
import eu.withoutaname.mod.binarycraft.BinaryCraft
import net.minecraft.client.renderer.block.model.ItemOverrides
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.resources.model.BakedModel
import net.minecraft.client.resources.model.Material
import net.minecraft.client.resources.model.ModelBaker
import net.minecraft.client.resources.model.ModelState
import net.minecraft.resources.ResourceLocation
import net.neoforged.neoforge.client.event.ModelEvent
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry
import java.util.function.Function

class WireModelLoader : IGeometryLoader<WireModelLoader.WireModelGeometry> {

    override fun read(jsonObject: JsonObject, deserializationContext: JsonDeserializationContext): WireModelGeometry {
        return WireModelGeometry()
    }

    class WireModelGeometry : IUnbakedGeometry<WireModelGeometry> {
        override fun bake(
            context: IGeometryBakingContext,
            baker: ModelBaker,
            spriteGetter: Function<Material, TextureAtlasSprite>,
            modelState: ModelState,
            overrides: ItemOverrides
        ): BakedModel {
            return WireBakedModel(overrides)
        }
    }

    companion object {
        val resourceLocation: ResourceLocation = ResourceLocation.fromNamespaceAndPath(BinaryCraft.ID, "wire_loader")

        fun register(event: ModelEvent.RegisterGeometryLoaders) {
            event.register(resourceLocation, WireModelLoader())
        }
    }
}
