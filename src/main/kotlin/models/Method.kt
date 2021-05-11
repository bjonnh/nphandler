package net.nprod.nphandler.models

data class Method(
    override val prefix: String,
    override val id: String,
    override val realId: String,
    override val name: String,
    val type: MethodType,
    val description: String,
    val inputFractions: List<Fraction>,
    val outputFractions: List<Fraction>
): Thing
