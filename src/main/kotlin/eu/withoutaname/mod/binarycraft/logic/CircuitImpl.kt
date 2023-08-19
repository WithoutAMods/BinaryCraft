package eu.withoutaname.mod.binarycraft.logic

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

    private data class ConnectionData(
        val connection: Connection,
        val discoveryTime: Int,
        var finishedTime: Int? = null,
        val cyclicDependencies: MutableSet<Connection> = mutableSetOf()
    )

    private fun analyzeGraph(gate: Gate): MutableMap<Connection, ConnectionData> {
        val data = mutableMapOf<Connection, ConnectionData>()

        fun dfs(connection: Connection, time: Int): Int {
            var t = time
            data[connection] = ConnectionData(connection, t++)
            connection.forEachDependentConnection {
                if (it !in data) {
                    t = dfs(it, t)
                } else {
                    data[it]!!.apply {
                        if (finishedTime == null) {
                            cyclicDependencies.add(connection)
                        }
                    }
                }
            }
            data[connection]!!.finishedTime = t++
            return t
        }

        var t = 0
        gate.outputs.forEach {
            it?.let { t = dfs(it, t) }
        }
        return data
    }

    private fun update(gate: Gate) {
        val dataMap = analyzeGraph(gate)
        val dataList = dataMap.values.sortedByDescending { it.finishedTime!! }.toMutableList()
        val needsUpdate = gate.outputs.mapNotNull { it }.toMutableSet()
        updateAcyclic(dataMap, dataList, needsUpdate)
    }

    private fun updateAcyclic(
        dataMap: MutableMap<Connection, ConnectionData>,
        dataList: MutableList<ConnectionData>,
        needsUpdate: MutableSet<Connection>
    ) {
        var updated = mutableSetOf<Gate>()
        while (dataList.isNotEmpty()) {
            if (dataList.first().cyclicDependencies.isEmpty()) {
                val data = dataList.removeFirst()
                if (needsUpdate.contains(data.connection)) {
                    data.connection.calculateState(updated, needsUpdate)
                }
            } else {
                updateCyclic(dataMap, dataList, needsUpdate)
                updated = mutableSetOf()
            }
        }
    }

    private fun updateCyclic(
        dataMap: MutableMap<Connection, ConnectionData>,
        dataList: MutableList<ConnectionData>,
        needsUpdate: MutableSet<Connection>
    ) {
        var lastDependency: ConnectionData? = null
        for (data in dataList) {
            data.cyclicDependencies.forEach {
                if (dataMap[it]!!.finishedTime!! < (lastDependency?.finishedTime ?: Int.MAX_VALUE)) {
                    lastDependency = dataMap[it]
                }
            }

            data.connection.calculateState(mutableSetOf(), needsUpdate)

            if (data == lastDependency) {
                break
            }
        }

        for (data in dataList) {
            data.cyclicDependencies.forEach {
                if (it.state == State.INVALID) {
                    it.state = State.Z
                }
            }

            data.connection.calculateState(mutableSetOf(), needsUpdate)

            if (data == lastDependency) {
                break
            }
        }

        var stable = true
        for (data in dataList) {
            val old = data.connection.state
            data.connection.calculateState(mutableSetOf(), needsUpdate)
            if (data.connection.state != old) {
                stable = false
            }
            if (data == lastDependency) {
                break
            }
        }

        if (!stable) {
            for (data in dataList) {
                data.connection.state = State.INVALID
                if (data == lastDependency) {
                    break
                }
            }
        }
        while (dataList.isNotEmpty()) {
            val data = dataList.removeFirst()
            if (data == lastDependency) {
                break
            }
        }
    }

    private inner class Connection {
        var state: State = State.Z
        val connectedGateOutputs = mutableSetOf<Pair<Gate, Int>>()
        val connectedGateInputs = mutableSetOf<Pair<Gate, Int>>()

        fun calculateState(updated: MutableSet<Gate>, needsUpdate: MutableSet<Connection>) {
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
            if (state != oldState) {
                forEachDependentConnection {
                    needsUpdate.add(it)
                }
            }
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
