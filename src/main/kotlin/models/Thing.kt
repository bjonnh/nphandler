package net.nprod.nphandler.models

/**
 * Interface that all the handled objects for our graphs have to abide by
 */
interface Thing {
    val prefix: String
    val id: String
    val realId: String
    val name: String
}
