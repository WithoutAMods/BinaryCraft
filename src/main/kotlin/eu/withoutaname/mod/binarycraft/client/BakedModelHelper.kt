package eu.withoutaname.mod.binarycraft.client

import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.renderer.block.model.BakedQuad
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.core.Direction
import net.minecraft.util.FastColor.ARGB32
import net.minecraft.world.item.DyeColor
import net.minecraft.world.phys.Vec3
import net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer


class BakedModelHelper(var sprite: TextureAtlasSprite) {

    private val quads = mutableListOf<BakedQuad>()
    private val builder = QuadBakingVertexConsumer()
    private var colorR = 255
    private var colorG = 255
    private var colorB = 255
    private var colorA = 255

    fun addAll(quads: Collection<BakedQuad>) {
        this.quads.addAll(quads)
    }

    fun color(color: DyeColor) {
        color(color.textureDiffuseColor)
    }

    private fun color(argb32: Int) {
        color(ARGB32.red(argb32), ARGB32.green(argb32), ARGB32.blue(argb32), ARGB32.alpha(argb32))
    }

    fun color(r: Int, g: Int, b: Int, a: Int = 255) {
        colorR = r
        colorG = g
        colorB = b
        colorA = a
    }

    fun addCube(v1: Vec3, v2: Vec3, rotation: Int = 0) {
        val rot = rotation and 3 // modulo 4
        if (rot != 0) {
            when (rot) {
                1 -> addCube(v(1 - v2.z, v1.y, v1.x), v(1 - v1.z, v2.y, v2.x))
                2 -> addCube(v(1 - v2.x, v1.y, 1 - v2.z), v(1 - v1.x, v2.y, 1 - v1.z))
                3 -> addCube(v(v1.z, v1.y, 1 - v2.x), v(v2.z, v2.y, 1 - v1.x))
                else -> addCube(v1, v2)
            }
            return
        }

        val wbn = v(v1.x, v1.y, v1.z)
        val wbs = v(v1.x, v1.y, v2.z)
        val wtn = v(v1.x, v2.y, v1.z)
        val wts = v(v1.x, v2.y, v2.z)
        val ebn = v(v2.x, v1.y, v1.z)
        val ebs = v(v2.x, v1.y, v2.z)
        val etn = v(v2.x, v2.y, v1.z)
        val ets = v(v2.x, v2.y, v2.z)


        addQuad(wtn, wts, ets, etn) // top
        addQuad(ebn, ebs, wbs, wbn) // bottom
        addQuad(etn, ebn, wbn, wtn) // north
        addQuad(wts, wbs, ebs, ets) // south
        addQuad(wtn, wbn, wbs, wts) // west
        addQuad(ets, ebs, ebn, etn) // east
    }

    fun addQuad(v1: Vec3, v2: Vec3, v3: Vec3, v4: Vec3, rotation: Int = 0) {
        val rot = rotation and 3 // modulo 4
        if (rot != 0) {
            when (rot) {
                1 -> addQuad(v2, v3, v4, v1)
                2 -> addQuad(v3, v4, v1, v2)
                3 -> addQuad(v4, v1, v2, v3)
                else -> addQuad(v1, v2, v3, v4)
            }
            return
        }
        val normal = v3.subtract(v2).cross(v1.subtract(v2)).normalize()
        builder.setSprite(sprite)
        builder.setDirection(Direction.getNearest(normal.x, normal.y, normal.z))
        putVertex(builder, normal, v1.x, v1.y, v1.z, 0f, 0f)
        putVertex(builder, normal, v2.x, v2.y, v2.z, 0f, 1f)
        putVertex(builder, normal, v3.x, v3.y, v3.z, 1f, 1f)
        putVertex(builder, normal, v4.x, v4.y, v4.z, 1f, 0f)
        quads.add(builder.bakeQuad())
    }

    private fun putVertex(
        builder: VertexConsumer, normal: Vec3, x: Double, y: Double, z: Double, u: Float, v: Float
    ) {
        val iu = sprite.getU(u)
        val iv = sprite.getV(v)
        builder
            .addVertex(x.toFloat(), y.toFloat(), z.toFloat())
            .setUv(iu, iv)
            .setUv2(0, 0)
            .setColor(colorR, colorG, colorB, colorA)
            .setNormal(normal.x().toFloat(), normal.y().toFloat(), normal.z().toFloat())
    }

    fun v(x: Double, y: Double, z: Double): Vec3 {
        return Vec3(x, y, z)
    }

    companion object {
        fun buildQuads(sprite: TextureAtlasSprite, block: BakedModelHelper.() -> Unit): MutableList<BakedQuad> {
            val helper = BakedModelHelper(sprite)
            helper.block()
            return helper.quads
        }
    }
}
