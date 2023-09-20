package eu.withoutaname.mod.binarycraft.logic

interface ConnectionCombiner<ID> {
    fun createNode(id: ID): Long
    fun subscribe(id: ID, callback: (Long) -> Unit)
    fun getNetworkId(id: ID): Long
    fun connect(id1: ID, id2: ID)
    fun disconnect(id1: ID, id2: ID)
}
