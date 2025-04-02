package eu.withoutaname.mod.binarycraft.logic

import java.util.*

class ConnectionGraphImpl<ID> : ConnectionGraph<ID> {
    private val nodes = mutableMapOf<ID, Node>()
    private val connections = IdList<Connection>()

    override fun createNode(id: ID): ConnectionId {
        val connection = Connection()
        val connectionId = ConnectionId(connections.add(connection))
        nodes[id] = Node(connectionId)
        connection.members.add(id)
        return connectionId
    }

    override fun subscribe(id: ID, callback: (ConnectionId) -> Unit) {
        nodes[id]?.subscribers?.add(callback)
    }

    override fun getConnectionId(id: ID): ConnectionId {
        return nodes[id]?.connectionID ?: throw IllegalArgumentException("ID $id is not registered")
    }

    private fun mergeConnections(connectionID1: ConnectionId, connectionID2: ConnectionId) {
        val connection1 = connections[connectionID1.id]
        val connection2 = connections[connectionID2.id]

        if (connection1.members.size < connection2.members.size) {
            mergeConnections(connectionID2, connectionID1)
            return
        }

        for (id in connection2.members) {
            nodes[id]!!.connectionID = connectionID1
            connection1.members.add(id)
        }
        connections.remove(connectionID2.id)
    }

    override fun connect(id1: ID, id2: ID) {
        nodes[id1]!!.neighbors.add(id2)
        nodes[id2]!!.neighbors.add(id1)
        val connectionID1 = getConnectionId(id1)
        val connectionID2 = getConnectionId(id2)
        if (connectionID1 == connectionID2) return
        mergeConnections(connectionID1, connectionID2)
    }

    private fun isReachable(from: ID, to: ID): Boolean {
        val visited = mutableSetOf<ID>()
        val queue = LinkedList<ID>()
        queue.add(from)
        while (!queue.isEmpty()) {
            val current = queue.poll()

            if (current == to) return true

            for (neighbor in nodes[current]!!.neighbors) {
                if (neighbor !in visited) queue.add(neighbor)
            }
            visited.add(current)
        }
        return false
    }

    private fun assignNewConnection(id: ID) {
        val oldConnection = connections[getConnectionId(id).id]
        val newConnection = Connection()
        val newConnectionID = ConnectionId(connections.add(newConnection))

        val visited = mutableSetOf<ID>()
        val queue = LinkedList<ID>()
        queue.add(id)
        while (!queue.isEmpty()) {
            val current = queue.poll()
            val currentNode = nodes[current]!!

            oldConnection.members.remove(current)
            newConnection.members.add(current)
            currentNode.connectionID = newConnectionID

            for (neighbor in currentNode.neighbors) {
                if (neighbor !in visited) queue.add(neighbor)
            }
            visited.add(current)
        }
    }

    override fun disconnect(id1: ID, id2: ID) {
        nodes[id1]!!.neighbors.remove(id2)
        nodes[id2]!!.neighbors.remove(id1)
        if (isReachable(id1, id2)) return
        assignNewConnection(id2)
    }

    private inner class Node(connectionID: ConnectionId) {
        var connectionID = connectionID
            set(value) {
                field = value
                for (subscriber in subscribers) {
                    subscriber(value)
                }
            }
        val neighbors = mutableSetOf<ID>()
        val subscribers = mutableSetOf<(ConnectionId) -> Unit>()
    }

    private inner class Connection {
        val members = mutableSetOf<ID>()
    }
}
