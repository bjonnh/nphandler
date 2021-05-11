package net.nprod.nphandler

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.required
import net.nprod.nphandler.graph.export.toDOTGraph
import net.nprod.nphandler.helpers.toFile
import net.nprod.nphandler.models.import.npDatasetFromSpreadSheet

fun main(args: Array<String>) {
    val parser = ArgParser("example")
    val input by parser.option(ArgType.String, shortName = "i", description = "Input file").required()
    val output by parser.option(ArgType.String, shortName = "o", description = "Output file name").required()
    parser.parse(args)

    val npDataset = npDatasetFromSpreadSheet(input)

    val gr = npDataset
        //.filterGraph(from=npDataset.methods["JIN_M_0003"], to=npDataset.fractions["JIN_0084"])
        .toGraph()
        .toDOTGraph()
        .toFile(output)
}
