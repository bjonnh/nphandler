package net.nprod.nphandler

import net.nprod.nphandler.graph.MyEdge
import net.nprod.nphandler.models.Fraction
import net.nprod.nphandler.models.Method
import net.nprod.nphandler.models.MethodType
import net.nprod.nphandler.models.Thing
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultDirectedGraph



class InvalidMethodTypeException(private val type: String) :
    Exception(
        "The method type $type is unknown, the valid method types are: ${
            MethodType.values().map { it.text }.joinToString(", ")
        } "
    )

class UnknownFractionInMethodException(
    private val method: String,
    private val position: String,
    private val name: String
) :
    Exception("The fraction $name in method $method $position cannot be found in your fractions list.")


data class NPDataset(
    val fractions: Map<String, Fraction>,
    val methods: Map<String, Method>
) {

    fun toGraph(): SchemeGraph {
        val directedGraph: Graph<Thing, MyEdge> = DefaultDirectedGraph(MyEdge::class.java)

        methods.values.forEach { method ->
            directedGraph.addVertex(method)
            method.inputFractions.forEach { frac ->
                directedGraph.addVertex(frac)
                directedGraph.addEdge(frac, method)
            }
            method.outputFractions.forEach { frac ->
                directedGraph.addVertex(frac)
                directedGraph.addEdge(method, frac)
            }
        }

        return directedGraph
    }

    fun SchemeGraph.filterGraph(vertex: Thing, directionIsFrom: Boolean): SchemeGraph {
        val newGraph: SchemeGraph = DefaultDirectedGraph(MyEdge::class.java)

        val toConsider = mutableSetOf<Thing>()
        val edges = this.edgeSet().toMutableSet()
        while (edges.size > 0) {
            var didSomething = false
            val deleteEdges = mutableSetOf<MyEdge>()
            edges.forEach { edge ->
                val vertexToConsider = if (directionIsFrom) {
                    edge.from
                } else {
                    edge.to
                }

                val otherSideVertexToConsider = if (directionIsFrom) {
                    edge.to
                } else {
                    edge.from
                }

                if (vertexToConsider == vertex) {
                    newGraph.addVertex(edge.from)
                    newGraph.addVertex(edge.to)
                    newGraph.addEdge(edge.from, edge.to)
                    deleteEdges.add(edge)
                    didSomething = true
                    toConsider.add(otherSideVertexToConsider)
                } else if (vertexToConsider in toConsider) {
                    newGraph.addVertex(edge.from)
                    newGraph.addVertex(edge.to)
                    newGraph.addEdge(edge.from, edge.to)
                    deleteEdges.add(edge)
                    didSomething = true
                    toConsider.add(otherSideVertexToConsider)
                }
            }
            deleteEdges.forEach { edges.remove(it) }
            if (!didSomething) edges.clear()  // We have not done anything this cycle, so there will be nothing next time either
        }
        return newGraph
    }

    /**
     * Filter the graph between/to those two nodes. There is likely a way to make that in a single pass
     * But our graphs are small so that should work for now
     */
    fun filterGraph(from: Thing?, to: Thing?): SchemeGraph {
        return toGraph().let {
            if (from != null) {
                it.filterGraph(from, directionIsFrom = true)
            } else {
                it
            }
        }.let {
            if (to != null) {
                it.filterGraph(to, directionIsFrom = false)
            } else {
                it
            }
        }
    }
}
