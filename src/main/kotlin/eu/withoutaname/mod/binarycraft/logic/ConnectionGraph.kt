package eu.withoutaname.mod.binarycraft.logic

interface ConnectionGraph<ID> {
    fun createNode(id: ID): ConnectionId
    fun subscribe(id: ID, callback: (ConnectionId) -> Unit)
    fun getConnectionId(id: ID): ConnectionId
    fun connect(id1: ID, id2: ID)
    fun disconnect(id1: ID, id2: ID)
}
