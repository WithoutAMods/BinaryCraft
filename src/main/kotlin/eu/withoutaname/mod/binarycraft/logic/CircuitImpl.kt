package eu.withoutaname.mod.binarycraft.logic

import java.util.*

class CircuitImpl : Circuit {
    private val connections = IdList<Connection>()
    private val gates = IdList<Gate>()

    override fun createConnection(): ConnectionId {
        val id = connections.add(Connection())
        return ConnectionId(id)
    }

    override fun removeConnection(connection: ConnectionId) {
        val removed = connections.remove(connection.id)
        removed.disconnectEverything()
    }

    override fun getState(connection: ConnectionId) = connections[connection.id].state

    override fun addGate(gateBehavior: GateBehavior): GateId {
        val id = gates.add(Gate(gateBehavior))
        return GateId(id)
    }

    override fun removeGate(gateId: GateId) {
        val removed = gates.remove(gateId.id)
        removed.disconnectEverything()
    }

    override fun setGateInput(gateId: GateId, inputIndex: Int, connection: ConnectionId?) {
        gates[gateId].setInput(inputIndex, connection?.let { connections[it.id] })
    }

    override fun setGateOutput(gateId: GateId, outputIndex: Int, connection: ConnectionId?) {
        gates[gateId].setOutput(outputIndex, connection?.let { connections[it.id] })
    }

    private fun getNumIncomingInSubTree(gate: Gate): MutableMap<Connection, Int> {
        val numIncoming = mutableMapOf<Connection, Int>()
        val queue = LinkedList<Connection>()
        gate.outputs.forEach {
            it?.let {
                numIncoming[it] = 0
                queue.add(it)
            }
        }
        while (queue.isNotEmpty()) {
            val connection = queue.remove()
            connection.forEachDependentConnection {
                if (numIncoming[it] == null) {
                    numIncoming[it] = 1
                    queue.add(it)
                } else {
                    numIncoming[it] = numIncoming[it]!! + 1
                }
            }
        }
        return numIncoming
    }

    private fun update(gate: Gate) {
        val numIncoming = getNumIncomingInSubTree(gate)
        updateAcyclic(numIncoming, mutableSetOf())
    }

    private fun updateAcyclic(numIncoming: MutableMap<Connection, Int>, updated: MutableSet<Gate>) {
        val queue = LinkedList<Connection>()
        numIncoming.filter { (_, count) -> count == 0 }.forEach { (connection, _) ->
            numIncoming.remove(connection)
            queue.add(connection)
        }
        while (queue.isNotEmpty()) {
            val connection = queue.remove()
            val changed = connection.calculateState(updated)
            if (changed) {
                connection.forEachDependentConnection {
                    numIncoming[it] = numIncoming[it]!! - 1
                    if (numIncoming[it] == 0) {
                        numIncoming.remove(it)
                        queue.add(it)
                    }
                }
            } else {
                val queueNotNeeded = LinkedList<Connection>()
                queueNotNeeded.add(connection)
                while (queueNotNeeded.isNotEmpty()) {
                    queueNotNeeded.remove().forEachDependentConnection {
                        numIncoming[it] = numIncoming[it]!! - 1
                        if (numIncoming[it] == 0) {
                            numIncoming.remove(it)
                            queueNotNeeded.add(it)
                        }
                    }
                }
            }
        }
        if (numIncoming.isNotEmpty()) {
            updateCyclic(numIncoming, updated)
        }
    }

    private fun updateCyclic(
        numIncoming: MutableMap<Connection, Int>, updated: MutableSet<Gate>
    ) {
        TODO()
    }

    private inner class Connection {
        var state: State = State.Z
        val connectedGateOutputs = mutableSetOf<Pair<Gate, Int>>()
        val connectedGateInputs = mutableSetOf<Pair<Gate, Int>>()

        fun calculateState(updated: MutableSet<Gate>): Boolean {
            connectedGateOutputs.forEach { (gate, _) ->
                if (gate !in updated) {
                    gate.calculateOutputs()
                    updated.add(gate)
                }
            }

            val oldState = state
            state = State.Z
            for ((gate, index) in connectedGateOutputs) {
                when (gate.outputStates[index]) {
                    State.Z -> {
                        continue
                    }

                    State.INVALID -> {
                        state = State.INVALID
                        break
                    }

                    else -> {
                        if (state == State.Z) {
                            state = gate.outputStates[index]
                        } else if (state != gate.outputStates[index]) {
                            state = State.INVALID
                            break
                        }
                    }
                }
            }
            return oldState != state
        }

        fun disconnectEverything() {
            connectedGateOutputs.forEach { (gate, index) ->
                gate.setInput(index, null)
            }
            connectedGateInputs.forEach { (gate, index) ->
                gate.setOutput(index, null)
            }
        }

        inline fun forEachDependentConnection(block: (Connection) -> Unit) {
            connectedGateInputs.forEach { (gate, _) ->
                gate.outputs.forEach {
                    it?.let(block)
                }
            }
        }
    }

    private inner class Gate(
        val gateBehavior: GateBehavior
    ) {
        val inputs: MutableList<Connection?> = mutableListOf()
        val outputs: MutableList<Connection?> = mutableListOf()
        lateinit var outputStates: List<State>

        init {
            while (inputs.size < gateBehavior.inputCount) {
                inputs.add(null)
            }
            while (outputs.size < gateBehavior.outputCount) {
                outputs.add(null)
            }
            gateBehavior.updateTrigger = { update(this) }
            calculateOutputs()
        }

        private fun state(connection: Connection?) = (connection?.state ?: State.Z)

        fun setInput(index: Int, connection: Connection?) {
            val old = inputs[index]
            old?.connectedGateInputs?.remove(this to index)
            inputs[index] = connection
            connection?.connectedGateInputs?.add(this to index)
            if (state(old) != state(connection)) {
                update(this)
            }
        }

        fun setOutput(index: Int, connection: Connection?) {
            val old = outputs[index]
            old?.connectedGateOutputs?.remove(this to index)
            outputs[index] = connection
            connection?.connectedGateOutputs?.add(this to index)
            if (old != connection) {
                update(this)
            }
        }

        fun disconnectEverything() {
            for (i in inputs.indices) {
                setInput(i, null)
            }
            for (i in outputs.indices) {
                setOutput(i, null)
            }
        }

        fun calculateOutputs() {
            val inputStates = inputs.map { state(it) }
            outputStates = gateBehavior.calculateOutputs(inputStates)
        }
    }

    private operator fun IdList<Connection>.get(connectionId: ConnectionId) = this[connectionId.id]
    private operator fun IdList<Gate>.get(gateId: GateId) = this[gateId.id]
}
