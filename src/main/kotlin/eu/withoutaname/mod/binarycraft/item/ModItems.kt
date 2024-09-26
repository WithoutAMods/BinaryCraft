package eu.withoutaname.mod.binarycraft.item

import eu.withoutaname.mod.binarycraft.BinaryCraft
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.Item
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import thedarkcolour.kotlinforforge.neoforge.forge.getValue

object ModItems {
    val REGISTRY: DeferredRegister<Item> = DeferredRegister.createItems(BinaryCraft.ID)
    val TAB_REGISTRY: DeferredRegister<CreativeModeTab> =
        DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB, BinaryCraft.ID)

    private val WIRE_HOLDERS = buildMap<DyeColor, DeferredHolder<Item, WireItem>> {
        DyeColor.entries.forEach { color ->
            put(color, REGISTRY.register("wire_${color.getName()}") { -> WireItem(color) })
        }
    }

    val WIRES: Map<DyeColor, WireItem>
        get() = WIRE_HOLDERS.mapValues { (_, holder) -> holder.get() }

    val BINARY_CRAFT_TAB: CreativeModeTab by TAB_REGISTRY.register(BinaryCraft.ID) { ->
        CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.${BinaryCraft.ID}"))
            .icon { WIRES.getValue(DyeColor.BLUE).defaultInstance }
            .displayItems { _, output ->
                output.acceptAll(WIRES.values.map { it.defaultInstance })
            }
            .build()
    }
}
