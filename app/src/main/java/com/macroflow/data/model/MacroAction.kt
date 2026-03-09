package com.macroflow.data.model

data class MacroAction(
    val id: Int = 0,
    val type: String,
    val x: Float,
    val y: Float,
    val scrollDirection: String? = null,
    val scrollDistance: Int? = null,
    val delayAfter: Long = 0L
)
