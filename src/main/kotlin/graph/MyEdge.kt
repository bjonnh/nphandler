package net.nprod.nphandler.graph

import net.nprod.nphandler.models.Thing
import org.jgrapht.graph.DefaultEdge

/**
 * A custom type of edge that allows searching for vertices
 */
class MyEdge : DefaultEdge() {
    /**
     * Source vertex
     */
    val from: Thing
        get() = source as Thing

    /**
     * Destination vertex
     */
    val to: Thing
        get() = target as Thing

    /**
     * Does this edge has `thing` as a vertex
     */
    fun hasVertex(thing: Thing): Boolean {
        return ((from == thing) || (to == thing))
    }

    /**
     * Does this edge has one of the items of `things` as a vertex
     */
    fun hasVertexIn(things: Collection<Thing>): Boolean {
        return ((from in things) || (to in things))
    }
}
