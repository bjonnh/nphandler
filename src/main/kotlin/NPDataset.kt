import com.github.miachm.sods.SpreadSheet
import models.Fraction
import models.Method
import models.MethodType
import models.Thing
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.nio.Attribute
import org.jgrapht.nio.DefaultAttribute
import org.jgrapht.nio.dot.DOTExporter
import java.io.File
import java.io.StringWriter
import java.io.Writer
import java.util.*


val fractionsSheetStructure = listOf("PREFIX", "ID", "Name", "Weight (mg)", "Left (mg)", "Location")
val methodsSheetStructure = listOf("PREFIX", "ID", "Type", "Name", "Description", "Input fractions", "Output fractions")

class FractionsSheetException : Exception("There should be a sheet called 'Fractions' in the document")
class MethodsSheetException : Exception("There should be a sheet called 'Methods' in the document")
class FractionsSheetStructureException(private val headers: List<Any>) : Exception(
    "The Fractions sheet should have a structure such as:" +
            "${fractionsSheetStructure.joinToString(",")} \n" +
            "it was: ${headers.joinToString(",")}"
)

class MethodsSheetStructureException(private val headers: List<Any>) : Exception(
    "The Methods sheet should have a structure such as:" +
            "${methodsSheetStructure.joinToString(",")} \n" +
            "it was: ${headers.joinToString(",")}"
)

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


typealias SchemeGraph = Graph<Thing, MyEdge>

fun SchemeGraph.toDOTGraph(): String {
    val exporter: DOTExporter<Thing, MyEdge> = DOTExporter { v -> v.realId }
    exporter.setVertexAttributeProvider { v ->
        val map: MutableMap<String, Attribute> = LinkedHashMap<String, Attribute>()

        when (v) {
            is Method -> {
                map["label"] = DefaultAttribute.createAttribute("${v.realId}\n${v.name}\n${v.description}")
                map["style"] = DefaultAttribute.createAttribute("filled")
                map["shape"] = DefaultAttribute.createAttribute("box")
            }
            is Fraction -> {
                map["label"] =
                    DefaultAttribute.createAttribute("${v.realId}\n${v.name}\nWeight: ${v.weight ?: "unknown"}\nLeft: ${v.weightLeft ?: "unknown"}")
                map["color"] = DefaultAttribute.createAttribute("#A0A0F0")
                map["style"] = DefaultAttribute.createAttribute("filled")
                map["shape"] = DefaultAttribute.createAttribute("ellipse")
            }
            else -> println("I don't eat that")
        }

        map
    }
    val writer: Writer = StringWriter()
    exporter.exportGraph(this, writer)
    return writer.toString()
}

class MyEdge : DefaultEdge() {
    val from: Thing
        get() = source as Thing

    val to: Thing
        get() = target as Thing

    fun hasVertex(thing: Thing): Boolean {
        return ((from == thing) || (to == thing))
    }

    fun hasVertexIn(thing: Collection<Thing>): Boolean {
        return ((from in thing) || (to in thing))
    }
}


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

    companion object {
        fun fromSpreadSheet(name: String): NPDataset {
            val spread = SpreadSheet(File("/home/bjo/Documents/0-Chicago/Current/ITR/FracSchemes/2020-08-31_Jin.ods"))
            val fractionsSheet = spread.getSheet("Fractions") ?: throw FractionsSheetException()
            val methodsSheet = spread.getSheet("Methods") ?: throw MethodsSheetException()

            // Getting and validating fractions header
            val fractionsHeader = fractionsSheet.dataRange.values.first().mapNotNull { it }
            if (fractionsHeader.subList(0, 6) != fractionsSheetStructure) {
                throw FractionsSheetStructureException(fractionsHeader.subList(0, 6))
            }

            val fractions = fractionsSheet.dataRange.values.drop(1).mapNotNull {
                if (it[0] != null) {
                    val id = (it[1] as Double).toInt().toString().padStart(4, '0')
                    val realId = it[0].toString() + "_" + id
                    realId to Fraction(
                        prefix = it[0] as String,
                        id = id,
                        realId = realId,
                        name = it[2].toString(),
                        weight = it[3] as Number?,
                        weightLeft = it[4] as Number?,
                        location = (it[5] ?: "").toString()
                    )
                } else {
                    null
                }
            }.toMap()

            // Check for fraction duplicates
            fractions.keys.groupingBy { it }.eachCount().filter { it.value > 1 }.forEach {
                println("Warning! Duplicate fraction: ${it.key}")
            }

            // Getting and validating methods header
            val methodsHeader = methodsSheet.dataRange.values.first().mapNotNull { it }
            if (methodsHeader.subList(0, 7) != methodsSheetStructure) {
                throw MethodsSheetStructureException(methodsHeader.subList(0, 7))
            }

            val methods = methodsSheet.dataRange.values.drop(1).mapNotNull {
                if (it[0] != null) {
                    val id = (it[1] as Double).toInt().toString().padStart(4, '0')
                    val realId = it[0].toString() + "_" + id
                    realId to Method(
                        prefix = it[0] as String,
                        id = id,
                        realId = realId,
                        type = MethodType.values().firstOrNull { methodType -> methodType.text == it[2].toString() }
                            ?: throw InvalidMethodTypeException(it[2].toString()),
                        name = it[3].toString(),
                        description = (it[4] ?: "").toString(),
                        inputFractions = it[5].toString().split("|").map { fractionName ->
                            fractions[fractionName] ?: throw UnknownFractionInMethodException(
                                realId,
                                "input",
                                fractionName
                            )
                        },
                        outputFractions = it[6].toString().split("|").map { fractionName ->
                            fractions[fractionName] ?: throw UnknownFractionInMethodException(
                                realId,
                                "output",
                                fractionName
                            )
                        })
                } else {
                    null
                }
            }.toMap()

            methods.keys.groupingBy { it }.eachCount().filter { it.value > 1 }.forEach {
                println("Warning! Duplicate methods: ${it.key}")
            }

            // TODO: Need to validate that fractions are coming from… somewhere

            return NPDataset(fractions, methods)
        }
    }
}