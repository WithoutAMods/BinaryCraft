package eu.withoutaname.mod.binarycraft.logic

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CircuitTest {

    private class InputGateBehavior(initialState: OutputState = OutputState.INVALID) : GateBehavior() {
        override val inputCount = 0
        override val outputCount = 1
        var state = initialState
            set(value) {
                field = value
                updateTrigger()
            }

        override fun calculateOutputs(inputStates: List<ConnectionState>): List<OutputState> {
            return listOf(state)
        }
    }

    private class RepeaterGateBehavior : BooleanGateBehavior() {
        override val inputCount = 1
        override val outputCount = 1

        override fun calculateBooleanOutputs(inputs: List<Boolean>): List<Boolean> {
            return inputs
        }
    }

    @Test
    fun testInputGate() {
        val circuit = CircuitImpl()
        val input = InputGateBehavior(OutputState.LOW)
        val connection = circuit.createConnection()

        val inputId = circuit.addGate(input)
        circuit.setGateOutput(inputId, 0, connection)

        assertEquals(ConnectionState.LOW, circuit.getState(connection))
        input.state = OutputState.INVALID
        assertEquals(ConnectionState.INVALID, circuit.getState(connection))
        input.state = OutputState.HIGH
        assertEquals(ConnectionState.HIGH, circuit.getState(connection))
        input.state = OutputState.LOW
        assertEquals(ConnectionState.LOW, circuit.getState(connection))
        input.state = OutputState.PULL_UP
        assertEquals(ConnectionState.HIGH, circuit.getState(connection))
        input.state = OutputState.PULL_DOWN
        assertEquals(ConnectionState.LOW, circuit.getState(connection))
        input.state = OutputState.Z
        assertEquals(ConnectionState.INVALID, circuit.getState(connection))
    }

    @Test
    fun testConnectionWithTwoInputs() {
        val circuit = CircuitImpl()
        val inputA = InputGateBehavior(OutputState.LOW)
        val inputB = InputGateBehavior(OutputState.LOW)
        val connection = circuit.createConnection()

        val inputAId = circuit.addGate(inputA)
        circuit.setGateOutput(inputAId, 0, connection)
        val inputBId = circuit.addGate(inputB)
        circuit.setGateOutput(inputBId, 0, connection)

        assertEquals(ConnectionState.LOW, circuit.getState(connection))

        for (stateA in listOf(OutputState.PULL_UP, OutputState.PULL_DOWN, OutputState.Z)) {
            inputA.state = stateA
            inputB.state = OutputState.INVALID
            assertEquals(ConnectionState.INVALID, circuit.getState(connection))
            inputB.state = OutputState.HIGH
            assertEquals(ConnectionState.HIGH, circuit.getState(connection))
            inputB.state = OutputState.LOW
            assertEquals(ConnectionState.LOW, circuit.getState(connection))
        }

        inputA.state = OutputState.INVALID
        for (stateB in OutputState.entries) {
            inputB.state = stateB
            assertEquals(ConnectionState.INVALID, circuit.getState(connection))
        }

        for ((state, connectionState) in listOf(
            OutputState.INVALID to ConnectionState.INVALID,
            OutputState.HIGH to ConnectionState.HIGH,
            OutputState.LOW to ConnectionState.LOW,
            OutputState.PULL_UP to ConnectionState.HIGH,
            OutputState.PULL_DOWN to ConnectionState.LOW,
            OutputState.Z to ConnectionState.INVALID,
        )) {
            inputA.state = state
            inputB.state = state
            assertEquals(connectionState, circuit.getState(connection))

        }

        inputA.state = OutputState.Z
        inputB.state = OutputState.PULL_UP
        assertEquals(ConnectionState.HIGH, circuit.getState(connection))
        inputA.state = OutputState.Z
        inputB.state = OutputState.PULL_DOWN
        assertEquals(ConnectionState.LOW, circuit.getState(connection))
        inputA.state = OutputState.PULL_UP
        inputB.state = OutputState.PULL_DOWN
        assertEquals(ConnectionState.INVALID, circuit.getState(connection))
        inputA.state = OutputState.HIGH
        inputB.state = OutputState.LOW
        assertEquals(ConnectionState.INVALID, circuit.getState(connection))
    }

    @Test
    fun testWithRepeaterGate() {
        val circuit = CircuitImpl()
        val input = InputGateBehavior(OutputState.LOW)
        val inConnection = circuit.createConnection()
        val outConnection = circuit.createConnection()

        val inputId = circuit.addGate(input)
        circuit.setGateOutput(inputId, 0, inConnection)
        val repeaterId = circuit.addGate(RepeaterGateBehavior())
        circuit.setGateInput(repeaterId, 0, inConnection)
        circuit.setGateOutput(repeaterId, 0, outConnection)

        for (state in OutputState.entries) {
            input.state = state
            assertEquals(circuit.getState(inConnection), circuit.getState(outConnection))
        }
    }

    @Test
    fun testWithTwoParallelRepeaterGates() {
        val circuit = CircuitImpl()
        val input = InputGateBehavior(OutputState.LOW)
        val inConnection = circuit.createConnection()
        val outConnection = circuit.createConnection()

        val inputId = circuit.addGate(input)
        circuit.setGateOutput(inputId, 0, inConnection)
        val repeaterAId = circuit.addGate(RepeaterGateBehavior())
        circuit.setGateInput(repeaterAId, 0, inConnection)
        circuit.setGateOutput(repeaterAId, 0, outConnection)
        val repeaterBId = circuit.addGate(RepeaterGateBehavior())
        circuit.setGateInput(repeaterBId, 0, inConnection)
        circuit.setGateOutput(repeaterBId, 0, outConnection)

        for (state in OutputState.entries) {
            input.state = state
            assertEquals(circuit.getState(inConnection), circuit.getState(outConnection))
        }
    }

    @Test
    fun testCyclicRepeaterGate() {
        val circuit = CircuitImpl()
        val input = InputGateBehavior(OutputState.LOW)
        val connection = circuit.createConnection()

        val inputId = circuit.addGate(input)
        circuit.setGateOutput(inputId, 0, connection)
        val repeaterId = circuit.addGate(RepeaterGateBehavior())
        circuit.setGateInput(repeaterId, 0, connection)
        circuit.setGateOutput(repeaterId, 0, connection)

        assertEquals(ConnectionState.LOW, circuit.getState(connection))

        for (state in listOf(OutputState.PULL_UP, OutputState.PULL_DOWN, OutputState.Z)) {
            input.state = state
            assertEquals(ConnectionState.LOW, circuit.getState(connection))
        }

        input.state = OutputState.HIGH
        assertEquals(ConnectionState.INVALID, circuit.getState(connection))

        for (state in OutputState.entries) {
            input.state = state
            assertEquals(ConnectionState.INVALID, circuit.getState(connection))
        }
    }
}
