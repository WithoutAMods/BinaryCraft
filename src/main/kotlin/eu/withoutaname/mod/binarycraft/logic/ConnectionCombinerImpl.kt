package eu.withoutaname.mod.binarycraft.logic

import java.util.*

class ConnectionCombinerImpl<ID> : ConnectionCombiner<ID> {
    private val adjacencyList = mutableMapOf<ID, MutableSet<ID>>()
    private val idToNetID = mutableMapOf<ID, Long>()
    private val netIDToID = mutableMapOf<Long, MutableSet<ID>>()
    private val netIDSubscribers = mutableMapOf<Long, MutableSet<Pair<ID, (Long) -> Unit>>>()

    private val deletedNetIDs: Queue<Long> = LinkedList()
    private var nextNetID: Long = 0
        get() {
            return if (!deletedNetIDs.isEmpty()) {
                deletedNetIDs.poll()
            } else {
                field++
            }
        }

    override fun createNode(id: ID): Long {
        val netID = nextNetID
        adjacencyList[id] = mutableSetOf()
        idToNetID[id] = netID
        netIDToID[netID] = mutableSetOf(id)
        netIDSubscribers[netID] = mutableSetOf()
        return netID
    }

    override fun subscribe(id: ID, callback: (Long) -> Unit) {
        netIDSubscribers[getNetworkId(id)]!!.add(id to callback)
    }

    override fun getNetworkId(id: ID): Long {
        return idToNetID[id] ?: throw IllegalArgumentException("ID $id is not registered")
    }

    private fun mergeNetworks(netID1: Long, netID2: Long) {
        val ids1 = netIDToID[netID1]!!
        val ids2 = netIDToID[netID2]!!

        if (ids1.size < ids2.size) {
            mergeNetworks(netID2, netID1)
            return
        }

        for (id in netIDToID.remove(netID2)!!) {
            idToNetID[id] = netID1
            ids1.add(id)
        }
        val subscribers1 = netIDSubscribers[netID1]!!
        for ((id, callback) in netIDSubscribers.remove(netID2)!!) {
            callback(netID1)
            subscribers1.add(id to callback)
        }
        deletedNetIDs.add(netID2)
    }

    override fun connect(id1: ID, id2: ID) {
        adjacencyList[id1]!!.add(id2)
        adjacencyList[id2]!!.add(id1)
        val netID1 = getNetworkId(id1)
        val netID2 = getNetworkId(id2)
        if (netID1 == netID2) return
        mergeNetworks(netID1, netID2)
    }

    private fun isReachable(from: ID, to: ID): Boolean {
        val visited = mutableSetOf<ID>()
        val queue = LinkedList<ID>()
        queue.add(from)
        while (!queue.isEmpty()) {
            val current = queue.poll()

            if (current == to) return true

            for (neighbor in adjacencyList[current]!!) {
                if (neighbor !in visited) queue.add(neighbor)
            }
            visited.add(current)
        }
        return false
    }

    private fun changeNetwork(id: ID, newNetID: Long) {
        val oldNetID = getNetworkId(id)
        if (oldNetID == newNetID) return

        val oldIDs = netIDToID[oldNetID]!!
        val newIDs = mutableSetOf<ID>()
        netIDToID[newNetID] = newIDs

        val visited = mutableSetOf<ID>()
        val queue = LinkedList<ID>()
        queue.add(id)
        while (!queue.isEmpty()) {
            val current = queue.poll()

            idToNetID[current] = newNetID
            oldIDs.remove(current)
            newIDs.add(current)

            for (neighbor in adjacencyList[current]!!) {
                if (neighbor !in visited) queue.add(neighbor)
            }
            visited.add(current)
        }

        val oldSubscribers = netIDSubscribers[oldNetID]!!
        val newSubscribers = mutableSetOf<Pair<ID, (Long) -> Unit>>()
        netIDSubscribers[newNetID] = newSubscribers
        for ((subscriber, callback) in oldSubscribers) {
            if (subscriber in newIDs) {
                callback(newNetID)
                newSubscribers.add(subscriber to callback)
            }
        }
    }

    override fun disconnect(id1: ID, id2: ID) {
        adjacencyList[id1]!!.remove(id2)
        adjacencyList[id2]!!.remove(id1)
        if (isReachable(id1, id2)) return
        changeNetwork(id2, nextNetID)
    }
}
