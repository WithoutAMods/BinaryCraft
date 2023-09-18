package eu.withoutaname.mod.binarycraft.logic


enum class ConnectionState {
    INVALID, HIGH, LOW;
}

enum class OutputState {
    INVALID, HIGH, LOW, PULL_UP, PULL_DOWN, Z;
}

fun List<OutputState>.toConnectionState(): ConnectionState {
    if (isEmpty()) return ConnectionState.INVALID

    var high = false
    var low = false
    var pullUp = false
    var pullDown = false
    for (state in this) {
        when (state) {
            OutputState.INVALID -> return ConnectionState.INVALID
            OutputState.HIGH -> high = true
            OutputState.LOW -> low = true
            OutputState.PULL_UP -> pullUp = true
            OutputState.PULL_DOWN -> pullDown = true
            OutputState.Z -> {}
        }
    }
    if (high && low) return ConnectionState.INVALID
    if (high) return ConnectionState.HIGH
    if (low) return ConnectionState.LOW
    if (pullUp == pullDown) return ConnectionState.INVALID
    if (pullUp) return ConnectionState.HIGH
    return ConnectionState.LOW
}
