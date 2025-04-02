package eu.withoutaname.mod.binarycraft.util

const val cableThickness = .5 / 16.0
const val cableSpacing = .25 / 16.0
const val cableStart = .5 - (cableThickness * 16 + cableSpacing * 15) / 2
fun horizontalCableOffset(id: Int) = cableStart + (cableThickness + cableSpacing) * id

const val pillarWidth = 1 / 16.0
const val platformHeight = 4 / 16.0
const val platformThickness = .5 / 16.0
const val pillarHeight = platformHeight - platformThickness
fun verticalCableOffset(level: Int) = level * platformHeight
