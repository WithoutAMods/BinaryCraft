package eu.withoutaname.mod.binarycraft.logic

import eu.withoutaname.mod.binarycraft.logic.api.Circuit
import eu.withoutaname.mod.binarycraft.logic.api.Gate
import eu.withoutaname.mod.binarycraft.logic.api.State
import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CircuitTest {
    private fun getCircuit(): Circuit {
        TODO()
    }

    @Test
    fun testExternalInputs() {
        val circuit = getCircuit()
        val inputA = circuit.createInput()
        val inputB = circuit.createInput()
        val inputC = circuit.createInput()
        val inputs = listOf(inputA, inputB, inputC)
        inputs.forEach { it.state = State.LOW }

        val gate = mockk<Gate>()
        every { gate.inputCount } returns 3
        every { gate.outputCount } returns 0
        every { gate.update(any()) } answers { emptyList() }

        circuit.addGate(gate, inputs, emptyList())
        inputA.state = State.HIGH
        inputC.state = State.HIGH
        inputA.state = State.Z
        inputB.state = State.INVALID

        verifyOrder {
            gate.update(listOf(State.LOW, State.LOW, State.LOW))
            gate.update(listOf(State.HIGH, State.LOW, State.LOW))
            gate.update(listOf(State.HIGH, State.LOW, State.HIGH))
            gate.update(listOf(State.Z, State.LOW, State.HIGH))
            gate.update(listOf(State.Z, State.INVALID, State.HIGH))
        }

        verify(atLeast = 0) {
            gate.inputCount
            gate.outputCount
        }

        confirmVerified(gate)

    }

    private fun mockRepeaterGate(): Gate {
        val gate = mockk<Gate>()
        every { gate.inputCount } returns 1
        every { gate.outputCount } returns 1
        val inputSlot = slot<List<State>>()
        every { gate.update(capture(inputSlot)) } answers { listOf(inputSlot.captured[0]) }
        return gate
    }

    @Test
    fun testWithRepeaterGate() {
        val circuit = getCircuit()
        val input = circuit.createInput()
        val output = circuit.createConnection()
        input.state = State.LOW

        val gate = mockRepeaterGate()

        circuit.addGate(gate, listOf(input), listOf(output))
        assertEquals(State.LOW, output.state)
        State.entries.forEach {
            input.state = it
            assertEquals(it, output.state)
        }
    }

    @Test
    fun testWithTwoParallelRepeaterGates() {
        val circuit = getCircuit()
        val input = circuit.createInput()
        val output = circuit.createConnection()
        input.state = State.LOW

        val gateA = mockRepeaterGate()
        val gateB = mockRepeaterGate()

        circuit.addGate(gateA, listOf(input), listOf(output))
        circuit.addGate(gateB, listOf(input), listOf(output))
        assertEquals(State.LOW, output.state)
        State.entries.forEach {
            input.state = it
            assertEquals(it, output.state)
        }
    }

    @Test
    fun testTwoRepeaterGatesSameOutput() {
        val circuit = getCircuit()
        val inputA = circuit.createInput()
        val inputB = circuit.createInput()
        val output = circuit.createConnection()
        inputA.state = State.LOW
        inputB.state = State.LOW

        val gateA = mockRepeaterGate()
        val gateB = mockRepeaterGate()

        circuit.addGate(gateA, listOf(inputA), listOf(output))
        circuit.addGate(gateB, listOf(inputB), listOf(output))
        assertEquals(State.LOW, output.state)

        inputA.state = State.Z
        State.entries.forEach {
            inputB.state = it
            assertEquals(it, output.state)
        }

        inputA.state = State.INVALID
        State.entries.forEach {
            inputB.state = it
            assertEquals(State.INVALID, output.state)
        }

        inputA.state = State.LOW
        inputB.state = State.LOW
        assertEquals(State.LOW, output.state)

        inputA.state = State.LOW
        inputB.state = State.HIGH
        assertEquals(State.INVALID, output.state)

        inputA.state = State.HIGH
        inputB.state = State.LOW
        assertEquals(State.INVALID, output.state)

        inputA.state = State.HIGH
        inputB.state = State.HIGH
        assertEquals(State.HIGH, output.state)
    }
}
