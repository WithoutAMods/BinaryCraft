package eu.withoutaname.mod.binarycraft.client

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonObject
import net.minecraft.client.renderer.block.model.ItemOverrides
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.resources.model.BakedModel
import net.minecraft.client.resources.model.Material
import net.minecraft.client.resources.model.ModelBaker
import net.minecraft.client.resources.model.ModelState
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.client.event.ModelEvent.RegisterGeometryLoaders
import net.minecraftforge.client.model.geometry.IGeometryBakingContext
import net.minecraftforge.client.model.geometry.IGeometryLoader
import net.minecraftforge.client.model.geometry.IUnbakedGeometry
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
            overrides: ItemOverrides,
            modelLocation: ResourceLocation
        ): BakedModel {
            return WireBakedModel(overrides)
        }
    }

    companion object {
        const val ID = "wire_loader"

        fun register(event: RegisterGeometryLoaders) {
            event.register(ID, WireModelLoader())
        }
    }
}
