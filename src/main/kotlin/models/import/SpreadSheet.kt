package net.nprod.nphandler.models.import

import com.github.miachm.sods.SpreadSheet
import net.nprod.nphandler.InvalidMethodTypeException
import net.nprod.nphandler.NPDataset
import net.nprod.nphandler.UnknownFractionInMethodException
import net.nprod.nphandler.models.Fraction
import net.nprod.nphandler.models.Method
import net.nprod.nphandler.models.MethodType
import java.io.File

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


/**
 * Generate a NPDataset from the given ODS spreadsheet
 */
fun npDatasetFromSpreadSheet(name: String): NPDataset {
    val spread = SpreadSheet(File(name))
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