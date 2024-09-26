package eu.withoutaname.mod.binarycraft.datagen

import eu.withoutaname.mod.binarycraft.BinaryCraft
import eu.withoutaname.mod.binarycraft.block.ModBlocks
import eu.withoutaname.mod.binarycraft.item.ModItems
import net.minecraft.data.PackOutput
import net.minecraft.network.chat.Component
import net.neoforged.neoforge.common.data.LanguageProvider

class Language(output: PackOutput) : LanguageProvider(output, BinaryCraft.ID, "en_us") {
    override fun addTranslations() {
        add(ModBlocks.WIRE, "Wire")
        ModItems.WIRES.forEach { (color, item) ->
            add(item, "%s Wire".format(Component.translatable("color.minecraft.${color.getName()}").string))
        }
        add("itemGroup.${BinaryCraft.ID}", "BinaryCraft")
    }
}
