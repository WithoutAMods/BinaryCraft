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
        val offsetY = level * 4 / 16.0
        val cableThickness = .5 / 16.0

        if (data.isSimple) {
            val start = .5 - cableThickness / 2
            for (rot in 0..3) {
                val direction = Direction.from2DDataValue(rot)
                if (data.hasConnection(direction, ConnectionType.Simple)) {
                    addCube(v(start, offsetY, start), v(start + cableThickness, offsetY + cableThickness, 1.0), rot)
                }
            }
        } else {
            val cableSpacing = .25 / 16.0
            val cableStart = .5 - (cableThickness * 16 + cableSpacing + 15) / 2
            fun cableOffset(i: Int) = cableStart + (cableThickness + cableSpacing) * i

            val north = data.sideData[Direction.NORTH.get2DDataValue()]
            val south = data.sideData[Direction.SOUTH.get2DDataValue()]
            val east = data.sideData[Direction.EAST.get2DDataValue()]
            val west = data.sideData[Direction.WEST.get2DDataValue()]
            val northAndSouth = north and south
            val northOrSouth = north or south
            val eastAndWest = east and west
            val eastOrWest = east or west
            val northOrSouthAndEastOrWest = northOrSouth and eastOrWest

            var lastNorth = -1
            var firstSouth = 16
            val northAndAny = north and (south or eastOrWest)
            for (i in 15 downTo 0) {
                if (northAndAny and (1 shl i) == 1) {
                    lastNorth = i
                    break
                }
            }
            val southAndAny = south and (north or eastOrWest)
            for (i in 0..15) {
                if (southAndAny and (1 shl i) == 1) {
                    firstSouth = i
                    break
                }
            }

            for (i in 0..15) {
                color(DyeColor.byId(i))
                val offset = cableOffset(i)
                val mask = 1 shl i
                if (northAndSouth and mask == 1) {
                    addCube(v(offset, offsetY, .0), v(offset + cableThickness, offsetY + cableThickness, 1.0))
                } else {
                    if (northOrSouthAndEastOrWest and mask == 1) {
                        if (north and mask == 1) {
                            addCube(
                                v(offset, offsetY, .0),
                                v(offset + cableThickness, offsetY + cableThickness, offset + cableThickness)
                            )
                        } else {
                            addCube(
                                v(offset, offsetY, offset),
                                v(offset + cableThickness, offsetY + cableThickness, 1.0)
                            )
                        }
                    } else {
                        if (north and mask == 1) {
                            addCube(
                                v(offset, offsetY, .0),
                                v(offset + cableThickness, offsetY + cableThickness, cableStart)
                            )
                        } else if (south and mask == 1) {
                            addCube(
                                v(offset, offsetY, 1 - cableStart),
                                v(offset + cableThickness, offsetY + cableThickness, 1.0)
                            )
                        }
                    }
                }
                val isWest = west and mask == 1
                val isEast = east and mask == 1
                if (isWest) {
                    addCube(
                        v(.0, offsetY, offset),
                        v(
                            if (firstSouth < i) cableOffset(firstSouth) else offset,
                            offsetY + cableThickness,
                            offset + cableThickness
                        )
                    )
                }
                if (isEast) {
                    addCube(
                        v(
                            (if (lastNorth > i) cableOffset(lastNorth) else offset) + cableThickness,
                            offsetY,
                            offset
                        ),
                        v(1.0, offsetY + cableThickness, offset + cableThickness)
                    )
                }
                if (isWest && firstSouth < i || isEast && lastNorth > i) {
                    addCube(
                        v(
                            if (isWest && firstSouth < i) cableOffset(firstSouth) - cableThickness else offset,
                            offsetY,
                            offset
                        ),
                        v(
                            (if (isEast && lastNorth > i) cableOffset(lastNorth) + cableThickness else offset) + cableThickness,
                            offsetY + cableThickness,
                            offset + cableThickness
                        )
                    )
                }
            }
        }
    }

    override fun getQuads(
        state: BlockState?,
        side: Direction?,
        rand: RandomSource,
        extraData: ModelData,
        renderType: RenderType?
    ) = BakedModelHelper.buildQuads(blank) {
        if (side != null || renderType != RenderType.solid()) return@buildQuads

        val data = extraData[WireBlock.DATA] ?: WireBlockData()

        for (i in 0 until data.level - 1) {
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
