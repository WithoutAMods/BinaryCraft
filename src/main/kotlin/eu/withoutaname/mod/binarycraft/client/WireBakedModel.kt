package eu.withoutaname.mod.binarycraft.client

import eu.withoutaname.mod.binarycraft.BinaryCraft
import eu.withoutaname.mod.binarycraft.block.WireBlock
import eu.withoutaname.mod.binarycraft.block.WireBlockData
import eu.withoutaname.mod.binarycraft.logic.ConnectionType
import eu.withoutaname.mod.binarycraft.util.*
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.block.model.BakedQuad
import net.minecraft.client.renderer.block.model.ItemOverrides
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.core.Direction
import net.minecraft.core.Direction.*
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.RandomSource
import net.minecraft.world.inventory.InventoryMenu
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.neoforge.client.ChunkRenderTypeSet
import net.neoforged.neoforge.client.model.IDynamicBakedModel
import net.neoforged.neoforge.client.model.data.ModelData
import kotlin.math.max
import kotlin.math.min

class WireBakedModel(private val overrides: ItemOverrides) : IDynamicBakedModel {
    companion object {
        private val blankTexture = ResourceLocation.fromNamespaceAndPath(BinaryCraft.ID, "block/blank")
        private val gridTexture = ResourceLocation.fromNamespaceAndPath(BinaryCraft.ID, "block/grid")
        val blank: TextureAtlasSprite by lazy {
            Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(blankTexture)
        }
        val grid: TextureAtlasSprite by lazy {
            Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(gridTexture)
        }
    }

    private val gridQuadsCache by lazy { Array(4) { getGridQuads(it) } }
    private val gridQuadsSideCache by lazy { Array(4) { getGridQuadsSides(it) } }

    private fun getGridQuads(level: Int) = BakedModelHelper.buildQuads(grid) {
        val offset = verticalCableOffset(level)

        gridColor()

        // top
        addQuad(
            v(.0, offset + platformHeight, .0),
            v(.0, offset + platformHeight, 1.0),
            v(1.0, offset + platformHeight, 1.0),
            v(1.0, offset + platformHeight, .0)
        )
        // bottom
        addQuad(
            v(.0, offset + pillarHeight, 1.0),
            v(.0, offset + pillarHeight, .0),
            v(1.0, offset + pillarHeight, .0),
            v(1.0, offset + pillarHeight, 1.0)
        )

        sprite = blank
        // pillar nw face s
        addQuad(
            v(.0, offset + pillarHeight, pillarWidth),
            v(.0, offset, pillarWidth),
            v(pillarWidth, offset, pillarWidth),
            v(pillarWidth, offset + pillarHeight, pillarWidth)
        )
        // pillar nw face e
        addQuad(
            v(pillarWidth, offset + pillarHeight, pillarWidth),
            v(pillarWidth, offset, pillarWidth),
            v(pillarWidth, offset, .0),
            v(pillarWidth, offset + pillarHeight, .0)
        )
        // pillar sw face n
        addQuad(
            v(pillarWidth, offset + pillarHeight, 1 - pillarWidth),
            v(pillarWidth, offset, 1 - pillarWidth),
            v(.0, offset, 1 - pillarWidth),
            v(.0, offset + pillarHeight, 1 - pillarWidth)
        )
        // pillar sw face e
        addQuad(
            v(pillarWidth, offset + pillarHeight, 1.0),
            v(pillarWidth, offset, 1.0),
            v(pillarWidth, offset, 1 - pillarWidth),
            v(pillarWidth, offset + pillarHeight, 1 - pillarWidth)
        )
        // pillar se face n
        addQuad(
            v(1.0, offset + pillarHeight, 1 - pillarWidth),
            v(1.0, offset, 1 - pillarWidth),
            v(1 - pillarWidth, offset, 1 - pillarWidth),
            v(1 - pillarWidth, offset + pillarHeight, 1 - pillarWidth)
        )
        // pillar se face w
        addQuad(
            v(1 - pillarWidth, offset + pillarHeight, 1 - pillarWidth),
            v(1 - pillarWidth, offset, 1 - pillarWidth),
            v(1 - pillarWidth, offset, 1.0),
            v(1 - pillarWidth, offset + pillarHeight, 1.0)
        )
        // pillar ne face s
        addQuad(
            v(1 - pillarWidth, offset + pillarHeight, pillarWidth),
            v(1 - pillarWidth, offset, pillarWidth),
            v(1.0, offset, pillarWidth),
            v(1.0, offset + pillarHeight, pillarWidth)
        )
        // pillar ne face w
        addQuad(
            v(1 - pillarWidth, offset + pillarHeight, .0),
            v(1 - pillarWidth, offset, .0),
            v(1 - pillarWidth, offset, pillarWidth),
            v(1 - pillarWidth, offset + pillarHeight, pillarWidth)
        )
    }

    private fun getGridQuadsSides(level: Int): Array<MutableList<BakedQuad>> {
        val offset = verticalCableOffset(level)

        return arrayOf(
            BakedModelHelper.buildQuads(blank) {
                // south
                gridColor()

                // left
                addQuad(
                    v(.0, offset + pillarHeight, 1.0),
                    v(.0, offset, 1.0),
                    v(pillarWidth, offset, 1.0),
                    v(pillarWidth, offset + pillarHeight, 1.0)
                )

                // top
                addQuad(
                    v(.0, offset + platformHeight, 1.0),
                    v(.0, offset + pillarHeight, 1.0),
                    v(1.0, offset + pillarHeight, 1.0),
                    v(1.0, offset + platformHeight, 1.0),
                )

                // right
                addQuad(
                    v(1 - pillarWidth, offset + pillarHeight, 1.0),
                    v(1 - pillarWidth, offset, 1.0),
                    v(1.0, offset, 1.0),
                    v(1.0, offset + pillarHeight, 1.0)
                )
            },
            BakedModelHelper.buildQuads(blank) {
                // west
                gridColor()

                // left
                addQuad(
                    v(.0, offset + pillarHeight, .0),
                    v(.0, offset, .0),
                    v(.0, offset, pillarWidth),
                    v(.0, offset + pillarHeight, pillarWidth)
                )

                // top
                addQuad(
                    v(.0, offset + platformHeight, .0),
                    v(.0, offset + pillarHeight, .0),
                    v(.0, offset + pillarHeight, 1.0),
                    v(.0, offset + platformHeight, 1.0),
                )

                // right
                addQuad(
                    v(.0, offset + pillarHeight, 1 - pillarWidth),
                    v(.0, offset, 1 - pillarWidth),
                    v(.0, offset, 1.0),
                    v(.0, offset + pillarHeight, 1.0)
                )
            },
            BakedModelHelper.buildQuads(blank) {
                // north
                gridColor()

                // left
                addQuad(
                    v(1.0, offset + pillarHeight, .0),
                    v(1.0, offset, .0),
                    v(1 - pillarWidth, offset, .0),
                    v(1 - pillarWidth, offset + pillarHeight, .0)
                )

                // top
                addQuad(
                    v(1.0, offset + platformHeight, .0),
                    v(1.0, offset + pillarHeight, .0),
                    v(.0, offset + pillarHeight, .0),
                    v(.0, offset + platformHeight, .0),
                )

                // right
                addQuad(
                    v(pillarWidth, offset + pillarHeight, .0),
                    v(pillarWidth, offset, .0),
                    v(.0, offset, .0),
                    v(.0, offset + pillarHeight, .0)
                )
            },
            BakedModelHelper.buildQuads(blank) {
                // east
                gridColor()

                // left
                addQuad(
                    v(1.0, offset + pillarHeight, 1.0),
                    v(1.0, offset, 1.0),
                    v(1.0, offset, 1 - pillarWidth),
                    v(1.0, offset + pillarHeight, 1 - pillarWidth)
                )

                // top
                addQuad(
                    v(1.0, offset + platformHeight, 1.0),
                    v(1.0, offset + pillarHeight, 1.0),
                    v(1.0, offset + pillarHeight, .0),
                    v(1.0, offset + platformHeight, .0),
                )

                // right
                addQuad(
                    v(1.0, offset + pillarHeight, pillarWidth),
                    v(1.0, offset, pillarWidth),
                    v(1.0, offset, .0),
                    v(1.0, offset + pillarHeight, .0)
                )
            }
        )
    }

    private fun BakedModelHelper.gridColor() {
        color(160, 160, 160)
    }

    override fun getQuads(
        state: BlockState?, side: Direction?, rand: RandomSource, extraData: ModelData, renderType: RenderType?
    ) = BakedModelHelper.buildQuads(blank) {
        if (side != null && renderType == RenderType.cutout()) return@buildQuads

        val data = extraData[WireBlock.DATA] ?: WireBlockData()

        if (renderType == RenderType.cutout()) {
            for (i in 0 until if (data.wireAbove) 4 else data.numLevels - 1) {
                addAll(gridQuadsCache[i])
            }
            return@buildQuads
        }

        when (side) {
            DOWN -> {}
            UP -> {}
            null -> for (i in 0 until data.numLevels) addWireQuads(i, data.levelData[i], data.neighbourLevels)
            else -> {
                for (i in 0 until if (data.wireAbove) 4 else data.numLevels - 1) {
                    val rotation = side.get2DDataValue()
                    if (data.neighbourLevels[rotation] <= i) {
                        addAll(gridQuadsSideCache[i][rotation])
                    }
                }
            }
        }
    }

    private fun BakedModelHelper.addWireQuads(level: Int, data: WireBlockData.LevelData, neighbourLevels: IntArray) {
        if (data.isSimple) {
            addSimpleWireQuads(data, level, neighbourLevels)
        } else {
            addComplexWireQuads(data, level, neighbourLevels)
        }
    }

    private fun BakedModelHelper.addSimpleWireQuads(
        data: WireBlockData.LevelData, level: Int, neighbourLevels: IntArray
    ) {
        color(DyeColor.RED)
        val start = .5 - cableThickness / 2
        for (rot in 0..3) {
            val direction = from2DDataValue(rot)
            if (data.hasConnection(direction, ConnectionType.Simple)) {
                addCube(
                    v(start, verticalCableOffset(level), start),
                    v(start + cableThickness, verticalCableOffset(level) + cableThickness, 1.0),
                    rot
                )
                val neighbourLevel = neighbourLevels[rot]
                if (neighbourLevel < level) {
                    addCube(
                        v(start, verticalCableOffset(neighbourLevel), 1.0),
                        v(start + cableThickness, verticalCableOffset(level) + cableThickness, 1 + cableThickness),
                        rot
                    )
                }
            }
        }
    }

    private fun BakedModelHelper.addComplexWireQuads(
        data: WireBlockData.LevelData, level: Int, neighbourLevels: IntArray
    ) {
        val north = data.sideData[NORTH.get2DDataValue()]
        val south = data.sideData[SOUTH.get2DDataValue()]
        val east = data.sideData[EAST.get2DDataValue()]
        val west = data.sideData[WEST.get2DDataValue()]

        for (i in 0..15) {
            color(DyeColor.byId(i))
            val offset = horizontalCableOffset(i)
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
        addVerticalWireQuads(level, south, neighbourLevels[0], 0)
        addVerticalWireQuads(level, west, neighbourLevels[1], 1)
        addVerticalWireQuads(level, north, neighbourLevels[2], 2)
        addVerticalWireQuads(level, east, neighbourLevels[3], 3)
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
        val yOffset = verticalCableOffset(level)
        addCube(
            v(offset, yOffset, start),
            v(offset + cableThickness, yOffset + cableThickness, end)
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
            else -> horizontalCableOffset(firstIndex)
        }
        val end = when (lastIndex) {
            -1 -> cableStart
            16 -> 1.0
            else -> horizontalCableOffset(lastIndex) + cableThickness
        }

        val yOffset = verticalCableOffset(level)
        if (fistObstruction == null || lastObstruction == null) {
            addCube(
                v(start, yOffset, offset),
                v(end, yOffset + cableThickness, offset + cableThickness)
            )
            return
        }

        addCube(
            v(start, yOffset, offset),
            v(horizontalCableOffset(fistObstruction), yOffset + cableThickness, offset + cableThickness)
        )
        addCube(
            v(horizontalCableOffset(fistObstruction) - cableThickness, yOffset + cableThickness, offset),
            v(
                horizontalCableOffset(lastObstruction) + 2 * cableThickness,
                yOffset + 2 * cableThickness,
                offset + cableThickness
            )
        )
        addCube(
            v(horizontalCableOffset(lastObstruction) + cableThickness, yOffset, offset),
            v(end, yOffset + cableThickness, offset + cableThickness)
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

    private fun BakedModelHelper.addVerticalWireQuads(level: Int, data: Int, neighbourLevel: Int, rotation: Int) {
        if (neighbourLevel >= level) return

        for (i in 0..15) {
            if (!(data hasBit i)) continue

            color(DyeColor.byId(i))
            val offset = horizontalCableOffset(if (rotation < 2) i else 15 - i)
            addCube(
                v(offset, verticalCableOffset(neighbourLevel), 1.0),
                v(offset + cableThickness, verticalCableOffset(level) + cableThickness, 1 + cableThickness),
                rotation
            )
        }
    }

    private infix fun Int.hasBit(i: Int) = this and (1 shl i) != 0

    override fun getRenderTypes(itemStack: ItemStack, fabulous: Boolean) =
        mutableListOf(RenderType.solid(), RenderType.cutout())

    override fun getRenderTypes(state: BlockState, rand: RandomSource, data: ModelData): ChunkRenderTypeSet =
        ChunkRenderTypeSet.of(RenderType.solid(), RenderType.cutout())

    override fun useAmbientOcclusion() = true

    @Deprecated("Deprecated in Java")
    override fun getParticleIcon() = blank

    override fun isGui3d() = false

    override fun usesBlockLight() = false

    override fun isCustomRenderer() = false

    override fun getOverrides() = overrides
}
