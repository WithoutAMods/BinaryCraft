package eu.withoutaname.mod.binarycraft.logic

abstract class GateBehavior {
    abstract val inputCount: Int
    abstract val outputCount: Int
    var updateTrigger: (() -> Unit) = {}
    abstract fun calculateOutputs(inputStates: List<ConnectionState>): List<OutputState>
}


abstract class BooleanGateBehavior : GateBehavior() {
    abstract fun calculateBooleanOutputs(inputs: List<Boolean>): List<Boolean>

    override fun calculateOutputs(inputStates: List<ConnectionState>): List<OutputState> {
        if (inputStates.size != inputCount) throw IllegalArgumentException("Wrong number of inputs")
        val inputs = inputStates.map {
            when (it) {
                ConnectionState.INVALID -> return List(outputCount) { OutputState.INVALID }
                ConnectionState.HIGH -> true
                ConnectionState.LOW -> false
            }
        }
        return calculateBooleanOutputs(inputs).map { if (it) OutputState.HIGH else OutputState.LOW }
    }
}
