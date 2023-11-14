package eu.withoutaname.mod.binarycraft.block

import eu.withoutaname.mod.binarycraft.logic.ConnectionType
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraftforge.common.util.INBTSerializable


class WireBlockData : INBTSerializable<CompoundTag> {
    var level = 1
        private set
    val levelData = Array(4) { LevelData() }
    var wireAbove = false
        private set
    var neighbourHeights = IntArray(4)
        private set

    fun addLevel() {
        if (level == 4) throw IllegalStateException("Cannot add more than 4 levels")
        level++
    }

    fun removeLevel() {
        if (level == 1) throw IllegalStateException("Cannot remove level 1")
        levelData[--level].clear()
    }

    fun hasConnection(level: Int, side: Direction, connectionType: ConnectionType): Boolean {
        return levelData[level].hasConnection(side, connectionType)
    }

    fun setConnection(level: Int, side: Direction, connectionType: ConnectionType, value: Boolean) {
        levelData[level].setConnection(side, connectionType, value)
    }

    override fun serializeNBT(): CompoundTag {
        val tag = CompoundTag()
        tag.putInt("level", level)
        val levelDataTag = ListTag()
        for (level in levelData) {
            levelDataTag.add(level.serializeNBT())
        }
        tag.put("levelData", levelDataTag)
        return tag
    }

    override fun deserializeNBT(nbt: CompoundTag) {
        level = nbt.getInt("level").coerceAtLeast(1).coerceAtMost(4)
        val levelDataTag = nbt.getList("levelData", 10)
        for (i in 0 until minOf(levelDataTag.size, levelData.size)) {
            levelData[i].deserializeNBT(levelDataTag.getCompound(i))
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

        override fun serializeNBT(): CompoundTag {
            val tag = CompoundTag()
            tag.putBoolean("isSimple", isSimple)
            tag.putIntArray("sideData", sideData)
            return tag
        }

        override fun deserializeNBT(nbt: CompoundTag) {
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
