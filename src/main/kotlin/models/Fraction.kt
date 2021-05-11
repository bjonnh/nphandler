package net.nprod.nphandler.models

data class Fraction (
    override val prefix: String,
    override val id: String,
    override val realId: String,
    override val name: String,
    val weight: Number?,
    val weightLeft: Number?,
    val location: String,
) : Thing
