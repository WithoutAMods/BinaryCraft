package eu.withoutaname.mod.binarycraft.block

import eu.withoutaname.mod.binarycraft.logic.ConnectionType
import net.minecraft.core.Direction
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.neoforged.neoforge.common.util.INBTSerializable


class WireBlockData : INBTSerializable<CompoundTag> {
    var numLevels = 1
        private set
    var wireAbove = false
    var neighbourLevels = IntArray(4)
        private set
    val levelData = Array(4) { LevelData() }

    fun addLevel() {
        if (numLevels == 4) throw IllegalStateException("Cannot add more than 4 levels")
        numLevels++
    }

    fun removeLevel() {
        if (numLevels == 1) throw IllegalStateException("Cannot remove level 1")
        levelData[--numLevels].clear()
    }

    fun hasConnection(level: Int, side: Direction, connectionType: ConnectionType): Boolean {
        return levelData[level].hasConnection(side, connectionType)
    }

    fun setConnection(level: Int, side: Direction, connectionType: ConnectionType, value: Boolean) {
        levelData[level].setConnection(side, connectionType, value)
    }

    fun addConnection(level: Int, side: Direction, connectionType: ConnectionType): Boolean {
        if (levelData[level].hasConnection(side, connectionType)) return false
        levelData[level].setConnection(side, connectionType, true)
        return true
    }

    override fun serializeNBT(registries: HolderLookup.Provider): CompoundTag {
        val tag = CompoundTag()
        tag.putInt("numLevels", numLevels)
        tag.putBoolean("wireAbove", wireAbove)
        tag.putIntArray("neighbourLevels", neighbourLevels)
        val levelDataTag = ListTag()
        for (level in levelData) {
            levelDataTag.add(level.serializeNBT(registries))
        }
        tag.put("levelData", levelDataTag)
        return tag
    }

    override fun deserializeNBT(registries: HolderLookup.Provider, nbt: CompoundTag) {
        numLevels = nbt.getInt("numLevels").coerceAtLeast(1).coerceAtMost(4)
        wireAbove = nbt.getBoolean("wireAbove")
        val neighbourHeightsNew = nbt.getIntArray("neighbourLevels")
        if (neighbourHeightsNew.size == 4) {
            neighbourLevels = neighbourHeightsNew
        } else {
            for (i in 0 until minOf(neighbourHeightsNew.size, 4)) {
                neighbourLevels[i] = neighbourHeightsNew[i]
            }
            for (i in neighbourHeightsNew.size until 4) {
                neighbourLevels[i] = 1
            }
        }
        val levelDataTag = nbt.getList("levelData", 10)
        for (i in 0 until minOf(levelDataTag.size, levelData.size)) {
            levelData[i].deserializeNBT(registries, levelDataTag.getCompound(i))
        }
        for (i in levelDataTag.size until levelData.size) {
            levelData[i].clear()
        }
    }

    class LevelData : INBTSerializable<CompoundTag> {
        var isSimple = false
            private set
        var sideData = IntArray(4)
            private set

        fun clear() {
            isSimple = false
            sideData = IntArray(4)
        }

        fun hasConnection(direction: Direction, connectionType: ConnectionType): Boolean {
            val value = sideData[direction.get2DDataValue()]
            return when (connectionType) {
                is ConnectionType.Simple -> isSimple && value == 1
                is ConnectionType.Colored -> !isSimple && value shr connectionType.color.id and 1 == 1
            }
        }

        fun setConnection(direction: Direction, connectionType: ConnectionType, value: Boolean) {
            val oldValue = sideData[direction.get2DDataValue()]
            val shouldBeSimple = connectionType is ConnectionType.Simple
            if (isSimple != shouldBeSimple && oldValue != 0) {
                throw IllegalStateException("Cannot combine simple and colored connections")
            }
            val newValue = when (connectionType) {
                is ConnectionType.Simple -> if (value) 1 else 0
                is ConnectionType.Colored -> {
                    val mask = 1 shl connectionType.color.id
                    if (value) oldValue or mask else oldValue and mask.inv()
                }
            }
            sideData[direction.get2DDataValue()] = newValue
            isSimple = shouldBeSimple
        }

        override fun serializeNBT(registries: HolderLookup.Provider): CompoundTag {
            val tag = CompoundTag()
            tag.putBoolean("isSimple", isSimple)
            tag.putIntArray("sideData", sideData)
            return tag
        }

        override fun deserializeNBT(registries: HolderLookup.Provider, nbt: CompoundTag) {
            isSimple = nbt.getBoolean("isSimple")
            val intArray = nbt.getIntArray("sideData")
            if (intArray.size == 4) {
                sideData = intArray
            } else {
                for (i in 0 until minOf(intArray.size, sideData.size)) {
                    sideData[i] = intArray[i]
                }
                for (i in intArray.size until sideData.size) {
                    sideData[i] = 0
                }
            }
        }
    }
}
