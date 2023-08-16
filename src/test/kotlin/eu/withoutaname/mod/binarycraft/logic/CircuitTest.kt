package eu.withoutaname.mod.binarycraft.logic

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CircuitTest {

    private class InputGateBehavior(initialState: State = State.INVALID) : GateBehavior() {
        override val inputCount = 0
        override val outputCount = 1
        var state = initialState
            set(value) {
                field = value
                updateTrigger()
            }

        override fun calculateOutputs(inputStates: List<State>): List<State> {
            return listOf(state)
        }
    }

    private class RepeaterGateBehavior : GateBehavior() {
        override val inputCount = 1
        override val outputCount = 1

        override fun calculateOutputs(inputStates: List<State>): List<State> {
            return inputStates
        }
    }

    @Test
    fun testInputGate() {
        val circuit = CircuitImpl()
        val input = InputGateBehavior(State.LOW)
        val connection = circuit.createConnection()

        val inputId = circuit.addGate(input)
        circuit.setGateOutput(inputId, 0, connection)

        assertEquals(State.LOW, circuit.getState(connection))
        State.entries.forEach {
            input.state = it
            assertEquals(it, circuit.getState(connection))
        }
    }

    @Test
    fun testConnectionWithTwoInputs() {
        val circuit = CircuitImpl()
        val inputA = InputGateBehavior(State.LOW)
        val inputB = InputGateBehavior(State.LOW)
        val connection = circuit.createConnection()

        val inputAId = circuit.addGate(inputA)
        circuit.setGateOutput(inputAId, 0, connection)
        val inputBId = circuit.addGate(inputB)
        circuit.setGateOutput(inputBId, 0, connection)

        assertEquals(State.LOW, circuit.getState(connection))

        inputA.state = State.Z
        State.entries.forEach {
            inputB.state = it
            assertEquals(it, circuit.getState(connection))
        }

        inputA.state = State.INVALID
        State.entries.forEach {
            inputB.state = it
            assertEquals(State.INVALID, circuit.getState(connection))
        }

        inputA.state = State.LOW
        inputB.state = State.LOW
        assertEquals(State.LOW, circuit.getState(connection))

        inputA.state = State.LOW
        inputB.state = State.HIGH
        assertEquals(State.INVALID, circuit.getState(connection))

        inputA.state = State.HIGH
        inputB.state = State.LOW
        assertEquals(State.INVALID, circuit.getState(connection))

        inputA.state = State.HIGH
        inputB.state = State.HIGH
        assertEquals(State.HIGH, circuit.getState(connection))
    }

    @Test
    fun testWithRepeaterGate() {
        val circuit = CircuitImpl()
        val input = InputGateBehavior(State.LOW)
        val inConnection = circuit.createConnection()
        val outConnection = circuit.createConnection()

        val inputId = circuit.addGate(input)
        circuit.setGateOutput(inputId, 0, inConnection)
        val repeaterId = circuit.addGate(RepeaterGateBehavior())
        circuit.setGateInput(repeaterId, 0, inConnection)
        circuit.setGateOutput(repeaterId, 0, outConnection)

        assertEquals(State.LOW, circuit.getState(outConnection))
        State.entries.forEach {
            input.state = it
            assertEquals(it, circuit.getState(outConnection))
        }
    }

    @Test
    fun testWithTwoParallelRepeaterGates() {
        val circuit = CircuitImpl()
        val input = InputGateBehavior(State.LOW)
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

        assertEquals(State.LOW, circuit.getState(outConnection))
        State.entries.forEach {
            input.state = it
            assertEquals(it, circuit.getState(outConnection))
        }
    }

    @Test
    fun testCyclicRepeaterGate() {
        val circuit = CircuitImpl()
        val input = InputGateBehavior(State.LOW)
        val connection = circuit.createConnection()

        val inputId = circuit.addGate(input)
        circuit.setGateOutput(inputId, 0, connection)
        val repeaterId = circuit.addGate(RepeaterGateBehavior())
        circuit.setGateInput(repeaterId, 0, connection)
        circuit.setGateOutput(repeaterId, 0, connection)

        assertEquals(State.LOW, circuit.getState(connection))
        State.entries.forEach {
            input.state = it
            assertEquals(it, circuit.getState(connection))
        }
    }
}
