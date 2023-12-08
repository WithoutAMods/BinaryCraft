package eu.withoutaname.mod.binarycraft.logic

class IdList<T : Any> {
    private val list = mutableListOf<T?>()
    private var nextId = 0

    fun add(element: T): Int {
        val id = nextId
        if (id < list.size) {
            list[id] = element
        } else {
            list.add(element)
        }
        while (nextId < list.size && list[nextId] != null) {
            nextId++
        }
        return id
    }

    fun remove(id: Int): T {
        val element = get(id)
        list[id] = null
        if (id < nextId) {
            nextId = id
        }
        return element
    }

    operator fun get(id: Int): T {
        val element = list[id]
        require(element != null) { "Element with id $id does not exist" }
        return element
    }
}
