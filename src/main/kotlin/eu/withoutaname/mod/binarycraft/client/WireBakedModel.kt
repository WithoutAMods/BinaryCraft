package eu.withoutaname.mod.binarycraft.client

import eu.withoutaname.mod.binarycraft.BinaryCraft
import eu.withoutaname.mod.binarycraft.block.WireBlock
import eu.withoutaname.mod.binarycraft.block.WireBlockData
import eu.withoutaname.mod.binarycraft.logic.ConnectionType
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.block.model.ItemOverrides
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.core.Direction
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.RandomSource
import net.minecraft.world.inventory.InventoryMenu
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.state.BlockState
import net.minecraftforge.client.model.IDynamicBakedModel
import net.minecraftforge.client.model.data.ModelData
import kotlin.math.max
import kotlin.math.min


private fun yOffset(level: Int) = level * 4 / 16.0
private const val cableThickness = .5 / 16.0
private const val cableSpacing = .25 / 16.0
private const val cableStart = .5 - (cableThickness * 16 + cableSpacing * 15) / 2
private fun cableOffset(i: Int) = cableStart + (cableThickness + cableSpacing) * i

class WireBakedModel(private val overrides: ItemOverrides) : IDynamicBakedModel {
    companion object {
        val blankTexture: ResourceLocation = ResourceLocation(BinaryCraft.ID, "block/blank")
        val blank: TextureAtlasSprite by lazy {
            Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(blankTexture)
        }
    }

    private val gridQuadsCache by lazy { Array(4) { getGridQuads(it) } }

    private fun getGridQuads(level: Int) = BakedModelHelper.buildQuads(blank) {
        val pillarWidth = 1 / 16.0
        val platformHeight = 4 / 16.0
        val platformThickness = .5 / 16.0
        val pillarHeight = platformHeight - platformThickness
        val offset = level * platformHeight

        addCube(v(.0, offset, .0), v(pillarWidth, offset + pillarHeight, pillarWidth))
        addCube(v(1 - pillarWidth, offset, .0), v(1.0, offset + pillarHeight, pillarWidth))
        addCube(v(.0, offset, 1 - pillarWidth), v(pillarWidth, offset + pillarHeight, 1.0))
        addCube(v(1 - pillarWidth, offset, 1 - pillarWidth), v(1.0, offset + pillarHeight, 1.0))

        addCube(v(.0, offset + pillarHeight, .0), v(1.0, offset + platformHeight, 1.0))
    }

    private fun BakedModelHelper.addWireQuads(level: Int, data: WireBlockData.LevelData) {
        if (data.isSimple) {
            addSimpleWireQuads(data, level)
        } else {
            addComplexWireQuads(data, level)
        }
    }

    private fun BakedModelHelper.addSimpleWireQuads(
        data: WireBlockData.LevelData, level: Int
    ) {
        val start = .5 - cableThickness / 2
        for (rot in 0..3) {
            val direction = Direction.from2DDataValue(rot)
            if (data.hasConnection(direction, ConnectionType.Simple)) {
                addCube(
                    v(start, yOffset(level), start),
                    v(start + cableThickness, yOffset(level) + cableThickness, 1.0),
                    rot
                )
            }
        }
    }

    private fun BakedModelHelper.addComplexWireQuads(
        data: WireBlockData.LevelData, level: Int
    ) {
        val north = data.sideData[Direction.NORTH.get2DDataValue()]
        val south = data.sideData[Direction.SOUTH.get2DDataValue()]
        val east = data.sideData[Direction.EAST.get2DDataValue()]
        val west = data.sideData[Direction.WEST.get2DDataValue()]

        for (i in 0..15) {
            color(DyeColor.byId(i))
            val offset = cableOffset(i)
            addNorthSouthWireQuads(
                offset,
                level,
                north hasBit i,
                south hasBit i,
                east hasBit i,
                west hasBit i
            )
            addEastWestWireQuads(i, offset, level, north, south, east, west)
        }
    }

    private fun BakedModelHelper.addNorthSouthWireQuads(
        offset: Double, level: Int, north: Boolean, south: Boolean, east: Boolean, west: Boolean
    ) {
        if (!north && !south) return

        val start = when {
            north -> .0
            east || west -> offset
            else -> 1 - cableStart
        }
        val end = when {
            south -> 1.0
            east || west -> offset + cableThickness
            else -> cableStart
        }
        addCube(
            v(offset, yOffset(level), start),
            v(offset + cableThickness, yOffset(level) + cableThickness, end)
        )
    }

    private fun BakedModelHelper.addEastWestWireQuads(
        i: Int, offset: Double, level: Int, north: Int, south: Int, east: Int, west: Int
    ) {
        if (!(east or west hasBit i)) return

        val firstIndex = when {
            west hasBit i -> -1
            north or south hasBit i -> i
            else -> 16
        }
        val lastIndex = when {
            east hasBit i -> 16
            north or south hasBit i -> i
            else -> -1
        }
        val fistObstruction = getFirstObstruction(north, south, east, west, firstIndex, lastIndex, i)
        val lastObstruction = getLastObstruction(north, south, east, west, firstIndex, lastIndex, i)

        val start = when (firstIndex) {
            -1 -> .0
            16 -> 1 - cableStart
            else -> cableOffset(firstIndex)
        }
        val end = when (lastIndex) {
            -1 -> cableStart
            16 -> 1.0
            else -> cableOffset(lastIndex) + cableThickness
        }

        if (fistObstruction == null || lastObstruction == null) {
            addCube(
                v(start, yOffset(level), offset),
                v(end, yOffset(level) + cableThickness, offset + cableThickness)
            )
            return
        }

        addCube(
            v(start, yOffset(level), offset),
            v(cableOffset(fistObstruction), yOffset(level) + cableThickness, offset + cableThickness)
        )
        addCube(
            v(cableOffset(fistObstruction) - cableThickness, yOffset(level) + cableThickness, offset),
            v(
                cableOffset(lastObstruction) + 2 * cableThickness,
                yOffset(level) + 2 * cableThickness,
                offset + cableThickness
            )
        )
        addCube(
            v(cableOffset(lastObstruction) + cableThickness, yOffset(level), offset),
            v(end, yOffset(level) + cableThickness, offset + cableThickness)
        )
    }

    private fun getFirstObstruction(
        north: Int,
        south: Int,
        east: Int,
        west: Int,
        firstIndex: Int,
        lastIndex: Int,
        i: Int
    ): Int? {
        val southAndAny = south and (north or east or west)
        for (j in firstIndex + 1..<min(i, lastIndex))
            if (southAndAny hasBit j) return j
        val northAndAny = north and (south or east or west)
        for (j in max(firstIndex, i) + 1..<lastIndex)
            if (northAndAny hasBit j) return j
        return null
    }

    private fun getLastObstruction(
        north: Int,
        south: Int,
        east: Int,
        west: Int,
        firstIndex: Int,
        lastIndex: Int,
        i: Int
    ): Int? {
        val northAndAny = north and (south or east or west)
        for (j in lastIndex - 1 downTo i + 1)
            if (northAndAny hasBit j) return j
        val southAndAny = south and (north or east or west)
        for (j in i - 1 downTo firstIndex + 1)
            if (southAndAny hasBit j) return j
        return null
    }

    private infix fun Int.hasBit(i: Int): Boolean {
        return this and (1 shl i) != 0
    }

    override fun getQuads(
        state: BlockState?, side: Direction?, rand: RandomSource, extraData: ModelData, renderType: RenderType?
    ) = BakedModelHelper.buildQuads(blank) {
        if (side != null || renderType != RenderType.solid()) return@buildQuads

        val data = extraData[WireBlock.DATA] ?: WireBlockData()

        for (i in 0 until if (data.wireAbove) 4 else data.level - 1) {
            addAll(gridQuadsCache[i])
        }
        for (i in 0 until data.level) {
            addWireQuads(i, data.levelData[i])
        }
    }

    override fun getRenderTypes(itemStack: ItemStack, fabulous: Boolean) =
        mutableListOf(RenderType.solid(), RenderType.cutout())

    override fun useAmbientOcclusion() = true

    @Deprecated("Deprecated in Java")
    override fun getParticleIcon() = blank

    override fun isGui3d() = false

    override fun usesBlockLight() = false

    override fun isCustomRenderer() = false

    override fun getOverrides() = overrides
}
