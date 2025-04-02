package eu.withoutaname.mod.binarycraft.datagen

import eu.withoutaname.mod.binarycraft.BinaryCraft
import eu.withoutaname.mod.binarycraft.item.ModItems
import net.minecraft.data.PackOutput
import net.neoforged.neoforge.client.model.generators.ItemModelProvider
import net.neoforged.neoforge.common.data.ExistingFileHelper

class ItemModels(output: PackOutput, exFileHelper: ExistingFileHelper) :
    ItemModelProvider(output, BinaryCraft.ID, exFileHelper) {

    override fun registerModels() {
        for ((color, item) in ModItems.WIRES) {
            withExistingParent(item.toString(), modLoc("block/wire_inventory"))
                .texture("texture", modLoc("block/color/${color.getName()}"))
        }
    }
}
