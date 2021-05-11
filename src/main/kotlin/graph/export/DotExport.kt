package net.nprod.nphandler.graph.export

import net.nprod.nphandler.SchemeGraph
import net.nprod.nphandler.graph.MyEdge
import net.nprod.nphandler.models.Fraction
import net.nprod.nphandler.models.Method
import net.nprod.nphandler.models.Thing
import org.jgrapht.nio.Attribute
import org.jgrapht.nio.DefaultAttribute
import org.jgrapht.nio.dot.DOTExporter
import java.io.StringWriter
import java.io.Writer

/**
 * Export the given SchemeGraph to the DOT (GraphViz format)
 *
 * @return the content of the DOT file
 */

typealias DotGraph = String

fun SchemeGraph.toDOTGraph(): DotGraph {
    val exporter: DOTExporter<Thing, MyEdge> = DOTExporter { v -> v.realId }
    exporter.setVertexAttributeProvider { v ->
        val map: MutableMap<String, Attribute> = LinkedHashMap<String, Attribute>()

        when (v) {
            is Method -> {
                map["label"] = org.jgrapht.nio.DefaultAttribute.createAttribute("${v.realId}\n${v.name}\n${v.description}")
                map["style"] = org.jgrapht.nio.DefaultAttribute.createAttribute("filled")
                map["shape"] = org.jgrapht.nio.DefaultAttribute.createAttribute("box")
            }
            is Fraction -> {
                map["label"] =
                    org.jgrapht.nio.DefaultAttribute.createAttribute("${v.realId}\n${v.name}\nWeight: ${v.weight ?: "unknown"}\nLeft: ${v.weightLeft ?: "unknown"}")
                map["color"] = org.jgrapht.nio.DefaultAttribute.createAttribute("#A0A0F0")
                map["style"] = org.jgrapht.nio.DefaultAttribute.createAttribute("filled")
                map["shape"] = org.jgrapht.nio.DefaultAttribute.createAttribute("ellipse")
            }
            else -> println("I don't eat that")
        }

        map
    }
    val writer: Writer = StringWriter()
    exporter.exportGraph(this, writer)
    return writer.toString()
}
