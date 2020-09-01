import java.io.File

fun main() {
    val npDataset = NPDataset.fromSpreadSheet("/home/bjo/Documents/0-Chicago/Current/ITR/FracSchemes/2020-08-31_Jin.ods")

    File("/tmp/test.dot").bufferedWriter().use {
        val gr = npDataset.filterGraph(from=npDataset.methods["JIN_M_0003"], to=npDataset.fractions["JIN_0084"]).toDOTGraph()
        it.write(gr)
    }
}