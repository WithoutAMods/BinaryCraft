package eu.withoutaname.mod.binarycraft.logic.api

interface Gate {
    val inputCount: Int
    val outputCount: Int

    fun update(inputStates: List<State>): List<State>
}
