package eu.withoutaname.mod.binarycraft.logic

abstract class GateBehavior {
    abstract val inputCount: Int
    abstract val outputCount: Int
    var updateTrigger: (() -> Unit) = {}
    abstract fun calculateOutputs(inputStates: List<State>): List<State>
}
